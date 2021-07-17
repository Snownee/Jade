package mcp.mobius.waila.api.ui;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;

/**
 * A general implementation of {@link IElement}
 *
 * @author Snownee
 */
public abstract class Element implements IElement {

	protected Align align = Align.LEFT;
	protected Vector2f translation = Vector2f.ZERO;
	protected ResourceLocation tag;
	protected Vector2f size;

	@Override
	public IElement size(Vector2f size) {
		this.size = size;
		return this;
	}

	@Override
	public Vector2f getCachedSize() {
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
	public IElement translate(Vector2f translation) {
		this.translation = translation;
		return this;
	}

	@Override
	public Vector2f getTranslation() {
		return translation;
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
