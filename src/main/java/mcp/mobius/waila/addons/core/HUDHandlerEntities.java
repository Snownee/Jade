package mcp.mobius.waila.addons.core;

import java.util.List;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITaggableList;
import mcp.mobius.waila.api.RenderableTextComponent;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import snownee.jade.Jade;
import snownee.jade.JadePlugin;
import snownee.jade.Renderables;

public class HUDHandlerEntities implements IEntityComponentProvider {

	static final IEntityComponentProvider INSTANCE = new HUDHandlerEntities();

	@Override
	public void appendHead(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
		((ITaggableList<ResourceLocation, ITextComponent>) tooltip).setTag(HUDHandlerBlocks.OBJECT_NAME_TAG, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getEntityName(), getEntityName(accessor.getEntity()))));
		if (config.get(PluginCore.CONFIG_SHOW_REGISTRY))
			((ITaggableList<ResourceLocation, ITextComponent>) tooltip).setTag(HUDHandlerBlocks.REGISTRY_NAME_TAG, new StringTextComponent(accessor.getEntity().getType().getRegistryName().toString()).mergeStyle(TextFormatting.GRAY));
	}

	public static String getEntityName(Entity entity) {
		if (!entity.hasCustomName()) {
			if (entity instanceof ItemEntity) {
				return ((ItemEntity) entity).getItem().getDisplayName().getString();
			}
		}
		return entity.getName().getString();
	}

	@Override
	public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
		if (!(accessor.getEntity() instanceof LivingEntity))
			return;
		if (config.get(PluginCore.CONFIG_SHOW_ENTITY_ARMOR))
			appendArmor((LivingEntity) accessor.getEntity(), tooltip);
		if (config.get(PluginCore.CONFIG_SHOW_ENTITY_HEALTH))
			appendHealth((LivingEntity) accessor.getEntity(), tooltip);
	}

	@Override
	public void appendTail(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
		if (config.get(JadePlugin.HIDE_MOD_NAME))
			return;
		tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModName(accessor.getEntity()))));
	}

	private void appendHealth(LivingEntity living, List<ITextComponent> tooltip) {
		if (living instanceof ArmorStandEntity) {
			return;
		}

		float health = living.getHealth();
		float maxHealth = living.getMaxHealth();

		if (living.getMaxHealth() > Waila.CONFIG.get().getGeneral().getMaxHealthForRender()) {
			CompoundNBT healthData = new CompoundNBT();
			healthData.putFloat("health", 1);
			healthData.putFloat("max", 1);
			RenderableTextComponent icon = new RenderableTextComponent(PluginCore.RENDER_ENTITY_HEALTH, healthData);
			RenderableTextComponent text = Renderables.offsetText(String.format("%s/%s", Jade.dfCommas.format(health), Jade.dfCommas.format(maxHealth)), 5, 0);
			tooltip.add(0, Renderables.of(icon, text));
		} else {
			CompoundNBT healthData = new CompoundNBT();
			healthData.putFloat("health", health * 0.5F);
			healthData.putFloat("max", maxHealth * 0.5F);
			tooltip.add(0, new RenderableTextComponent(PluginCore.RENDER_ENTITY_HEALTH, healthData));
		}
	}

	private void appendArmor(LivingEntity living, List<ITextComponent> tooltip) {
		float armor = living.getTotalArmorValue();
		if (armor == 0)
			return;
		if (armor > Waila.CONFIG.get().getGeneral().getMaxHealthForRender()) {
			CompoundNBT armorData = new CompoundNBT();
			armorData.putFloat("armor", -1);
			RenderableTextComponent icon = new RenderableTextComponent(PluginCore.RENDER_ENTITY_ARMOR, armorData);
			RenderableTextComponent text = Renderables.offsetText(Jade.dfCommas.format(armor), 5, 0);
			tooltip.add(0, Renderables.of(icon, text));
		} else {
			CompoundNBT armorData = new CompoundNBT();
			armorData.putFloat("armor", armor * 0.5F);
			tooltip.add(0, new RenderableTextComponent(PluginCore.RENDER_ENTITY_ARMOR, armorData));
		}
	}
}
