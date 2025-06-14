package snownee.jade.overlay;

import org.joml.Matrix3x2fStack;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.JadeIds;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.Rect2f;
import snownee.jade.api.ui.TooltipAnimation;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.WailaConfig.General;
import snownee.jade.impl.ui.BoxElementImpl;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.ModIdentification;

public class OverlayRenderer {

	public static final TooltipAnimation animation = new TooltipAnimation();
	public static float ticks;
	public static boolean shown;
	private static BoxElementImpl lingerTooltip;
	private static float disappearTicks;

	public static boolean shouldShow() {
		if (JadeClient.tickHandler().rootElement == null) {
			return false;
		}

		IWailaConfig.General general = IWailaConfig.get().general();
		if (!general.shouldDisplayTooltip()) {
			return false;
		}

		if (general.getDisplayMode() == IWailaConfig.DisplayMode.HOLD_KEY && !JadeClient.showOverlay.isDown()) {
			return false;
		}

		BossBarOverlapMode mode = general.getBossBarOverlapMode();
		if (mode == BossBarOverlapMode.HIDE_TOOLTIP && !(Minecraft.getInstance().screen instanceof BaseOptionsScreen) &&
				ClientProxy.getBossBarRect() != null) {
			return false;
		}

		return true;
	}

	public static boolean shouldShowImmediately(BoxElementImpl box) {
		if (box.getTooltip().isEmpty()) {
			return false;
		}

		Minecraft mc = Minecraft.getInstance();

		if (!ClientProxy.shouldShowWithGui(mc, mc.screen)) {
			return false;
		}

		box.updateExpectedRect(animation);
		if (mc.screen instanceof PreviewOptionsScreen optionsScreen) {
			if (optionsScreen.forcePreviewOverlay()) {
				return true;
			}
			if (!Jade.history().previewOverlay) {
				return false;
			}
			Window window = mc.getWindow();
			double x = mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
			double y = mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
			if (animation.expectedRect.contains((int) x, (int) y)) {
				return false;
			}
		}

		General general = Jade.config().general();
		if (mc.getOverlay() != null || mc.options.hideGui) {
			return false;
		}

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
	public static void renderOverlay478757(GuiGraphics graphics, float delta) {
		ticks += delta;
		shown = false;
		BoxElementImpl root = JadeClient.tickHandler().rootElement;
		boolean show;
		if (root == null && PreviewOptionsScreen.isAdjustingPosition()) {
			Tooltip tooltip = new Tooltip();
			tooltip.add(IThemeHelper.get().title(Blocks.GRASS_BLOCK.getName()));
			tooltip.add(IThemeHelper.get().modName(ModIdentification.getModName(Blocks.GRASS_BLOCK)));
			Theme theme = IThemeHelper.get().theme();
			tooltip.setIcon(theme.modifyIcon(JadeUI.item(new ItemStack(Blocks.GRASS_BLOCK))));
			root = new BoxElementImpl(tooltip, theme.tooltipStyle);
			root.tag(JadeIds.ROOT);
			root.updateExpectedRect(animation);
			show = true;
		} else {
			show = shouldShow();
		}
		IWailaConfig.Overlay overlay = IWailaConfig.get().overlay();
		IWailaConfig.General general = IWailaConfig.get().general();
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
			animation.showHideAlpha += (show ? speed : -speed) * delta;
			animation.showHideAlpha = Mth.clamp(animation.showHideAlpha, 0, 1);
		} else {
			animation.showHideAlpha = show ? 1 : 0;
		}

		if (root == null) {
			return;
		}

		if (animation.showHideAlpha < 0.1F || !shouldShowImmediately(root)) {
			if (!PreviewOptionsScreen.isAdjustingPosition()) {
				lingerTooltip = null;
				animation.rect.setWidth(0); // mark dirty
				JadeClient.tickHandler().clearLastNarration();
				return;
			}
		}

		Profiler.get().push("Jade Overlay");
		renderOverlay(root, graphics, -1, -1, delta); //TODO pass correct mouseX, mouseY
		Profiler.get().pop();
	}

	public static void renderOverlay(BoxElementImpl root, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		root.updateRect(animation);

		for (JadeBeforeRenderCallback callback : WailaClientRegistration.instance().beforeRenderCallback.callbacks()) {
			if (callback.beforeRender(root, animation, graphics, ObjectDataCenter.get())) {
				return;
			}
		}

		Matrix3x2fStack matrixStack = graphics.pose();
		matrixStack.pushMatrix();
		Rect2f rect = animation.rect;
		matrixStack.translate(rect.getX(), rect.getY());

		float scale = animation.scale;
		if (scale != 1f) {
			matrixStack.scale(scale);
		}

		root.setWidgetAlpha(animation.alpha);
		root.render(graphics, -1, -1, partialTicks);
		if (IWailaConfig.get().general().isDebug() && Screen.hasControlDown()) {
			root.renderDebug(graphics, mouseX, mouseY, partialTicks, new Element.RenderDebugContext(root, rect));
		}

		WailaClientRegistration.instance().afterRenderCallback.call(callback -> {
			callback.afterRender(root, animation, graphics, ObjectDataCenter.get());
		});

		matrixStack.popMatrix();

		if (IWailaConfig.get().accessibility().shouldEnableTextToSpeech()) {
			JadeClient.tickHandler().narrate(root, true);
		}

		shown = true;
	}

	public static void clearState() {
		lingerTooltip = null;
		JadeClient.tickHandler().clearLastNarration();
	}
}
