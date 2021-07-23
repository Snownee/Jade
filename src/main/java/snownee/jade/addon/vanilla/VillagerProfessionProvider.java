package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import snownee.jade.VanillaPlugin;

public class VillagerProfessionProvider implements IEntityComponentProvider {

	public static final VillagerProfessionProvider INSTANCE = new VillagerProfessionProvider();
	private static final Component field_243352_C = new TextComponent(" - ");

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.PROFESSION)) {
			return;
		}
		VillagerData data = null;
		if (accessor.getEntity() instanceof Villager) {
			data = ((Villager) accessor.getEntity()).getVillagerData();
		} else if (accessor.getEntity() instanceof ZombieVillager) {
			data = ((ZombieVillager) accessor.getEntity()).getVillagerData();
		}
		if (data == null) {
			return;
		}
		int level = data.getLevel();
		ResourceLocation profName = data.getProfession().getRegistryName();
		tooltip.add(new TranslatableComponent(EntityType.VILLAGER.getDescriptionId() + '.' + (!"minecraft".equals(profName.getNamespace()) ? profName.getNamespace() + '.' : "") + profName.getPath()).append(field_243352_C).append(new TranslatableComponent("merchant.level." + level)));
	}

}
