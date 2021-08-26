package mcp.mobius.waila.overlay;

import java.awt.Rectangle;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.addons.core.PluginCore;
import mcp.mobius.waila.api.RenderContext;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import mcp.mobius.waila.api.impl.DataAccessor;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import mcp.mobius.waila.api.impl.config.WailaConfig;
import mcp.mobius.waila.api.impl.config.WailaConfig.IconMode;
import mcp.mobius.waila.gui.GuiOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;

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
		if (WailaTickHandler.instance().tooltip == null)
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
			if (!(mc.currentScreen instanceof GuiOptions)) {
				return;
			} else {
				Rectangle position = WailaTickHandler.instance().tooltip.getPosition();
				double x = mc.mouseHelper.getMouseX() * (double) mc.getMainWindow().getScaledWidth() / mc.getMainWindow().getWidth();
				double y = mc.mouseHelper.getMouseY() * (double) mc.getMainWindow().getScaledHeight() / mc.getMainWindow().getHeight();
				if (position.contains(x, y)) {
					return;
				}
			}
		}

		if (mc.ingameGUI.getTabList().visible || mc.loadingGui != null || !Minecraft.isGuiEnabled())
			return;

		if (mc.gameSettings.showDebugInfo && Waila.CONFIG.get().getGeneral().shouldHideFromDebug())
			return;

		if (RayTracing.INSTANCE.getTarget().getType() == RayTraceResult.Type.BLOCK)
			renderOverlay(WailaTickHandler.instance().tooltip, new MatrixStack());

		if (RayTracing.INSTANCE.getTarget().getType() == RayTraceResult.Type.ENTITY && PluginConfig.INSTANCE.get(PluginCore.CONFIG_SHOW_ENTITY))
			renderOverlay(WailaTickHandler.instance().tooltip, new MatrixStack());
	}

	public static void enableGUIStandardItemLighting() {
		RenderSystem.pushMatrix();
		RenderSystem.rotatef(-30.0F, 0.0F, 1.0F, 0.0F);
		RenderSystem.rotatef(165.0F, 1.0F, 0.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		RenderSystem.popMatrix();
	}

	public static void renderOverlay(Tooltip tooltip, MatrixStack matrixStack) {
		Minecraft.getInstance().getProfiler().startSection("Waila Overlay");
		RenderContext.matrixStack = matrixStack;
		matrixStack.push();
		saveGLState();

		WailaRenderEvent.Pre preEvent = new WailaRenderEvent.Pre(DataAccessor.INSTANCE, tooltip.getPosition());
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
		//float scale = Waila.CONFIG.get().getOverlay().getOverlayScale();
		//matrixStack.translate(position.x, position.y, 0);
		//matrixStack.scale(scale, scale, 1.0F);

		WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
		if (color.getRawAlpha() > 0) {
			WailaRenderEvent.Color colorEvent = new WailaRenderEvent.Color(color.getAlpha(), color.getBackgroundColor(), color.getGradientStart(), color.getGradientEnd());
			MinecraftForge.EVENT_BUS.post(colorEvent);
			drawTooltipBox(matrixStack, position.x, position.y, position.width, position.height, colorEvent.getBackground(), colorEvent.getGradientStart(), colorEvent.getGradientEnd(), Waila.CONFIG.get().getOverlay().getSquare());
		}

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		tooltip.draw();
		RenderSystem.disableBlend();

		if (tooltip.hasItem())
			enableGUIStandardItemLighting();

		RenderSystem.enableRescaleNormal();
		if (tooltip.hasItem()) {
			if (tooltip.identifierStack == null) {
				tooltip.identifierStack = RayTracing.INSTANCE.getIdentifierStack();
			}
			int y = position.y;
			if (Waila.CONFIG.get().getGeneral().getIconMode() == IconMode.TOP) {
				y += 2;
			} else {
				y += position.height / 2 - 8;
			}
			DisplayUtil.renderStack(matrixStack, position.x + 5, y, tooltip.identifierStack, 1);
		}

		WailaRenderEvent.Post postEvent = new WailaRenderEvent.Post(position);
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
		DisplayUtil.drawGradientRect(matrixStack, x + 1, y + 1, w - 1, h - 1, bg, bg);//center
		if (!square) {
			DisplayUtil.drawGradientRect(matrixStack, x + 1, y, w - 1, 1, bg, bg);
			DisplayUtil.drawGradientRect(matrixStack, x + 1, y + h, w - 1, 1, bg, bg);
			DisplayUtil.drawGradientRect(matrixStack, x, y + 1, 1, h - 1, bg, bg);
			DisplayUtil.drawGradientRect(matrixStack, x + w, y + 1, 1, h - 1, bg, bg);
		}
		DisplayUtil.drawGradientRect(matrixStack, x + 1, y + 2, 1, h - 3, grad1, grad2);
		DisplayUtil.drawGradientRect(matrixStack, x + w - 1, y + 2, 1, h - 3, grad1, grad2);
		DisplayUtil.drawGradientRect(matrixStack, x + 1, y + 1, w - 1, 1, grad1, grad1);
		DisplayUtil.drawGradientRect(matrixStack, x + 1, y + h - 1, w - 1, 1, grad2, grad2);
	}
}
