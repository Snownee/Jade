package mcp.mobius.waila.addons.minecraft;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.item.ItemStack;
import snownee.jade.addon.vanilla.MiscEntityNameProvider;

public class HUDHandlerEntityIcon implements IEntityComponentProvider {

	public static final IEntityComponentProvider INSTANCE = new HUDHandlerEntityIcon();

	@Override
	public ItemStack getDisplayItem(IEntityAccessor accessor, IPluginConfig config) {
		return MiscEntityNameProvider.INSTANCE.getDisplayItem(accessor, config);
	}
}
