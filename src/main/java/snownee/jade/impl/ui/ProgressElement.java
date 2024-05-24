package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.track.ProgressTrackInfo;
import snownee.jade.overlay.WailaTickHandler;

public class ProgressElement extends Element implements StyledElement {
	private final float progress;
	@Nullable
	private final Component text;
	private final ProgressStyle style;
	private final BoxStyle boxStyle;
	private ProgressTrackInfo track;
	private boolean canDecrease;

	public ProgressElement(float progress, Component text, ProgressStyle style, BoxStyle boxStyle, boolean canDecrease) {
		this.progress = Mth.clamp(progress, 0, 1);
		this.text = text;
		this.style = style;
		this.boxStyle = boxStyle;
		this.canDecrease = canDecrease;
	}

	@Override
	public Vec2 getSize() {
		int height = text == null ? 8 : 14;
		float width = 0;
		width += boxStyle.borderWidth() * 2;
		if (text != null) {
			Font font = Minecraft.getInstance().font;
			width += font.width(text) + 3;
		}
		float finalWidth = width = Math.max(20, width);
		if (getTag() != null) {
			track = WailaTickHandler.instance().progressTracker.getOrCreate(getTag(), ProgressTrackInfo.class, () -> {
				return new ProgressTrackInfo(canDecrease, this.progress, finalWidth);
			});
			width = track.getWidth();
		}
		return new Vec2(width, height);
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		float width = style.direction().isHorizontal() && style.fitContentX() ? maxX - x : getCachedSize().x;
		float height = style.direction().isVertical() && style.fitContentY() ? maxY - y : getCachedSize().y;
		x = style.direction().isHorizontal() ? x : x + (maxX - x - width) / 2;
		y = style.direction().isVertical() ? y : y + (maxY - y - height) / 2;
		boxStyle.render(guiGraphics, this, x, y, width, height, IDisplayHelper.get().opacity());
		float progress = this.progress;
		if (track == null && getTag() != null) {
			track = WailaTickHandler.instance().progressTracker.getOrCreate(getTag(), ProgressTrackInfo.class, () -> {
				return new ProgressTrackInfo(canDecrease, this.progress, width);
			});
		}
		if (track != null) {
			track.setProgress(progress);
			track.setExpectedWidth(width);
			track.update(Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());
			progress = track.getSmoothProgress();
		}
		float b = boxStyle.borderWidth();
		style.render(guiGraphics, x + b, y + b, width - b * 2, height - b * 2, progress, text);
	}

	@Override
	public @Nullable String getMessage() {
		return text == null ? null : text.getString();
	}

	@Override
	public IElement getIcon() {
		return null;
	}

	@Override
	public BoxStyle getStyle() {
		return boxStyle;
	}
}
