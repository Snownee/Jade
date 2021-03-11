package mcp.mobius.waila.addons.core;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import snownee.jade.Jade;
import snownee.jade.JadePlugin;
import snownee.jade.Renderables;
import snownee.jade.addon.vanilla.MiscEntityNameProvider;

import java.util.List;

public class BaseEntityProvider implements IEntityComponentProvider {

    static final IEntityComponentProvider INSTANCE = new BaseEntityProvider();

    @Override
    public void appendHead(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
        ((ITaggableList<ResourceLocation, ITextComponent>) tooltip).setTag(BaseBlockProvider.OBJECT_NAME_TAG, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getEntityName(), accessor.getEntity().getDisplayName().getString())));
        if (config.get(CorePlugin.CONFIG_SHOW_REGISTRY))
            ((ITaggableList<ResourceLocation, ITextComponent>) tooltip).setTag(BaseBlockProvider.REGISTRY_NAME_TAG, new StringTextComponent(accessor.getEntity().getType().getRegistryName().toString()).mergeStyle(TextFormatting.GRAY));
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof LivingEntity))
            return;
        if (config.get(CorePlugin.CONFIG_SHOW_ENTITY_HEALTH))
            appendHealth((LivingEntity) accessor.getEntity(), tooltip);
        if (config.get(CorePlugin.CONFIG_SHOW_ENTITY_HEALTH))
            appendArmor((LivingEntity) accessor.getEntity(), tooltip);
    }

    @Override
    public void appendTail(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
        if (config.get(JadePlugin.HIDE_MOD_NAME) || accessor.getEntity() instanceof ItemEntity)
            return;
        tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModInfo(accessor.getEntity()).getName())));
    }

    private void appendHealth(LivingEntity living, List<ITextComponent> tooltip) {
        float health = living.getHealth();
        float maxHealth = living.getMaxHealth();

        if (living.getMaxHealth() > Waila.CONFIG.get().getGeneral().getMaxHealthForRender()) {
            CompoundNBT healthData = new CompoundNBT();
            healthData.putFloat("health", 1);
            healthData.putFloat("max", 1);
            RenderableTextComponent icon = new RenderableTextComponent(CorePlugin.RENDER_ENTITY_HEALTH, healthData);
            RenderableTextComponent text = Renderables.offsetText(String.format("%s/%s", Jade.dfCommas.format(health), Jade.dfCommas.format(maxHealth)), 5, 0);
            tooltip.add(Renderables.of(icon, text));
        } else {
            CompoundNBT healthData = new CompoundNBT();
            healthData.putFloat("health", health * 0.5F);
            healthData.putFloat("max", maxHealth * 0.5F);
            tooltip.add(new RenderableTextComponent(CorePlugin.RENDER_ENTITY_HEALTH, healthData));
        }
    }

    private void appendArmor(LivingEntity living, List<ITextComponent> tooltip) {
        float armor = living.getTotalArmorValue();
        if (armor == 0)
            return;
        if (armor > Waila.CONFIG.get().getGeneral().getMaxHealthForRender()) {
            CompoundNBT armorData = new CompoundNBT();
            armorData.putFloat("armor", -1);
            RenderableTextComponent icon = new RenderableTextComponent(CorePlugin.RENDER_ENTITY_ARMOR, armorData);
            RenderableTextComponent text = Renderables.offsetText(Jade.dfCommas.format(armor), 5, 0);
            tooltip.add(Renderables.of(icon, text));
        } else {
            CompoundNBT armorData = new CompoundNBT();
            armorData.putFloat("armor", armor * 0.5F);
            tooltip.add(new RenderableTextComponent(CorePlugin.RENDER_ENTITY_ARMOR, armorData));
        }
    }

    @Override
    public ItemStack getDisplayItem(IEntityAccessor accessor, IPluginConfig config) {
        return MiscEntityNameProvider.INSTANCE.getDisplayItem(accessor, config);
    }
}
