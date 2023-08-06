package snownee.jade.impl.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.Jade;
import snownee.jade.api.ui.Element;

public class ProgressSpriteElement extends Element {

	public static final ResourceLocation PROGRESS = new ResourceLocation(Jade.MODID, "progress");
	public static final ResourceLocation PROGRESS_BASE = new ResourceLocation(Jade.MODID, "progress_base");
	private final ResourceLocation progressTexture;
	private final ResourceLocation progressBaseTexture;
	private final int width;
	private final int height;
	public float progress;

	public ProgressSpriteElement(float progress) {
		this(progress, PROGRESS, PROGRESS_BASE, 22, 16);
	}

	public ProgressSpriteElement(float progress, ResourceLocation progressTexture, ResourceLocation progressBaseTexture, int width, int height) {
		this.progress = progress;
		this.progressTexture = progressTexture;
		this.progressBaseTexture = progressBaseTexture;
		this.width = width;
		this.height = height;
	}

	@Override
	public Vec2 getSize() {
		return new Vec2(width + 4, height);
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		// Draws the "empty" background arrow
		guiGraphics.blitSprite(progressBaseTexture, (int) (x + 2), (int) y, width, height);

		if (progress > 0) {
			int progress = (int) (this.progress * width);
			// Draws the "full" foreground arrow based on the progress
			guiGraphics.blitSprite(progressTexture, width, height, 0, 0, (int) (x + 2), (int) y, progress + 1, height);
		}
	}

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}
}
