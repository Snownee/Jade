package snownee.jade.overlay;

import org.apache.commons.lang3.mutable.MutableObject;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.TooltipRect;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.impl.config.WailaConfig.ConfigOverlay;
import snownee.jade.impl.ui.BoxElement;
import snownee.jade.util.ClientProxy;

public class OverlayRenderer {

	public static final MutableObject<Theme> theme = new MutableObject<>(IWailaConfig.get().getOverlay().getTheme());
	private static final TooltipRect rect = new TooltipRect();
	public static float ticks;
	public static boolean shown;
	public static float alpha;
	private static BoxElement lingerTooltip;
	private static float disappearTicks;

	public static boolean shouldShow() {
		if (WailaTickHandler.instance().rootElement == null) {
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

	public static boolean shouldShowImmediately(BoxElement box) {
		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null)
			return false;

		if (!ClientProxy.shouldShowWithOverlay(mc, mc.screen)) {
			return false;
		}

		box.updateExpectedRect(rect);
		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (mc.screen instanceof PreviewOptionsScreen optionsScreen) {
			if (!general.previewOverlay && !optionsScreen.forcePreviewOverlay()) {
				return false;
			}
			Window window = mc.getWindow();
			double x = mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
			double y = mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
			if (rect.expectedRect.contains((int) x, (int) y)) {
				return false;
			}
		}

		if (mc.gui.getDebugOverlay().showDebugScreen() && general.shouldHideFromDebug())
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
		BoxElement root = WailaTickHandler.instance().rootElement;
		float delta = Minecraft.getInstance().getDeltaFrameTime();
		ConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (root != null) {
			lingerTooltip = root;
		}
		if (root == null && lingerTooltip != null) {
			disappearTicks += delta;
			if (disappearTicks < overlay.getDisappearingDelay()) {
				root = lingerTooltip;
				show = true;
			}
		} else {
			disappearTicks = 0;
		}
		if (overlay.getAnimation() && lingerTooltip != null) {
			root = lingerTooltip;
			float speed = general.isDebug() ? 0.1F : 0.6F;
			alpha += (show ? speed : -speed) * delta;
			alpha = Mth.clamp(alpha, 0, 1);
		} else {
			alpha = show ? 1 : 0;
		}

		if (alpha < 0.1F || root == null || !shouldShowImmediately(root)) {
			lingerTooltip = null;
			rect.rect.setWidth(0); // mark dirty
			WailaTickHandler.clearLastNarration();
			return;
		}

		ticks += delta;
		Minecraft.getInstance().getProfiler().push("Jade Overlay");
		renderOverlay(root, guiGraphics);
		Minecraft.getInstance().getProfiler().pop();
	}

	public static void renderOverlay(BoxElement root, GuiGraphics guiGraphics) {
		root.updateRect(rect);

		for (JadeBeforeRenderCallback callback : WailaClientRegistration.instance().beforeRenderCallback.callbacks()) {
			if (callback.beforeRender(root, rect, guiGraphics, ObjectDataCenter.get())) {
				return;
			}
		}

		PoseStack matrixStack = guiGraphics.pose();
		matrixStack.pushPose();
		float z = Minecraft.getInstance().screen == null ? 1 : 100;
		matrixStack.translate(rect.rect.getX(), rect.rect.getY(), z);

		float scale = rect.scale;
		if (scale != 1) {
			matrixStack.scale(scale, scale, 1.0F);
		}

		RenderSystem.enableBlend();
		{
			float maxWidth = rect.rect.getWidth();
			float maxHeight = rect.rect.getHeight();
			if (root.getStyle().hasRoundCorner()) {
				maxWidth -= 2;
				maxHeight -= 2;
			}
			maxWidth = maxWidth / scale;
			maxHeight = maxHeight / scale;
			root.render(guiGraphics, 0, 0, maxWidth, maxHeight);
		}

		WailaClientRegistration.instance().afterRenderCallback.call(callback -> {
			callback.afterRender(root, rect, guiGraphics, ObjectDataCenter.get());
		});

		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		matrixStack.popPose();

		if (Jade.CONFIG.get().getGeneral().shouldEnableTextToSpeech()) {
			WailaTickHandler.narrate(root.getTooltip(), true);
		}

		shown = true;
	}
}
