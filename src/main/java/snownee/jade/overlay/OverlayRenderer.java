package snownee.jade.overlay;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;

import org.apache.commons.lang3.mutable.MutableObject;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback.ColorSetting;
import snownee.jade.api.callback.JadeRenderBackgroundCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.api.theme.Theme;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.impl.config.WailaConfig.ConfigOverlay;
import snownee.jade.util.ClientProxy;

public class OverlayRenderer {

	public static final MutableObject<Theme> theme = new MutableObject<>(IWailaConfig.get().getOverlay().getTheme());
	public static float ticks;
	public static boolean shown;
	public static float alpha;
	private static TooltipRenderer lingerTooltip;
	private static float disappearTicks;
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

		BossBarOverlapMode mode = Jade.CONFIG.get().getGeneral().getBossBarOverlapMode();
		if (mode == BossBarOverlapMode.HIDE_TOOLTIP && ClientProxy.getBossBarRect() != null) {
			return false;
		}

		return true;
	}

	public static boolean shouldShowImmediately(TooltipRenderer tooltipRenderer) {
		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null)
			return false;

		if (!ClientProxy.shouldShowWithOverlay(mc, mc.screen)) {
			return false;
		}

		tooltipRenderer.recalculateRealRect();
		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (mc.screen instanceof BaseOptionsScreen optionsScreen) {
			if (!general.previewOverlay && !optionsScreen.forcePreviewOverlay()) {
				return false;
			}
			Rect2i position = tooltipRenderer.getRealRect();
			Window window = mc.getWindow();
			double x = mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
			double y = mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
			if (position.contains((int) x, (int) y)) {
				return false;
			}
		}

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
	 * NOTE!!!
	 * <p>
	 * Please do NOT replace the whole codes with Mixin.
	 * It will make me unable to locate bugs.
	 * A regular plugin can also realize the same features.
	 * <p>
	 * Secondly, please notice the license that Jade is using.
	 * I don't think it is compatible with some open-source licenses.
	 */
	public static void renderOverlay478757(GuiGraphics guiGraphics) {
		shown = false;
		boolean show = shouldShow();
		TooltipRenderer tooltipRenderer = WailaTickHandler.instance().tooltipRenderer;
		float delta = Minecraft.getInstance().getDeltaFrameTime();
		ConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (tooltipRenderer != null) {
			lingerTooltip = tooltipRenderer;
		}
		if (tooltipRenderer == null && lingerTooltip != null) {
			disappearTicks += delta;
			if (disappearTicks < overlay.getDisappearingDelay()) {
				tooltipRenderer = lingerTooltip;
				show = true;
			}
		} else {
			disappearTicks = 0;
		}
		if (overlay.getAnimation() && lingerTooltip != null) {
			tooltipRenderer = lingerTooltip;
			float speed = general.isDebug() ? 0.1F : 0.6F;
			alpha += (show ? speed : -speed) * delta;
			alpha = Mth.clamp(alpha, 0, 1);
		} else {
			alpha = show ? 1 : 0;
		}

		if (alpha < 0.1F || tooltipRenderer == null || !shouldShowImmediately(tooltipRenderer)) {
			lingerTooltip = null;
			morphRect = null;
			WailaTickHandler.clearLastNarration();
			return;
		}

		ticks += delta;
		Minecraft.getInstance().getProfiler().push("Jade Overlay");
		renderOverlay(tooltipRenderer, guiGraphics);
		Minecraft.getInstance().getProfiler().pop();
	}

	public static void renderOverlay(TooltipRenderer tooltip, GuiGraphics guiGraphics) {
		PoseStack matrixStack = guiGraphics.pose();
		matrixStack.pushPose();

		Rect2i position = Objects.requireNonNull(tooltip.getRealRect());

		if (morphRect == null) {
			morphRect = new Rect2i(position.getX(), position.getY(), position.getWidth(), position.getHeight());
		} else {
			chase(position, Rect2i::getX, morphRect::setX);
			chase(position, Rect2i::getY, morphRect::setY);
			chase(position, Rect2i::getWidth, morphRect::setWidth);
			chase(position, Rect2i::getHeight, morphRect::setHeight);
		}

		ColorSetting colorSetting = new ColorSetting();
		ConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		colorSetting.alpha = overlay.getAlpha();
		Theme themeBefore = overlay.getTheme();
		theme.setValue(themeBefore);
		colorSetting.theme = theme;
		for (JadeBeforeRenderCallback callback : WailaClientRegistration.INSTANCE.beforeRenderCallback.callbacks()) {
			if (callback.beforeRender(tooltip.getTooltip(), morphRect, guiGraphics, ObjectDataCenter.get(), colorSetting)) {
				matrixStack.popPose();
				return;
			}
		}
		if (themeBefore != theme.getValue()) {
			tooltip.setPaddingFromTheme(theme.getValue());
		}

		float z = Minecraft.getInstance().screen == null ? 1 : 100;
		matrixStack.translate(morphRect.getX(), morphRect.getY(), z);

		float scale = tooltip.getRealScale();
		if (scale != 1) {
			matrixStack.scale(scale, scale, 1.0F);
		}

		boolean doDefault = true;
		colorSetting.alpha *= alpha;
		for (JadeRenderBackgroundCallback callback : WailaClientRegistration.INSTANCE.renderBackgroundCallback.callbacks()) {
			if (callback.onRender(tooltip, morphRect, guiGraphics, ObjectDataCenter.get(), colorSetting)) {
				doDefault = false;
				break;
			}
		}
		RenderSystem.enableBlend();
		if (doDefault && colorSetting.alpha > 0) {
			drawTooltipBox(guiGraphics, 0, 0, Mth.ceil(morphRect.getWidth() / scale), Mth.ceil(morphRect.getHeight() / scale), colorSetting.alpha, overlay.getSquare(), tooltip);
		}

		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		tooltip.draw(guiGraphics);

		WailaClientRegistration.INSTANCE.afterRenderCallback.call(callback -> {
			callback.afterRender(tooltip.getTooltip(), morphRect, guiGraphics, ObjectDataCenter.get());
		});

		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		matrixStack.popPose();

		if (Jade.CONFIG.get().getGeneral().shouldEnableTextToSpeech()) {
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

	public static void drawTooltipBox(GuiGraphics guiGraphics, int x, int y, int w, int h, float alpha, boolean square, TooltipRenderer tooltip) {
		Theme theme = OverlayRenderer.theme.getValue();
		if (theme.backgroundTexture != null) {
			ResourceLocation texture = theme.backgroundTexture;
			int[] uv = theme.backgroundTextureUV;
			if (theme.backgroundTexture_withIcon != null && tooltip.hasIcon()) {
				texture = theme.backgroundTexture_withIcon;
				uv = theme.backgroundTextureUV_withIcon;
			}
			RenderSystem.setShaderColor(1, 1, 1, alpha);
			//guiGraphics.blitNineSliced(theme.backgroundTexture, x, y, w, h, theme.backgroundTextureUV[0], theme.backgroundTextureUV[1], theme.backgroundTextureUV[2], theme.backgroundTextureUV[3], theme.backgroundTextureUV[4], theme.backgroundTextureUV[5], theme.backgroundTextureUV[6], theme.backgroundTextureUV[7]);
			blitNineSliced(guiGraphics, texture, x, y, w, h, uv[3], uv[0], uv[1], uv[2], uv[4], uv[5], uv[6], uv[7]);
			RenderSystem.setShaderColor(1, 1, 1, 1);
		} else {
			if (!square) {
				w -= 2;
				h -= 2;
			}
			int bg = theme.backgroundColor;
			if (bg != -1) {
				bg = IWailaConfig.IConfigOverlay.applyAlpha(bg, alpha);
				DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x + 1, y + 1, w - 2, h - 2, bg, bg);//center
				if (!square) {
					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x, y - 1, w, 1, bg, bg);
					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x, y + h, w, 1, bg, bg);
					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x - 1, y, 1, h, bg, bg);
					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x + w, y, 1, h, bg, bg);
				}
			}
			int[] borderColors = new int[4];
			for (int i = 0; i < 4; i++) {
				borderColors[i] = IWailaConfig.IConfigOverlay.applyAlpha(theme.borderColor[i], alpha);
			}
			DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x, y + 1, 1, h - 2, borderColors[0], borderColors[3]);
			DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x + w - 1, y + 1, 1, h - 2, borderColors[1], borderColors[2]);
			DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x, y, w, 1, borderColors[0], borderColors[1]);
			DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x, y + h - 1, w, 1, borderColors[3], borderColors[2]);
		}
	}

	public static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation pAtlasLocation, int pTargetX, int pTargetY, int pTargetWidth, int pTargetHeight, int pCornerWidth, int pCornerHeight, int pEdgeWidth, int pEdgeHeight, int pSourceWidth, int pSourceHeight, int pSourceX, int pSourceY) {
		pCornerWidth = Math.min(pCornerWidth, pTargetWidth / 2);
		pEdgeWidth = Math.min(pEdgeWidth, pTargetWidth / 2);
		pCornerHeight = Math.min(pCornerHeight, pTargetHeight / 2);
		pEdgeHeight = Math.min(pEdgeHeight, pTargetHeight / 2);
		if (pTargetWidth == pSourceWidth && pTargetHeight == pSourceHeight) {
			guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pTargetWidth, pTargetHeight);
		} else if (pTargetHeight == pSourceHeight) {
			guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pCornerWidth, pTargetHeight);
			guiGraphics.blitRepeating(pAtlasLocation, pTargetX + pCornerWidth, pTargetY, pTargetWidth - pEdgeWidth - pCornerWidth, pTargetHeight, pSourceX + pCornerWidth, pSourceY, pSourceWidth - pEdgeWidth - pCornerWidth, pSourceHeight);
			guiGraphics.blit(pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY, pSourceX + pSourceWidth - pEdgeWidth, pSourceY, pEdgeWidth, pTargetHeight);
		} else if (pTargetWidth == pSourceWidth) {
			guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pTargetWidth, pCornerHeight);
			guiGraphics.blitRepeating(pAtlasLocation, pTargetX, pTargetY + pCornerHeight, pTargetWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX, pSourceY + pCornerHeight, pSourceWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
			guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY + pTargetHeight - pEdgeHeight, pSourceX, pSourceY + pSourceHeight - pEdgeHeight, pTargetWidth, pEdgeHeight);
		} else {
			guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY, pSourceX, pSourceY, pCornerWidth, pCornerHeight);
			guiGraphics.blitRepeating(pAtlasLocation, pTargetX + pCornerWidth, pTargetY, pTargetWidth - pEdgeWidth - pCornerWidth, pCornerHeight, pSourceX + pCornerWidth, pSourceY, pSourceWidth - pEdgeWidth - pCornerWidth, pCornerHeight);
			guiGraphics.blit(pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY, pSourceX + pSourceWidth - pEdgeWidth, pSourceY, pEdgeWidth, pCornerHeight);
			guiGraphics.blit(pAtlasLocation, pTargetX, pTargetY + pTargetHeight - pEdgeHeight, pSourceX, pSourceY + pSourceHeight - pEdgeHeight, pCornerWidth, pEdgeHeight);
			guiGraphics.blitRepeating(pAtlasLocation, pTargetX + pCornerWidth, pTargetY + pTargetHeight - pEdgeHeight, pTargetWidth - pEdgeWidth - pCornerWidth, pEdgeHeight, pSourceX + pCornerWidth, pSourceY + pSourceHeight - pEdgeHeight, pSourceWidth - pEdgeWidth - pCornerWidth, pEdgeHeight);
			guiGraphics.blit(pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY + pTargetHeight - pEdgeHeight, pSourceX + pSourceWidth - pEdgeWidth, pSourceY + pSourceHeight - pEdgeHeight, pEdgeWidth, pEdgeHeight);
			guiGraphics.blitRepeating(pAtlasLocation, pTargetX, pTargetY + pCornerHeight, pCornerWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX, pSourceY + pCornerHeight, pCornerWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
			guiGraphics.blitRepeating(pAtlasLocation, pTargetX + pCornerWidth, pTargetY + pCornerHeight, pTargetWidth - pEdgeWidth - pCornerWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX + pCornerWidth, pSourceY + pCornerHeight, pSourceWidth - pEdgeWidth - pCornerWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
			guiGraphics.blitRepeating(pAtlasLocation, pTargetX + pTargetWidth - pEdgeWidth, pTargetY + pCornerHeight, pEdgeWidth, pTargetHeight - pEdgeHeight - pCornerHeight, pSourceX + pSourceWidth - pEdgeWidth, pSourceY + pCornerHeight, pEdgeWidth, pSourceHeight - pEdgeHeight - pCornerHeight);
		}
	}
}
