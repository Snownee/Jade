package mcp.mobius.waila.impl.ui;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.overlay.ProgressTracker.TrackInfo;
import mcp.mobius.waila.overlay.WailaTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;

public class ProgressElement extends Element {
	private final float progress;
	@Nullable
	private final ITextComponent text;
	private final ProgressStyle style;
	@Nullable
	private final BorderStyle borderStyle;
	private TrackInfo track;

	public ProgressElement(float progress, ITextComponent text, ProgressStyle style, BorderStyle borderStyle) {
		this.progress = MathHelper.clamp(progress, 0, 1);
		this.text = text;
		this.style = style;
		this.borderStyle = borderStyle;
	}

	@Override
	public Vector2f getSize() {
		int height = text == null ? 8 : 14;
		float width = 0;
		if (borderStyle != null) {
			width += borderStyle.width * 2;
		}
		if (text != null) {
			FontRenderer font = Minecraft.getInstance().fontRenderer;
			width += font.getStringWidth(text.getString());
		}
		width = Math.max(20, width);
		if (getTag() != null) {
			track = WailaTickHandler.instance().progressTracker.createInfo(getTag(), progress, width);
			width = track.getWidth();
		}
		return new Vector2f(width, height);
	}

	@Override
	public void render(MatrixStack matrixStack, float x, float y, float maxX, float maxY) {
		Vector2f size = getCachedSize();
		if (borderStyle != null) {
			DisplayHelper.INSTANCE.drawBorder(matrixStack, x, y, maxX - 2, y + size.y - 2, borderStyle);
		}
		int b = borderStyle.width;
		float progress = this.progress;
		if (track == null && getTag() != null) {
			track = WailaTickHandler.instance().progressTracker.createInfo(getTag(), progress, getSize().y);
		}
		if (track != null) {
			progress = track.tick(Minecraft.getInstance().getTickLength());
		}
		style.render(matrixStack, x + b, y + b, maxX - x - b * 2 - 2, size.y - b * 2 - 2, progress, text);
	}

}
