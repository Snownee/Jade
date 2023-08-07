package snownee.jade.api.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.ProgressTracker.TrackInfo;
import snownee.jade.overlay.WailaTickHandler;

public abstract class BoxStyle {

	private static BoxStyle DEFAULT;
	public int progressColor = 0;
	public float progress;
	protected ResourceLocation tag;
	protected Object track; //TODO: API

	public static BoxStyle getDefault() {
		if (DEFAULT == null) {
			DEFAULT = new GradientBorder();
			((GradientBorder) DEFAULT).borderWidth = 1;
		}
		((GradientBorder) DEFAULT).borderColor = IThemeHelper.get().theme().boxBorderColor;
		return DEFAULT;
	}

	public static GradientBorder createGradientBorder() {
		GradientBorder style = new GradientBorder();
		style.borderColor = IThemeHelper.get().theme().boxBorderColor;
		return style;
	}

	public void tag(ResourceLocation tag) {
		this.tag = tag;
	}

	public abstract void render(GuiGraphics guiGraphics, float x, float y, float w, float h);

	public abstract float borderWidth();

	public static class GradientBorder extends BoxStyle {
		public int bgColor = 0;
		public int borderColor;
		public float borderWidth;
		public boolean roundCorner;

		private GradientBorder() {
		}

		@Override
		public float borderWidth() {
			return borderWidth;
		}

		@Override
		public void render(GuiGraphics guiGraphics, float x, float y, float w, float h) {
			if (bgColor != 0)
				DisplayHelper.fill(guiGraphics, x + borderWidth, y + borderWidth, x + w - borderWidth, y + h - borderWidth, bgColor);
			IDisplayHelper.get().drawBorder(guiGraphics, x, y, x + w, y + h, borderWidth, borderColor, !roundCorner);
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
				((DisplayHelper) IDisplayHelper.get()).drawGradientProgress(guiGraphics, left, top, width, y + h - top, progress, progressColor);
			}
		}
	}

}
