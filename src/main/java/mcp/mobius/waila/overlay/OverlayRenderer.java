package mcp.mobius.waila.overlay;

import java.awt.Rectangle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.config.WailaConfig.ConfigOverlay;
import mcp.mobius.waila.api.config.WailaConfig.ConfigGeneral.IconMode;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import mcp.mobius.waila.gui.OptionsScreen;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mcp.mobius.waila.impl.Tooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.MinecraftForge;

public class OverlayRenderer {

	public static float ticks;

	public static void renderOverlay() {
		if (WailaTickHandler.instance().tooltipRenderer == null)
			return;

		if (!Waila.CONFIG.get().getGeneral().shouldDisplayTooltip())
			return;

		if (Waila.CONFIG.get().getGeneral().getDisplayMode() == WailaConfig.DisplayMode.HOLD_KEY && !WailaClient.showOverlay.isDown())
			return;

		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null)
			return;

		if (RayTracing.INSTANCE.getTarget() == null)
			return;

		if (mc.screen != null) {
			if (!(mc.screen instanceof OptionsScreen)) {
				return;
			} else {
				Rectangle position = WailaTickHandler.instance().tooltipRenderer.getPosition();
				ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
				Window window = mc.getWindow();
				double x = mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
				double y = mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
				x += position.width * overlay.tryFlip(overlay.getAnchorX());
				y += position.height * overlay.getAnchorY();
				if (position.contains(x, y)) {
					return;
				}
			}
		}

		if (mc.gui.getTabList().visible || mc.getOverlay() != null || mc.options.hideGui)
			return;

		if (mc.options.renderDebug && Waila.CONFIG.get().getGeneral().shouldHideFromDebug())
			return;

		ticks += mc.getDeltaFrameTime();
		if (RayTracing.INSTANCE.getTarget().getType() != HitResult.Type.MISS)
			renderOverlay(WailaTickHandler.instance().tooltipRenderer, new PoseStack());
	}

	public static void renderOverlay(TooltipRenderer tooltip, PoseStack matrixStack) {
		Minecraft.getInstance().getProfiler().push("Waila Overlay");
		matrixStack.pushPose();

		Rectangle position = tooltip.getPosition();
		ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
		if (!overlay.getSquare()) {
			position.width += 2;
			position.height += 2;
		}
		WailaRenderEvent.Pre preEvent = new WailaRenderEvent.Pre(ObjectDataCenter.get(), position, matrixStack);
		if (MinecraftForge.EVENT_BUS.post(preEvent)) {
			matrixStack.popPose();
			return;
		}

		//RenderSystem.disableRescaleNormal();
		//Lighting.disableStandardItemLighting();
		//RenderSystem.disableLighting();
		//RenderSystem.disableDepthTest();

		position = preEvent.getPosition();
		ConfigOverlay configOverlay = Waila.CONFIG.get().getOverlay();
		if (!configOverlay.getSquare()) {
			position.x++;
			position.y++;
		}
		matrixStack.translate(position.x, position.y, 1);

		float scale = configOverlay.getOverlayScale();
		Window window = Minecraft.getInstance().getWindow();
		float thresholdHeight = window.getGuiScaledHeight() * configOverlay.getAutoScaleThreshold();
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

		//RenderSystem.enableRescaleNormal();
		if (tooltip.hasIcon()) {
			Vec2 size = tooltip.icon.getCachedSize();
			Vec2 offset = tooltip.icon.getTranslation();
			float offsetY;
			if (Waila.CONFIG.get().getGeneral().getIconMode() == IconMode.TOP) {
				offsetY = offset.y + 2;
			} else {
				offsetY = (position.height - size.y) / 2 - 1;
			}
			float offsetX = offset.x + 5;
			Tooltip.drawBorder(matrixStack, offsetX, offsetY, tooltip.icon);
			tooltip.icon.render(matrixStack, offsetX, offsetY, offsetX + size.x, offsetY + size.y);
		}

		WailaRenderEvent.Post postEvent = new WailaRenderEvent.Post(position, matrixStack);
		MinecraftForge.EVENT_BUS.post(postEvent);

		RenderSystem.enableDepthTest();
		matrixStack.popPose();
		Minecraft.getInstance().getProfiler().pop();
	}

	public static void drawTooltipBox(PoseStack matrixStack, int x, int y, int w, int h, int bg, int grad1, int grad2, boolean square) {
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
