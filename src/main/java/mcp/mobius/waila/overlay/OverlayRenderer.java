package mcp.mobius.waila.overlay;

import java.awt.Rectangle;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.config.WailaConfig.ConfigOverlay;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import mcp.mobius.waila.gui.OptionsScreen;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mcp.mobius.waila.impl.Tooltip;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
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
	public static float ticks;

	public static void renderOverlay() {
		if (WailaTickHandler.instance().tooltipRenderer == null)
			return;

		if (!Waila.CONFIG.get().getGeneral().shouldDisplayTooltip())
			return;

		if (Waila.CONFIG.get().getGeneral().getDisplayMode() == WailaConfig.DisplayMode.HOLD_KEY && !WailaClient.showOverlay.getKeyBinding().isKeyDown())
			return;

		Minecraft mc = Minecraft.getInstance();

		if (mc.world == null)
			return;

		if (RayTracing.INSTANCE.getTarget() == null)
			return;

		if (mc.currentScreen != null) {
			if (!(mc.currentScreen instanceof OptionsScreen)) {
				return;
			} else {
				Rectangle position = WailaTickHandler.instance().tooltipRenderer.getPosition();
				ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
				double x = mc.mouseHelper.getMouseX() * (double) mc.getMainWindow().getScaledWidth() / mc.getMainWindow().getWidth();
				double y = mc.mouseHelper.getMouseY() * (double) mc.getMainWindow().getScaledHeight() / mc.getMainWindow().getHeight();
				x += position.width * overlay.tryFlip(overlay.getAnchorX());
				y += position.height * overlay.getAnchorY();
				if (position.contains(x, y)) {
					return;
				}
			}
		}

		if (mc.ingameGUI.getTabList().visible || mc.loadingGui != null || !Minecraft.isGuiEnabled())
			return;

		if (mc.gameSettings.showDebugInfo && Waila.CONFIG.get().getGeneral().shouldHideFromDebug())
			return;

		ticks += mc.getTickLength();
		if (RayTracing.INSTANCE.getTarget().getType() != RayTraceResult.Type.MISS)
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

		Rectangle position = tooltip.getPosition();
		ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
		if (!overlay.getSquare()) {
			position.width += 2;
			position.height += 2;
		}
		WailaRenderEvent.Pre preEvent = new WailaRenderEvent.Pre(ObjectDataCenter.get(), position, matrixStack);
		if (MinecraftForge.EVENT_BUS.post(preEvent)) {
			loadGLState();
			matrixStack.pop();
			return;
		}

		RenderSystem.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		RenderSystem.disableLighting();
		//RenderSystem.disableDepthTest();

		position = preEvent.getPosition();
		ConfigOverlay configOverlay = Waila.CONFIG.get().getOverlay();
		if (!configOverlay.getSquare()) {
			position.x++;
			position.y++;
		}
		matrixStack.translate(position.x, position.y, 1);

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
		if (color.getAlpha() > 0) {
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
			Vector2f size = tooltip.icon.getCachedSize();
			Vector2f offset = tooltip.icon.getTranslation();
			float offsetX = offset.x + 5;
			float offsetY = offset.y + 2;
			Tooltip.drawBorder(matrixStack, offsetX, offsetY, tooltip.icon);
			tooltip.icon.render(matrixStack, offsetX, offsetY, offsetX + size.x, offsetY + size.y); //TODO
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
		if (!square) {
			w -= 2;
			h -= 2;
		}
		DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + 1, y + 1, w - 2, h - 2, bg, bg);//center
		if (!square) {
			DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x, y - 1, w, 1, bg, bg);
			DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x, y + h, w, 1, bg, bg);
			DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x - 1, y, 1, h, bg, bg);
			DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + w, y, 1, h, bg, bg);
		}
		DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x, y + 1, 1, h - 2, grad1, grad2);
		DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x + w - 1, y + 1, 1, h - 2, grad1, grad2);
		DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x, y, w, 1, grad1, grad1);
		DisplayHelper.INSTANCE.drawGradientRect(matrixStack, x, y + h - 1, w, 1, grad2, grad2);
	}
}
