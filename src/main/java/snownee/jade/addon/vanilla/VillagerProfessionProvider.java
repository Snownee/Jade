package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.addons.core.HUDHandlerBlocks;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITaggableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import snownee.jade.JadePlugin;

public class VillagerProfessionProvider implements IEntityComponentProvider {

	public static final VillagerProfessionProvider INSTANCE = new VillagerProfessionProvider();
	private static final ITextComponent LEVEL_SEPARATOR = new StringTextComponent(" - ");

	@Override
	public void appendHead(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
		((ITaggableList<ResourceLocation, ITextComponent>) tooltip).setTag(HUDHandlerBlocks.OBJECT_NAME_TAG, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getEntityName(), getNameRaw(accessor.getEntity()).getString())));
	}

	private static ITextComponent getNameRaw(Entity entity) {
		ITextComponent itextcomponent = entity.getCustomName();
		return itextcomponent != null ? sanitizeName(itextcomponent) : entity.getType().getName();
	}

	private static ITextComponent sanitizeName(ITextComponent name) {
		IFormattableTextComponent iformattabletextcomponent = name.copyRaw().setStyle(name.getStyle().setClickEvent((ClickEvent) null));
		for (ITextComponent itextcomponent : name.getSiblings()) {
			iformattabletextcomponent.appendSibling(sanitizeName(itextcomponent));
		}
		return iformattabletextcomponent;
	}

	@Override
	public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.PROFESSION)) {
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
		IFormattableTextComponent component = new TranslationTextComponent(EntityType.VILLAGER.getTranslationKey() + '.' + (!"minecraft".equals(profName.getNamespace()) ? profName.getNamespace() + '.' : "") + profName.getPath());
		VillagerProfession profession = data.getProfession();
		if (profession != VillagerProfession.NONE && profession != VillagerProfession.NITWIT) {
			component.appendSibling(LEVEL_SEPARATOR).appendSibling(new TranslationTextComponent("merchant.level." + level));
		}
		tooltip.add(component);
	}

}
