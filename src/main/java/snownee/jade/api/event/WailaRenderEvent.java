package snownee.jade.api.event;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;

/**
 * The base event for rendering the Waila tooltip. This provides the opportunity to do last minute changes to the tooltip.
 * <p>
 * All sub-events are fired from {@link mcp.mobius.waila.overlay.OverlayRenderer#renderOverlay(mcp.mobius.waila.overlay.Tooltip)}.
 * All sub-events are fired every render tick.
 * <p>
 * {@link #position} The position and size of the tooltip being rendered
 */
public class WailaRenderEvent extends Event {

	private final ITooltip tooltip;
	private final Rect2i rect;
	private final PoseStack matrixStack;

	public WailaRenderEvent(ITooltip tooltip, Rect2i rect, PoseStack matrixStack) {
		this.tooltip = tooltip;
		this.rect = rect;
		this.matrixStack = matrixStack;
	}

	public ITooltip getTooltip() {
		return tooltip;
	}

	public Rect2i getRect() {
		return rect;
	}

	public PoseStack getPoseStack() {
		return matrixStack;
	}

	/**
	 * This event is fired just before the Waila tooltip is rendered and right after setting up the GL state in
	 * {@link mcp.mobius.waila.overlay.OverlayRenderer#renderOverlay(mcp.mobius.waila.overlay.Tooltip)}
	 * <p>
	 * This event is cancelable.
	 * If this event is canceled, the tooltip will not render.
	 */
	@Cancelable
	public static class Pre extends WailaRenderEvent {

		private final Accessor<?> accessor;

		public Pre(ITooltip tooltip, Accessor<?> accessor, Rect2i rect, PoseStack matrixStack) {
			super(tooltip, rect, matrixStack);

			this.accessor = accessor;
		}

		public Accessor<?> getAccessor() {
			return accessor;
		}
	}

	/**
     * This event is fired just after the tooltip is rendered and right before the GL state is reset in
     * {@link mcp.mobius.waila.overlay.OverlayRenderer#renderOverlay(mcp.mobius.waila.overlay.Tooltip)}
     * This event is only fired if {@link Pre} is not cancelled.
     * <p>
     * This event is not cancelable.
     */
	public static class Post extends WailaRenderEvent {

		public Post(ITooltip tooltip, Rect2i rect, PoseStack matrixStack) {
			super(tooltip, rect, matrixStack);
		}
	}

	public static class Color extends Event {
		private final float alpha;
		private int background;
		private int gradientStart;
		private int gradientEnd;
		private boolean reset;

		public Color(float alpha, int background, int gradientStart, int gradientEnd) {
			this.alpha = alpha;
			this.background = background;
			this.gradientStart = gradientStart;
			this.gradientEnd = gradientEnd;
		}

		public float getAlpha() {
			return alpha;
		}

		public int getBackground() {
			return background;
		}

		public void setBackground(int background) {
			this.background = background;
		}

		public int getGradientStart() {
			return gradientStart;
		}

		public void setGradientStart(int gradientStart) {
			this.gradientStart = gradientStart;
		}

		public int getGradientEnd() {
			return gradientEnd;
		}

		public void setGradientEnd(int gradientEnd) {
			this.gradientEnd = gradientEnd;
		}

		public boolean isReset() {
			return reset;
		}

		public void setReset(boolean reset) {
			this.reset = reset;
		}
	}
}
