package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IProgressStyle;
import snownee.jade.overlay.ProgressTracker.TrackInfo;
import snownee.jade.overlay.WailaTickHandler;

public class ProgressElement extends Element {
	private final float progress;
	@Nullable
	private final Component text;
	private final IProgressStyle style;
	private final BoxStyle boxStyle;
	private TrackInfo track;
	private boolean canDecrease;

	public ProgressElement(float progress, Component text, IProgressStyle style, BoxStyle boxStyle, boolean canDecrease) {
		this.progress = Mth.clamp(progress, 0, 1);
		this.text = text;
		this.style = style;
		if (boxStyle == BoxStyle.getDefault() && IThemeHelper.get().isLightColorScheme()) {
			var newStyle = BoxStyle.createGradientBorder();
			newStyle.bgColor = 0x44444444;
			boxStyle = newStyle;
		}
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
		width = Math.max(20, width);
		if (getTag() != null) {
			track = WailaTickHandler.instance().progressTracker.createInfo(getTag(), progress, canDecrease, width);
			width = track.getWidth();
		}
		return new Vec2(width, height);
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		Vec2 size = getCachedSize();
		float b = boxStyle.borderWidth();
		boxStyle.render(guiGraphics, x, y, maxX - x, size.y - 2);
		float progress = this.progress;
		if (track == null && getTag() != null) {
			track = WailaTickHandler.instance().progressTracker.createInfo(getTag(), progress, canDecrease, getSize().y);
		}
		if (track != null) {
			progress = track.tick(Minecraft.getInstance().getDeltaFrameTime());
		}
		style.render(guiGraphics, x + b, y + b, maxX - x - b * 2, size.y - b * 2 - 2, progress, text);
	}

	@Override
	public @Nullable String getMessage() {
		return text == null ? null : text.getString();
	}
}
