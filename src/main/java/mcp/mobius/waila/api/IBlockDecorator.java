package mcp.mobius.waila.api;

import net.minecraft.item.ItemStack;

/**
 * Callback class interface used to provide Block/TileEntity decorator.<br>
 * All methods in this interface shouldn't to be called by the implementing mod. An instance of the class is to be
 * registered to Waila via the {@link IRegistrar} instance provided in the original registration callback method
 * (cf. {@link IRegistrar} documentation for more information).
 *
 * @author ProfMobius
 */
// TODO implement
public interface IBlockDecorator {

    /**
     * Callback for the decorator. It provides a standard GL stack positioned on the block.</br>
     * Will be used if the implementing class is registered via {@link IRegistrar#registerDecorator}.</br>
     *
     * @param itemStack Current block scanned, in ItemStack form.
     * @param accessor  Contains most of the relevant information about the current environment.
     * @param config    Current configuration of Waila.
     */
    void decorateBlock(ItemStack itemStack, IDataAccessor accessor, IPluginConfig config);

}
