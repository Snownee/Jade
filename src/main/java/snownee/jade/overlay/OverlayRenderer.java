package snownee.jade.overlay;

import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback.ColorSetting;
import snownee.jade.api.callback.JadeRenderBackgroundCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.config.Theme;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.impl.config.WailaConfig.ConfigOverlay;
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
	public static float alpha;
	private static TooltipRenderer fadeTooltip;
	private static Rect2i morphRect;

	public static boolean shouldShow() {
		if (WailaTickHandler.instance().tooltipRenderer == null) {
			return false;
		}

		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (!general.shouldDisplayTooltip())
			return false;

		if (general.getDisplayMode() == IWailaConfig.DisplayMode.HOLD_KEY && !JadeClient.showOverlay.isDown())
			return false;

		return true;
	}

	public static boolean shouldShowImmediately(TooltipRenderer tooltipRenderer) {
		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null)
			return false;

		if (!ClientPlatformProxy.shouldShowWithOverlay(mc, mc.screen)) {
			return false;
		}

		if (mc.screen instanceof BaseOptionsScreen) {
			Rect2i position = tooltipRenderer.getPosition();
			Window window = mc.getWindow();
			double x = mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
			double y = mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
			ConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
			x += position.getWidth() * overlay.tryFlip(overlay.getAnchorX());
			y += position.getHeight() * overlay.getAnchorY();
			if (position.contains((int) x, (int) y)) {
				return false;
			}
		}

		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (mc.options.renderDebug && general.shouldHideFromDebug())
			return false;

		if (mc.getOverlay() != null || mc.options.hideGui)
			return false;

		if (mc.gui.getTabList().visible && general.shouldHideFromTabList()) {
			return false;
		}

		return true;
	}

	/**
	 *  NOTE!!!
	 *  
	 *  Please do NOT replace the whole codes with Mixin.
	 *  It will make me unable to locate bugs.
	 *  A regular plugin can also realize the same features.
	 *  
	 *  Secondly, please notice the license that Jade is using.
	 *  I don't think it is compatible with some open-source licenses.
	 */
	public static void renderOverlay478757(PoseStack poseStack) {
		shown = false;
		boolean show = shouldShow();
		TooltipRenderer tooltipRenderer = WailaTickHandler.instance().tooltipRenderer;
		float delta = Minecraft.getInstance().getDeltaFrameTime();
		ConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (overlay.getAnimation()) {
			if (tooltipRenderer == null) {
				tooltipRenderer = fadeTooltip;
			} else {
				fadeTooltip = tooltipRenderer;
			}
			float speed = general.isDebug() ? 0.1F : 0.6F;
			alpha += (show ? speed : -speed) * delta;
			alpha = Mth.clamp(alpha, 0, 1);
		} else {
			alpha = show ? 1 : 0;
		}

		if (alpha < 0.1F || tooltipRenderer == null || !shouldShowImmediately(tooltipRenderer)) {
			fadeTooltip = null;
			morphRect = null;
			return;
		}

		ticks += delta;
		Minecraft.getInstance().getProfiler().push("Jade Overlay");
		renderOverlay(tooltipRenderer, poseStack);
		Minecraft.getInstance().getProfiler().pop();
	}

	public static void renderOverlay(TooltipRenderer tooltip, PoseStack matrixStack) {
		matrixStack.pushPose();

		Rect2i position = tooltip.getPosition();
		ConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
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

		if (morphRect == null) {
			morphRect = new Rect2i(position.getX(), position.getY(), position.getWidth(), position.getHeight());
		} else {
			chase(position, Rect2i::getX, morphRect::setX);
			chase(position, Rect2i::getY, morphRect::setY);
			chase(position, Rect2i::getWidth, morphRect::setWidth);
			chase(position, Rect2i::getHeight, morphRect::setHeight);
		}

		ColorSetting colorSetting = new ColorSetting();
		colorSetting.alpha = overlay.getAlpha();
		colorSetting.backgroundColor = backgroundColorRaw;
		colorSetting.gradientStart = gradientStartRaw;
		colorSetting.gradientEnd = gradientEndRaw;
		for (JadeBeforeRenderCallback callback : WailaClientRegistration.INSTANCE.beforeRenderCallback.callbacks()) {
			if (callback.beforeRender(tooltip.getTooltip(), morphRect, matrixStack, ObjectDataCenter.get(), colorSetting)) {
				matrixStack.popPose();
				return;
			}
		}

		matrixStack.translate(morphRect.getX(), morphRect.getY(), 1);

		float scale = overlay.getOverlayScale();
		Window window = Minecraft.getInstance().getWindow();
		float thresholdHeight = window.getGuiScaledHeight() * overlay.getAutoScaleThreshold();
		if (position.getHeight() * scale > thresholdHeight) {
			scale = Math.max(scale * 0.5f, thresholdHeight / position.getHeight());
		}

		if (scale != 1) {
			matrixStack.scale(scale, scale, 1.0F);
		}
		matrixStack.translate((int) (-morphRect.getWidth() * overlay.tryFlip(overlay.getAnchorX())), (int) (-morphRect.getHeight() * overlay.getAnchorY()), 0);

		boolean doDefault = true;
		colorSetting.alpha *= alpha;
		for (JadeRenderBackgroundCallback callback : WailaClientRegistration.INSTANCE.renderBackgroundCallback.callbacks()) {
			if (callback.onRender(tooltip, morphRect, matrixStack, ObjectDataCenter.get(), colorSetting)) {
				doDefault = false;
				break;
			}
		}
		if (doDefault && colorSetting.alpha > 0) {
			drawTooltipBox(matrixStack, 0, 0, morphRect.getWidth(), morphRect.getHeight(), IConfigOverlay.applyAlpha(colorSetting.backgroundColor, colorSetting.alpha), IConfigOverlay.applyAlpha(colorSetting.gradientStart, colorSetting.alpha), IConfigOverlay.applyAlpha(colorSetting.gradientEnd, colorSetting.alpha), overlay.getSquare());
		}

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		tooltip.draw(matrixStack);
		RenderSystem.disableBlend();

		WailaClientRegistration.INSTANCE.afterRenderCallback.call(callback -> {
			callback.afterRender(tooltip.getTooltip(), morphRect, matrixStack, ObjectDataCenter.get());
		});

		RenderSystem.enableDepthTest();
		matrixStack.popPose();

		if (Jade.CONFIG.get().getGeneral().shouldEnableTextToSpeech() && Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 5 == 0) {
			WailaTickHandler.narrate(tooltip.getTooltip(), true);
		}

		shown = true;
	}

	private static void chase(Rect2i pos, ToIntFunction<Rect2i> getter, IntConsumer setter) {
		if (Jade.CONFIG.get().getOverlay().getAnimation()) {
			int value = getter.applyAsInt(morphRect);
			int target = getter.applyAsInt(pos);
			float diff = target - value;
			if (diff == 0) {
				return;
			}
			float delta = Minecraft.getInstance().getDeltaFrameTime() * 2;
			if (delta < 1)
				diff *= delta;
			if (Mth.abs(diff) < 1) {
				diff = diff > 0 ? 1 : -1;
			}
			setter.accept((int) (value + diff));
		} else {
			setter.accept(getter.applyAsInt(pos));
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
		backgroundColorRaw = Color.valueOf(theme.backgroundColor).toInt();
		gradientEndRaw = Color.valueOf(theme.gradientEnd).toInt();
		gradientStartRaw = Color.valueOf(theme.gradientStart).toInt();
		normalTextColorRaw = IConfigOverlay.applyAlpha(Color.valueOf(theme.normalTextColor).toInt(), 1);
		stressedTextColorRaw = IConfigOverlay.applyAlpha(Color.valueOf(theme.stressedTextColor).toInt(), 1);
	}
}
