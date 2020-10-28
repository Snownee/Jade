package mcp.mobius.waila.api;

import net.minecraft.nbt.CompoundNBT;

import java.awt.Dimension;

public interface ITooltipRenderer {
    /**
     * @param data     The data supplied by the provider
     * @param accessor A global accessor for TileEntities and Entities
     * @return Dimension of the reserved area
     */
    Dimension getSize(CompoundNBT data, ICommonAccessor accessor);

    /**
     * Draw method for the renderer. The GL matrice is automatically moved to the top left of the reserved zone.<br>
     * All calls should be relative to (0,0)
     *
     * @param data     The data supplied by the provider
     * @param accessor A global accessor for TileEntities and Entities
     */
    void draw( CompoundNBT data, ICommonAccessor accessor, int x, int y);
}
