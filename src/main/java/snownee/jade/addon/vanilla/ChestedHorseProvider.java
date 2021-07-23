package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;
import snownee.jade.JadeCommonConfig;
import snownee.jade.VanillaPlugin;
import snownee.jade.addon.forge.InventoryProvider;

public class ChestedHorseProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {
	public static final ChestedHorseProvider INSTANCE = new ChestedHorseProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.HORSE_INVENTORY)) {
			return;
		}
		AbstractChestedHorse horse = (AbstractChestedHorse) accessor.getEntity();
		if (horse instanceof Llama) {
			tooltip.add(new TranslatableComponent("jade.llamaStrength", ((Llama) horse).getStrength()));
		}
		if (horse.hasChest()) {
			InventoryProvider.append(tooltip, accessor);
		}
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, Entity t, boolean showDetails) {
		int size = showDetails ? JadeCommonConfig.inventoryDetailedShowAmount : JadeCommonConfig.inventoryNormalShowAmount;
		if (size == 0) {
			return;
		}

		AbstractChestedHorse horse = (AbstractChestedHorse) t;
		if (horse.hasChest()) {
			horse.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> InventoryProvider.putInvData(data, h, size, 2));
		}
	}
}
