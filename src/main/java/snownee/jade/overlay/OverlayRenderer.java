package snownee.jade.overlay;

import org.joml.Matrix3x2fStack;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.JadeInternals;
import snownee.jade.api.JadeIds;
import snownee.jade.api.JadeKeys;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.api.gaze.GazePoint;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.Rect2f;
import snownee.jade.api.ui.TooltipAnimation;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.WailaConfig.General;
import snownee.jade.impl.ui.BoxElementImpl;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.JadeGuiGraphics;
import snownee.jade.util.ModIdentification;

public class OverlayRenderer {

	public static final TooltipAnimation animation = new TooltipAnimation();
	public static float ticks;
	public static boolean shown;
	private static @Nullable BoxElementImpl lingerTooltip;
	private static float disappearTicks;

	public static boolean shouldShow() {
		if (JadeClient.tickHandler().rootElement == null) {
			return false;
		}

		IWailaConfig.General general = IWailaConfig.get().general();
		if (!general.shouldDisplayTooltip()) {
			return false;
		}

		if (general.getDisplayMode() == IWailaConfig.DisplayMode.HOLD_KEY && !JadeKeys.showOverlay().isDown()) {
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
			double x = mc.mouseHandler.getScaledXPos(window);
			double y = mc.mouseHandler.getScaledYPos(window);
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
				return;
			}
		}

		int mouseX = -1;
		int mouseY = -1;
		Minecraft mc = Minecraft.getInstance();
		if (JadeUI.isPinned()) {
			Window window = mc.getWindow();
			mouseX = (int) mc.mouseHandler.getScaledXPos(window);
			mouseY = (int) mc.mouseHandler.getScaledYPos(window);
		}

		Profiler.get().push("Jade Overlay");
		renderOverlay(root, graphics, mouseX, mouseY, delta); //TODO pass correct mouseX, mouseY
		Profiler.get().pop();
	}

	public static void renderOverlay(BoxElementImpl root, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		root.updateRect(animation);

		WailaTickHandler tickHandler = JadeClient.tickHandler();
		if (tickHandler.state != null) {
			for (JadeBeforeRenderCallback callback : WailaClientRegistration.instance().beforeRenderCallback.callbacks()) {
				if (callback.beforeRender(root, animation, graphics, tickHandler.state.accessor())) {
					return;
				}
			}
		}

		boolean renderDebug = IWailaConfig.get().general().isDebug() && JadeUI.hasControlDown();
		if (renderDebug) {
			Rect2f bossBarRect = ClientProxy.getBossBarRect();
			if (bossBarRect != null) {
				JadeInternals.getDisplayHelper().drawBorder(graphics, bossBarRect, 2, 0x88FF00FF, true);
			}
		}

		GazePoint gazePoint = WailaClientRegistration.instance().gazePoint;
		if (gazePoint.canUse()) {
			Vector2fc gazePos = gazePoint.pos();
			if (gazePos != null) {
//				JadeInternals.getDisplayHelper().drawBorder(
//						graphics,
//						new Rect2f(gazePos.x() - 10, gazePos.y() - 10, 20, 20),
//						2,
//						0x88FF0000,
//						true);

				Rect2f inflated = animation.rect.copy().inflate(50);
				if (!inflated.contains(gazePos.x(), gazePos.y())) {
					return;
				}
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

		Vector2i mouse = new Vector2i(mouseX, mouseY);
		if (mouseX != -1) {
			animation.mapMousePosition(mouseX, mouseY, (x, y) -> mouse.set(x.intValue(), y.intValue()));
		}

		root.setWidgetAlpha(animation.alpha);
		((JadeGuiGraphics) graphics).jade$setIgnoreScissorTest(true);
		graphics.deferredTooltip = null;
		root.render(graphics, mouse.x, mouse.y, partialTicks);
		((JadeGuiGraphics) graphics).jade$setIgnoreScissorTest(false);
		if (renderDebug) {
			root.renderDebug(graphics, mouse.x, mouse.y, partialTicks, new Element.RenderDebugContext(root, rect, true));
		} else if (JadeUI.isPinned() && JadeUI.hasControlDown()) {
			if (root.getChildAt(mouse.x, mouse.y).orElse(root) instanceof Element element) {
				element.renderDebug(graphics, mouse.x, mouse.y, partialTicks, new Element.RenderDebugContext(root, rect, false));
			}
		}

		if (tickHandler.state != null) {
			WailaClientRegistration.instance().afterRenderCallback.call(callback -> {
				callback.afterRender(root, animation, graphics, tickHandler.state.accessor());
			});
		}

		matrixStack.popMatrix();
		graphics.renderDeferredElements();

		if (IWailaConfig.get().accessibility().shouldEnableTextToSpeech()) {
			tickHandler.narrate(root, true);
		}

		shown = true;
	}

	public static void clearLingerTooltip() {
		lingerTooltip = null;
	}
}
