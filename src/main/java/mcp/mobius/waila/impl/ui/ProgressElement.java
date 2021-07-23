package mcp.mobius.waila.impl.ui;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.overlay.ProgressTracker.TrackInfo;
import mcp.mobius.waila.overlay.WailaTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class ProgressElement extends Element {
	private final float progress;
	@Nullable
	private final Component text;
	private final ProgressStyle style;
	@Nullable
	private final BorderStyle borderStyle;
	private TrackInfo track;

	public ProgressElement(float progress, Component text, ProgressStyle style, BorderStyle borderStyle) {
		this.progress = Mth.clamp(progress, 0, 1);
		this.text = text;
		this.style = style;
		this.borderStyle = borderStyle;
	}

	@Override
	public Vec2 getSize() {
		int height = text == null ? 8 : 14;
		float width = 0;
		if (borderStyle != null) {
			width += borderStyle.width * 2;
		}
		if (text != null) {
			Font font = Minecraft.getInstance().font;
			width += font.width(text.getString());
		}
		width = Math.max(20, width);
		if (getTag() != null) {
			track = WailaTickHandler.instance().progressTracker.createInfo(getTag(), progress, width);
			width = track.getWidth();
		}
		return new Vec2(width, height);
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		Vec2 size = getCachedSize();
		if (borderStyle != null) {
			DisplayHelper.INSTANCE.drawBorder(matrixStack, x, y, maxX - 2, y + size.y - 2, borderStyle);
		}
		int b = borderStyle.width;
		float progress = this.progress;
		if (track == null && getTag() != null) {
			track = WailaTickHandler.instance().progressTracker.createInfo(getTag(), progress, getSize().y);
		}
		if (track != null) {
			progress = track.tick(Minecraft.getInstance().getDeltaFrameTime());
		}
		style.render(matrixStack, x + b, y + b, maxX - x - b * 2 - 2, size.y - b * 2 - 2, progress, text);
	}

}
