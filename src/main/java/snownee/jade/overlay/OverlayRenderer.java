package snownee.jade.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.phys.Vec2;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.callback.JadeAfterRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback.ColorSetting;
import snownee.jade.api.callback.JadeRenderBackgroundCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.config.IWailaConfig.IconMode;
import snownee.jade.api.config.Theme;
import snownee.jade.api.ui.IElement;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.util.ClientPlatformProxy;
import snownee.jade.util.Color;

public class OverlayRenderer {

	public static float ticks;
	public static int backgroundColorRaw;
	public static int gradientStartRaw;
	public static int gradientEndRaw;
	public static int stressedTextColorRaw;
	public static int normalTextColorRaw;
	public static boolean shown;

	public static void renderOverlay(PoseStack poseStack) {
		shown = false;
		if (WailaTickHandler.instance().tooltipRenderer == null)
			return;

		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (!general.shouldDisplayTooltip())
			return;

		if (general.getDisplayMode() == IWailaConfig.DisplayMode.HOLD_KEY && !JadeClient.showOverlay.isDown())
			return;

		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null)
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

		if (mc.getOverlay() != null || mc.options.hideGui)
			return;

		if (mc.gui.getTabList().visible && general.shouldHideFromTabList()) {
			return;
		}

		if (mc.options.renderDebug && general.shouldHideFromDebug())
			return;

		ticks += mc.getDeltaFrameTime();
		renderOverlay(WailaTickHandler.instance().tooltipRenderer, poseStack);
	}

	public static void renderOverlay(TooltipRenderer tooltip, PoseStack matrixStack) {
		Minecraft.getInstance().getProfiler().push("Jade Overlay");
		matrixStack.pushPose();

		Rect2i position = tooltip.getPosition();
		IConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		if (!overlay.getSquare()) {
			position.setWidth(position.getWidth() + 2);
			position.setHeight(position.getHeight() + 2);
			position.setPosition(position.getX() + 1, position.getY() + 1);
		}

		BossBarOverlapMode mode = Jade.CONFIG.get().getGeneral().getBossBarOverlapMode();
		if (mode == BossBarOverlapMode.PUSH_DOWN) {
			Rect2i rect = ClientPlatformProxy.getBossBarRect();
			if (rect != null) {
				int tw = position.getWidth();
				int th = position.getHeight();
				int rw = rect.getWidth();
				int rh = rect.getHeight();
				int tx = position.getX();
				int ty = position.getY();
				int rx = rect.getX();
				int ry = rect.getY();
				rw += rx;
				rh += ry;
				tw += tx;
				th += ty;
				// check if tooltip intersects with boss bar
				if (rw > tx && rh > ty && tw > rx && th > ry) {
					position.setY(rect.getHeight());
				}
			}
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

		shown = true;
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
