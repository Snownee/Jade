package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.jade.VanillaPlugin;

public class VillagerProfessionProvider implements IEntityComponentProvider {

	public static final VillagerProfessionProvider INSTANCE = new VillagerProfessionProvider();
	private static final ITextComponent field_243352_C = new StringTextComponent(" - ");

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.PROFESSION)) {
			return;
		}
		VillagerData data = null;
		if (accessor.getEntity() instanceof VillagerEntity) {
			data = ((VillagerEntity) accessor.getEntity()).getVillagerData();
		} else if (accessor.getEntity() instanceof ZombieVillagerEntity) {
			data = ((ZombieVillagerEntity) accessor.getEntity()).getVillagerData();
		}
		if (data == null) {
			return;
		}
		int level = data.getLevel();
		ResourceLocation profName = data.getProfession().getRegistryName();
		tooltip.add(new TranslationTextComponent(EntityType.VILLAGER.getTranslationKey() + '.' + (!"minecraft".equals(profName.getNamespace()) ? profName.getNamespace() + '.' : "") + profName.getPath()).appendSibling(field_243352_C).appendSibling(new TranslationTextComponent("merchant.level." + level)));
	}

}
