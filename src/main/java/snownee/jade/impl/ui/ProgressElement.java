package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.ResizeableElement;
import snownee.jade.api.view.ProgressView;
import snownee.jade.track.ProgressTrackInfo;

public class ProgressElement extends ResizeableElement implements StyledElement {
	private final ProgressView view;
	private ProgressTrackInfo track;

	public ProgressElement(ProgressView view) {
		this.view = view;
		height = view.text == null ? 8 : 14;
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

	}

	@Override
	public @Nullable Component getNarration() {
		return view.text;
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

	}
}
