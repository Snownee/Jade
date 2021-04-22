package mcp.mobius.waila.overlay;

import java.awt.Rectangle;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.addons.core.CorePlugin;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import mcp.mobius.waila.api.ui.Size;
import mcp.mobius.waila.impl.DataAccessor;
import mcp.mobius.waila.impl.Tooltip;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.impl.config.WailaConfig;
import mcp.mobius.waila.impl.config.WailaConfig.ConfigOverlay;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;

@SuppressWarnings("deprecation")
public class OverlayRenderer {

	protected static boolean hasLight;
	protected static boolean hasDepthTest;
	protected static boolean hasLight0;
	protected static boolean hasLight1;
	protected static boolean hasRescaleNormal;
	protected static boolean hasColorMaterial;
	protected static boolean depthMask;
	protected static int depthFunc;

	public static void renderOverlay() {
		if (WailaTickHandler.instance().tooltipRenderer == null)
			return;

		if (!Waila.CONFIG.get().getGeneral().shouldDisplayTooltip())
			return;

		if (Waila.CONFIG.get().getGeneral().getDisplayMode() == WailaConfig.DisplayMode.HOLD_KEY && !WailaClient.showOverlay.getKeyBinding().isKeyDown())
			return;

		Minecraft mc = Minecraft.getInstance();
		if ((mc.currentScreen != null && mc.gameSettings.chatVisibility != ChatVisibility.HIDDEN) || mc.world == null)
			return;

		if (mc.ingameGUI.getTabList().visible || mc.loadingGui != null || !Minecraft.isGuiEnabled())
			return;

		if (mc.gameSettings.showDebugInfo && Waila.CONFIG.get().getGeneral().shouldHideFromDebug())
			return;

		if (RayTracing.INSTANCE.getTarget() == null)
			return;

		if (RayTracing.INSTANCE.getTarget().getType() == RayTraceResult.Type.BLOCK)
			renderOverlay(WailaTickHandler.instance().tooltipRenderer, new MatrixStack());

		if (RayTracing.INSTANCE.getTarget().getType() == RayTraceResult.Type.ENTITY && PluginConfig.INSTANCE.get(CorePlugin.CONFIG_ENTITY))
			renderOverlay(WailaTickHandler.instance().tooltipRenderer, new MatrixStack());
	}

	public static void enableGUIStandardItemLighting() {
		RenderSystem.pushMatrix();
		RenderSystem.rotatef(-30.0F, 0.0F, 1.0F, 0.0F);
		RenderSystem.rotatef(165.0F, 1.0F, 0.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		RenderSystem.popMatrix();
	}

	public static void renderOverlay(TooltipRenderer tooltip, MatrixStack matrixStack) {
		Minecraft.getInstance().getProfiler().startSection("Waila Overlay");
		matrixStack.push();
		saveGLState();

		WailaRenderEvent.Pre preEvent = new WailaRenderEvent.Pre(DataAccessor.INSTANCE, tooltip.getPosition(), matrixStack);
		if (MinecraftForge.EVENT_BUS.post(preEvent)) {
			loadGLState();
			matrixStack.pop();
			return;
		}

		RenderSystem.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();

		Rectangle position = preEvent.getPosition();
		ConfigOverlay configOverlay = Waila.CONFIG.get().getOverlay();
		matrixStack.translate(position.x, position.y, 0);

		float scale = configOverlay.getOverlayScale();
		MainWindow window = Minecraft.getInstance().getMainWindow();
		float thresholdHeight = window.getScaledHeight() * configOverlay.getAutoScaleThreshold();
		if (position.height * scale > thresholdHeight) {
			scale = Math.max(scale * 0.5f, thresholdHeight / position.height);
		}

		if (scale != 1) {
			matrixStack.scale(scale, scale, 1.0F);
		}
		matrixStack.translate(-position.width * configOverlay.tryFlip(configOverlay.getAnchorX()), -position.height * configOverlay.getAnchorY(), 0);

		WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
		if (color.getRawAlpha() > 0) {
			WailaRenderEvent.Color colorEvent = new WailaRenderEvent.Color(color.getAlpha(), color.getBackgroundColor(), color.getGradientStart(), color.getGradientEnd());
			MinecraftForge.EVENT_BUS.post(colorEvent);
			drawTooltipBox(matrixStack, 0, 0, position.width, position.height, colorEvent.getBackground(), colorEvent.getGradientStart(), colorEvent.getGradientEnd(), Waila.CONFIG.get().getOverlay().getSquare());
		}

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		tooltip.draw(matrixStack);
		RenderSystem.disableBlend();

		RenderSystem.enableRescaleNormal();
		if (tooltip.hasIcon()) {
			Size size = tooltip.icon.getCachedSize();
			Size offset = tooltip.icon.getTranslation();
			int offsetX = offset.width + 5;
			int offsetY = offset.height + 2;
			Tooltip.drawBorder(matrixStack, offsetX, offsetY, tooltip.icon);
			tooltip.icon.render(matrixStack, offsetX, offsetY, offsetX + size.width, offsetY + size.height); //TODO
		}

		WailaRenderEvent.Post postEvent = new WailaRenderEvent.Post(position, matrixStack);
		MinecraftForge.EVENT_BUS.post(postEvent);

		loadGLState();
		RenderSystem.enableDepthTest();
		matrixStack.pop();
		Minecraft.getInstance().getProfiler().endSection();
	}

	public static void saveGLState() {
		hasLight = GL11.glGetBoolean(GL11.GL_LIGHTING);
		hasLight0 = GL11.glGetBoolean(GL11.GL_LIGHT0);
		hasLight1 = GL11.glGetBoolean(GL11.GL_LIGHT1);
		hasDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
		hasRescaleNormal = GL11.glGetBoolean(GL12.GL_RESCALE_NORMAL);
		hasColorMaterial = GL11.glGetBoolean(GL11.GL_COLOR_MATERIAL);
		depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
		depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
		GL11.glPushAttrib(GL11.GL_CURRENT_BIT); // Leave me alone :(
	}

	public static void loadGLState() {
		RenderSystem.depthMask(depthMask);
		RenderSystem.depthFunc(depthFunc);
		if (hasLight)
			RenderSystem.enableLighting();
		else
			RenderSystem.disableLighting();

		if (hasLight0)
			GlStateManager.enableLight(0);
		else
		//GlStateManager.disableLight(0);

		if (hasLight1)
			GlStateManager.enableLight(1);
		else
		//GlStateManager.disableLight(1);

		if (hasDepthTest)
			RenderSystem.enableDepthTest();
		else
			RenderSystem.disableDepthTest();
		if (hasRescaleNormal)
			RenderSystem.enableRescaleNormal();
		else
			RenderSystem.disableRescaleNormal();
		if (hasColorMaterial)
			RenderSystem.enableColorMaterial();
		else
			RenderSystem.disableColorMaterial();

		RenderSystem.popAttributes();
	}

	public static void drawTooltipBox(MatrixStack matrixStack, int x, int y, int w, int h, int bg, int grad1, int grad2, boolean square) {
		DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + 1, y + 1, w - 1, h - 1, bg, bg);//center
		if (!square) {
			DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + 1, y, w - 1, 1, bg, bg);
			DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + 1, y + h, w - 1, 1, bg, bg);
			DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x, y + 1, 1, h - 1, bg, bg);
			DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + w, y + 1, 1, h - 1, bg, bg);
		}
		DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + 1, y + 2, 1, h - 3, grad1, grad2);
		DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + w - 1, y + 2, 1, h - 3, grad1, grad2);
		DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + 1, y + 1, w - 1, 1, grad1, grad1);
		DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + 1, y + h - 1, w - 1, 1, grad2, grad2);
	}
}
