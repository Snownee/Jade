package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.NarratableComponent;
import snownee.jade.api.ui.ResizeableElement;
import snownee.jade.api.view.ProgressView;
import snownee.jade.gui.ResizeableLayout;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.track.ProgressTrackInfo;

public class ProgressElement extends ResizeableElement implements StyledElement {
	private final ProgressView view;
	private ProgressTrackInfo track;

	public ProgressElement(ProgressView view) {
		this.view = view;
		width = 100;
		height = 8;
		if (view.text != null) {
			width = Math.max(width, DisplayHelper.font().width(view.text) + 10);
			height = 14;
		}
		if (view.style.direction().isHorizontal() && view.style.fitContentX()) {
			flexGrow(1);
		} else if (view.style.direction().isVertical() && view.style.fitContentY()) {
			flexGrow(1);
		}
	}

	public ProgressElement(ProgressView view, int width, int height) {
		this.view = view;
		this.width = width;
		this.height = height;
	}

//	@Override
//	public Vec2 getSize() {
//		int height = text == null ? 8 : 14;
//		float width = 0;
//		width += boxStyle.borderWidth() * 2;
//		if (text != null) {
//			width += DisplayHelper.font().width(text) + 3;
//		}
//		float finalWidth = width = Math.max(20, width);
//		if (getTag() != null) {
//			track = WailaTickHandler.instance().progressTracker.getOrCreate(
//					getTag(), ProgressTrackInfo.class, () -> {
//						return new ProgressTrackInfo(canDecrease, this.progress, finalWidth);
//					});
//			track.setExpectedWidth(width);
//			width = track.getWidth();
//		}
//		return new Vec2(width, height);
//	}
//
//	@Override
//	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
//		float width = style.direction().isHorizontal() && style.fitContentX() ? maxX - x : getCachedSize().x;
//		float height = style.direction().isVertical() && style.fitContentY() ? maxY - y : getCachedSize().y;
//		x = style.direction().isHorizontal() ? x : x + (maxX - x - width) / 2;
//		y = style.direction().isVertical() ? y : y + (maxY - y - height) / 2;
//		boxStyle.render(guiGraphics, this, x, y, width, height, IDisplayHelper.get().opacity());
//		float progress = this.progress;
//		if (track == null && getTag() != null) {
//			track = WailaTickHandler.instance().progressTracker.getOrCreate(
//					getTag(), ProgressTrackInfo.class, () -> {
//						return new ProgressTrackInfo(canDecrease, this.progress, width);
//					});
//		}
//		if (track != null) {
//			track.setProgress(progress);
//			track.update(Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
//			progress = track.getSmoothProgress();
//		}
//		float b = boxStyle.borderWidth();
//		style.render(guiGraphics, x + b, y + b, width - b * 2, height - b * 2, progress, text);
//	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		view.boxStyle.render(graphics, this, getX(), getY(), width, height, IDisplayHelper.get().opacity());

		int borderWidth = view.boxStyle.borderWidth();
		int freeX = getX() + borderWidth;
		int freeY = getY() + borderWidth;
		int freeWidth = width - borderWidth * 2;
		int freeHeight = height - borderWidth * 2;
		float progress = 0;
		float start = 0;
		for (int i = 0; i < view.parts.size(); i++) {
			ProgressView.Part part = view.parts.get(i);
			if (part.progress() <= 0F) {
				continue;
			}
			progress = Math.min(progress + part.progress(), 1F);
			start = renderPart(graphics, partialTicks, part, start, freeX, freeY, freeWidth, freeHeight, i == view.parts.size() - 1);
			if (progress == 1F) {
				break;
			}
		}
		if (progress > 0 && view.style.foreground() != null) {
			DisplayHelper.INSTANCE.blitSprite(
					graphics,
					RenderPipelines.GUI_TEXTURED,
					view.style.foreground(),
					freeX,
					freeY,
					(int) (freeWidth - start),
					freeHeight);
		}

		if (view.text != null) {
			IDisplayHelper.get().drawText(graphics, view.text, getX() + 4, getY() + 3, IThemeHelper.get().getNormalColor());
		}
	}

	private float renderPart(
			GuiGraphics graphics,
			float partialTicks,
			ProgressView.Part part,
			float start,
			int x,
			int y,
			int width,
			int height,
			boolean isLast) {
		float partWidth = Math.min(part.progress() * width, width - start);
		int roundedPartWidth = Mth.ceil(partWidth);
		if (part.overlay() == null) {
			return start + partWidth;//TODO
		}
//		graphics.enableScissor(x + (int) start, y, x + (int) start + roundedPartWidth, y + height);
		// we can only draw a sprite from its top-left corner, so only makes the last part more detailed
		if (isLast && view.style.foreground() == null && part.overlay() instanceof ProgressOverlayElement element &&
				element.canUseFloatingRect()) {
			element.setFloatingRect(x + start, y, partWidth, height);
			element.render(graphics, -1, -1, partialTicks);
			element.setFloatingRect(null);
//			graphics.disableScissor();
			return start + partWidth;
		} else {
			resizeElement(part.overlay(), x + (int) start, y, roundedPartWidth, height);
			part.overlay().render(graphics, -1, -1, partialTicks);
//			graphics.disableScissor();
			return start + roundedPartWidth;
		}
	}

	private void resizeElement(Element element, int x, int y, int width, int height) {
		element.setX(x);
		element.setY(y);
		if (element instanceof ResizeableLayout resizeableLayout) {
			resizeableLayout.setFreeSpace(width, height);
		}
	}

	@Override
	public @Nullable Component getNarration() {
		return view.text == null ? null : NarratableComponent.getNarration(view.text);
	}

	@Override
	public Element getIcon() {
		return null;
	}

	@Override
	public BoxStyle getStyle() {
		return view.boxStyle;
	}

	@Override
	public void setFreeSpace(int width, int height) {
		if (view.style.fitContentX()) {
			this.width = width;
		}
		if (view.style.fitContentY()) {
			this.height = height;
		}
	}
}
