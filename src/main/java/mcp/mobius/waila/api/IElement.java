package mcp.mobius.waila.api;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.ResourceLocation;

// TODO docs
public interface IElement {

    IElement size(Size size);

    /**
     * @param data     The data supplied by the provider
     * @param accessor A global accessor for TileEntities and Entities
     * @return Dimension of the reserved area
     */
    Size getSize();

    Size getCachedSize();

    /**
     * Draw method for the renderer. The GL matrice is automatically moved to the top left of the reserved zone.<br>
     * All calls should be relative to (0,0)
     * @param maxWidth 
     * @param maxHeight 
     *
     * @param data     The data supplied by the provider
     * @param accessor A global accessor for TileEntities and Entities
     */
    void render(MatrixStack matrixStack, int x, int y, int maxX, int maxY);

    IElement align(Align align);

    Align getAlignment();

    IElement translate(int x, int y);

    Size getTranslation();

    IElement tag(ResourceLocation tag);

    ResourceLocation getTag();

    public static enum Align {
        LEFT, RIGHT
    }
}
