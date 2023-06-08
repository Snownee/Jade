package snownee.jade.api.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.ProgressTracker.TrackInfo;
import snownee.jade.overlay.WailaTickHandler;

public class BoxStyle implements IBoxStyle {

	public static final BoxStyle DEFAULT;

	static {
		DEFAULT = new BoxStyle();
		DEFAULT.borderColor = 0xFF808080;
		DEFAULT.borderWidth = 1;
	}

	public int bgColor = 0;
	public int borderColor = 0;
	public float borderWidth = 0;
	public boolean roundCorner;
	public int progressColor = 0;
	public float progress;
	private ResourceLocation tag;
	private Object track; //TODO: API

	@Override
	public float borderWidth() {
		return borderWidth;
	}

	@Override
	public void tag(ResourceLocation tag) {
		this.tag = tag;
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float w, float h) {
		if (bgColor != 0)
			DisplayHelper.fill(guiGraphics, x + borderWidth, y + borderWidth, x + w - borderWidth, y + h - borderWidth, bgColor);
		DisplayHelper.INSTANCE.drawBorder(guiGraphics, x, y, x + w, y + h, borderWidth, borderColor, !roundCorner);
		if (progressColor != 0) {
			float left = roundCorner ? x + borderWidth : x;
			float width = roundCorner ? w - borderWidth * 2 : w;
			float top = y + h - Math.max(borderWidth, 0.5F);
			float progress = this.progress;
			if (track == null && tag != null) {
				track = WailaTickHandler.instance().progressTracker.createInfo(tag, progress, false, 0);
			}
			if (track != null) {
				progress = ((TrackInfo) track).tick(Minecraft.getInstance().getDeltaFrameTime());
			}
			DisplayHelper.INSTANCE.drawGradientProgress(guiGraphics, left, top, width, y + h - top, progress, progressColor);
		}
	}

}
