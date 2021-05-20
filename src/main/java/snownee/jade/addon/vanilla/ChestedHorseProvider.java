package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
		AbstractChestedHorseEntity horse = (AbstractChestedHorseEntity) accessor.getEntity();
		if (horse instanceof LlamaEntity) {
			tooltip.add(new TranslationTextComponent("jade.llamaStrength", TextFormatting.WHITE.toString() + ((LlamaEntity) horse).getStrength()));
		}
		if (horse.hasChest()) {
			InventoryProvider.append(tooltip, accessor);
		}
	}

	@Override
	public void appendServerData(CompoundNBT data, ServerPlayerEntity player, World world, Entity t) {
		int size = player.isCrouching() ? JadeCommonConfig.inventorySneakShowAmount : JadeCommonConfig.inventoryNormalShowAmount;
		if (size == 0) {
			return;
		}

		AbstractChestedHorseEntity horse = (AbstractChestedHorseEntity) t;
		if (horse.hasChest()) {
			horse.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> InventoryProvider.putInvData(data, h, size, 2));
		}
	}
}
