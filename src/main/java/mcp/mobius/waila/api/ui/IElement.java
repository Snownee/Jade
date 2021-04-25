package mcp.mobius.waila.api.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;

// TODO docs
public interface IElement {

	IElement size(Vector2f size);

	/**
     * @param data     The data supplied by the provider
     * @param accessor A global accessor for TileEntities and Entities
     * @return Dimension of the reserved area
     */
	Vector2f getSize();

	Vector2f getCachedSize();

	/**
     * Draw method for the renderer. The GL matrice is automatically moved to the top left of the reserved zone.<br>
     * All calls should be relative to (0,0)
     * @param maxWidth 
     * @param maxHeight 
     *
     * @param data     The data supplied by the provider
     * @param accessor A global accessor for TileEntities and Entities
     */
	void render(MatrixStack matrixStack, float x, float y, float maxX, float maxY);

	IElement align(Align align);

	Align getAlignment();

	IElement translate(Vector2f translation);

	Vector2f getTranslation();

	IElement tag(ResourceLocation tag);

	ResourceLocation getTag();

	public static enum Align {
		LEFT, RIGHT
	}
}
