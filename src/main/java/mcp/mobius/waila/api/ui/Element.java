package mcp.mobius.waila.api.ui;

import net.minecraft.util.ResourceLocation;

public abstract class Element implements IElement {

	protected Align align = Align.LEFT;
	protected Size translate = Size.ZERO;
	protected ResourceLocation tag;
	protected Size size;

	@Override
	public IElement size(Size size) {
		this.size = size;
		return this;
	}

	@Override
	public Size getCachedSize() {
		if (size == null)
			size = getSize();
		return size;
	}

	@Override
	public IElement align(Align align) {
		this.align = align;
		return this;
	}

	@Override
	public Align getAlignment() {
		return align;
	}

	@Override
	public IElement translate(int x, int y) {
		if (x == 0 && y == 0)
			this.translate = Size.ZERO;
		else
			this.translate = new Size(x, y);
		return this;
	}

	@Override
	public Size getTranslation() {
		return translate;
	}

	@Override
	public IElement tag(ResourceLocation tag) {
		this.tag = tag;
		return this;
	}

	@Override
	public ResourceLocation getTag() {
		return tag;
	}

}
