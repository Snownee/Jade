package mcp.mobius.waila.api.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;

/**
 * A basic renderable element to be drawn in Jade's tooltip.
 *
 * @author Snownee
 */
public interface IElement {

	/**
	 * Force a size that this element reserve
	 */
	IElement size(Vector2f size);

	/**
	 * Calculate the default reserved area of this element.
	 *
	 * Modders call getCachedSize instead
	 */
	Vector2f getSize();

	Vector2f getCachedSize();

	/**
	 * Draw method for the renderer.
	 *
	 * @param matrixStack
	 * @param x
	 * @param y
	 * @param maxX Max width this element can expand to
	 * @param maxY Max height this element can expand to
	 */
	void render(MatrixStack matrixStack, float x, float y, float maxX, float maxY);

	IElement align(Align align);

	Align getAlignment();

	/**
	 * Reposition this element with an offset
	 */
	IElement translate(Vector2f translation);

	Vector2f getTranslation();

	/**
	 * Tag this element for identify by other component providers or
	 * adding transition animation if this element is a progress bar
	 */
	IElement tag(ResourceLocation tag);

	ResourceLocation getTag();

	public enum Align {
		LEFT, RIGHT
	}
}
