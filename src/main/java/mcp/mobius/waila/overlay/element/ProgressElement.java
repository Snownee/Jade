package mcp.mobius.waila.overlay.element;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.api.ui.IProgressStyle;
import mcp.mobius.waila.api.ui.Size;
import mcp.mobius.waila.impl.ui.BorderStyle;
import mcp.mobius.waila.impl.ui.ProgressStyle;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class ProgressElement extends Element {
	private final float progress;
	@Nullable
	private final ITextComponent text;
	private final IProgressStyle style;
	@Nullable
	private final BorderStyle borderStyle;

	public ProgressElement(float progress, ITextComponent text, ProgressStyle style, BorderStyle borderStyle) {
		this.progress = MathHelper.clamp(progress, 0, 1);
		this.text = text;
		this.style = style;
		this.borderStyle = borderStyle;
	}

	@Override
	public Size getSize() {
		int height = text == null ? 8 : 14;
		int minWidth = 0;
		if (borderStyle != null) {
			minWidth += borderStyle.width * 2;
		}
		if (text != null) {
			FontRenderer font = Minecraft.getInstance().fontRenderer;
			minWidth += font.getStringWidth(text.getString());
		}
		return new Size(Math.max(20, minWidth), height);
	}

	@Override
	public void render(MatrixStack matrixStack, int x, int y, int maxX, int maxY) {
		Size size = getCachedSize();
		if (borderStyle != null) {
			DisplayHelper.INSTANCE.drawBorder(matrixStack, x, y, maxX - 2, y + size.height - 2, borderStyle);
		}
		int b = borderStyle.width;
		float w = maxX - x - b * 2 - 2;
		w *= progress;
		style.render(matrixStack, x + b, y + b, w, size.height - b * 2 - 2, text);
	}

}
