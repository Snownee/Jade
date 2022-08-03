package snownee.jade.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.callback.JadeAfterRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback.ColorSetting;
import snownee.jade.api.callback.JadeRenderBackgroundCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.config.IWailaConfig.IconMode;
import snownee.jade.api.config.Theme;
import snownee.jade.api.ui.IElement;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.util.Color;

public class OverlayRenderer {

	public static float ticks;
	public static int backgroundColorRaw;
	public static int gradientStartRaw;
	public static int gradientEndRaw;
	public static int stressedTextColorRaw;
	public static int normalTextColorRaw;

	public static void renderOverlay(PoseStack poseStack) {
		if (WailaTickHandler.instance().tooltipRenderer == null)
			return;

		if (!Jade.CONFIG.get().getGeneral().shouldDisplayTooltip())
			return;

		if (Jade.CONFIG.get().getGeneral().getDisplayMode() == IWailaConfig.DisplayMode.HOLD_KEY && !JadeClient.showOverlay.isDown())
			return;

		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null)
			return;

		if (RayTracing.INSTANCE.getTarget() == null)
			return;

		if (mc.screen != null) {
			if (!(mc.screen instanceof BaseOptionsScreen)) {
				return;
			} else {
				Rect2i position = WailaTickHandler.instance().tooltipRenderer.getPosition();
				IConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
				Window window = mc.getWindow();
				double x = mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
				double y = mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
				x += position.getWidth() * overlay.tryFlip(overlay.getAnchorX());
				y += position.getHeight() * overlay.getAnchorY();
				if (position.contains((int) x, (int) y)) {
					return;
				}
			}
		}

		if (mc.gui.getTabList().visible || mc.getOverlay() != null || mc.options.hideGui)
			return;

		if (mc.options.renderDebug && Jade.CONFIG.get().getGeneral().shouldHideFromDebug())
			return;

		ticks += mc.getDeltaFrameTime();
		if (RayTracing.INSTANCE.getTarget().getType() != HitResult.Type.MISS)
			renderOverlay(WailaTickHandler.instance().tooltipRenderer, poseStack);
	}

	public static void renderOverlay(TooltipRenderer tooltip, PoseStack matrixStack) {
		Minecraft.getInstance().getProfiler().push("Waila Overlay");
		matrixStack.pushPose();

		Rect2i position = tooltip.getPosition();
		IConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		if (!overlay.getSquare()) {
			position.setWidth(position.getWidth() + 2);
			position.setHeight(position.getHeight() + 2);
		}

		ColorSetting colorSetting = new ColorSetting();
		colorSetting.alpha = overlay.getAlpha();
		colorSetting.backgroundColor = backgroundColorRaw;
		colorSetting.gradientStart = gradientStartRaw;
		colorSetting.gradientEnd = gradientEndRaw;
		for (JadeBeforeRenderCallback callback : WailaClientRegistration.INSTANCE.beforeRenderCallbacks) {
			if (callback.beforeRender(tooltip.getTooltip(), position, matrixStack, ObjectDataCenter.get(), colorSetting)) {
				matrixStack.popPose();
				return;
			}
		}

		//RenderSystem.disableRescaleNormal();
		//Lighting.disableStandardItemLighting();
		//RenderSystem.disableLighting();
		//RenderSystem.disableDepthTest();

		if (!overlay.getSquare()) {
			position.setPosition(position.getX() + 1, position.getY() + 1);
		}
		matrixStack.translate(position.getX(), position.getY(), 1);

		float scale = overlay.getOverlayScale();
		Window window = Minecraft.getInstance().getWindow();
		float thresholdHeight = window.getGuiScaledHeight() * overlay.getAutoScaleThreshold();
		if (position.getHeight() * scale > thresholdHeight) {
			scale = Math.max(scale * 0.5f, thresholdHeight / position.getHeight());
		}

		if (scale != 1) {
			matrixStack.scale(scale, scale, 1.0F);
		}
		matrixStack.translate(-position.getWidth() * overlay.tryFlip(overlay.getAnchorX()), -position.getHeight() * overlay.getAnchorY(), 0);

		boolean doDefault = true;
		for (JadeRenderBackgroundCallback callback : WailaClientRegistration.INSTANCE.renderBackgroundCallbacks) {
			if (callback.onRender(tooltip, position, matrixStack, ObjectDataCenter.get(), colorSetting)) {
				doDefault = false;
				break;
			}
		}
		if (doDefault && colorSetting.alpha > 0) {
			drawTooltipBox(matrixStack, 0, 0, position.getWidth(), position.getHeight(), IConfigOverlay.applyAlpha(colorSetting.backgroundColor, colorSetting.alpha), IConfigOverlay.applyAlpha(colorSetting.gradientStart, colorSetting.alpha), IConfigOverlay.applyAlpha(colorSetting.gradientEnd, colorSetting.alpha), overlay.getSquare());
		}

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		tooltip.draw(matrixStack);
		RenderSystem.disableBlend();

		//RenderSystem.enableRescaleNormal();
		IElement icon = tooltip.getIcon();
		if (icon != null) {
			Vec2 size = tooltip.getIcon().getCachedSize();
			Vec2 offset = tooltip.getIcon().getTranslation();
			float offsetY;
			if (overlay.getIconMode() == IconMode.TOP) {
				offsetY = offset.y + tooltip.getPadding();
			} else {
				offsetY = (position.getHeight() - size.y) / 2 - 1;
			}
			float offsetX = offset.x + tooltip.getPadding() + 2;
			Tooltip.drawBorder(matrixStack, offsetX, offsetY, icon);
			icon.render(matrixStack, offsetX, offsetY, offsetX + size.x, offsetY + size.y);
		}

		for (JadeAfterRenderCallback callback : WailaClientRegistration.INSTANCE.afterRenderCallbacks) {
			callback.afterRender(tooltip.getTooltip(), position, matrixStack, ObjectDataCenter.get());
		}

		RenderSystem.enableDepthTest();
		matrixStack.popPose();
		Minecraft.getInstance().getProfiler().pop();

		if (Jade.CONFIG.get().getGeneral().shouldEnableTextToSpeech() && Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 5 == 0) {
			WailaTickHandler.narrate(tooltip.getTooltip(), true);
		}
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

	public static void updateTheme() {
		Theme theme = Jade.CONFIG.get().getOverlay().getTheme();
		backgroundColorRaw = Color.fromString(theme.backgroundColor).toInt();
		gradientEndRaw = Color.fromString(theme.gradientEnd).toInt();
		gradientStartRaw = Color.fromString(theme.gradientStart).toInt();
		normalTextColorRaw = IConfigOverlay.applyAlpha(Color.fromString(theme.normalTextColor).toInt(), 1);
		stressedTextColorRaw = IConfigOverlay.applyAlpha(Color.fromString(theme.stressedTextColor).toInt(), 1);
	}
}
