package snownee.jade.impl.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;

public class SpriteElement extends Element {

	private final ResourceLocation sprite;
	private final int width;
	private final int height;

	public SpriteElement(ResourceLocation sprite, int width, int height) {
		this.sprite = sprite;
		this.width = width;
		this.height = height;
	}

	@Override
	public Vec2 getSize() {
		return new Vec2(width, height);
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		IDisplayHelper.get().blitSprite(guiGraphics, sprite, width, height, 0, 0, Math.round(x), Math.round(y), Math.round(getCachedSize().x), Math.round(getCachedSize().y));
	}

}
