package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;
import snownee.jade.JadeCommonConfig;
import snownee.jade.addon.forge.BlockInventoryProvider;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum ChestedHorseProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		AbstractChestedHorse horse = (AbstractChestedHorse) accessor.getEntity();
		if (horse instanceof Llama) {
			tooltip.add(new TranslatableComponent("jade.llamaStrength", ((Llama) horse).getStrength()));
		}
		if (horse.hasChest()) {
			BlockInventoryProvider.append(tooltip, accessor);
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
			horse.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> BlockInventoryProvider.putInvData(data, h, size, 2));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_CHESTED_HORSE;
	}
}
