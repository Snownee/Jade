package mcp.mobius.waila.addons.core;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.overlay.element.ArmorElement;
import mcp.mobius.waila.overlay.element.HealthElement;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import snownee.jade.Jade;

public class BaseEntityProvider implements IEntityComponentProvider {

    static final IEntityComponentProvider INSTANCE = new BaseEntityProvider();

    @Override
    public void append(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        TooltipPosition position = accessor.getTooltipPosition();
        if (position == TooltipPosition.HEAD) {
            appendHead(tooltip, accessor, config);
        } else if (position == TooltipPosition.BODY) {
            appendBody(tooltip, accessor, config);
        } else if (position == TooltipPosition.TAIL) {
            appendTail(tooltip, accessor, config);
        }
    }

    public void appendHead(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        String name = accessor.getEntity().getDisplayName().getString();
        tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getEntityName(), name)), CorePlugin.TAG_OBJECT_NAME);
        if (config.get(CorePlugin.CONFIG_REGISTRY_NAME))
            tooltip.add(new StringTextComponent(accessor.getEntity().getType().getRegistryName().toString()).mergeStyle(TextFormatting.GRAY), CorePlugin.TAG_REGISTRY_NAME);
    }

    public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof LivingEntity))
            return;
        if (config.get(CorePlugin.CONFIG_ENTITY_HEALTH))
            appendHealth((LivingEntity) accessor.getEntity(), tooltip);
        if (config.get(CorePlugin.CONFIG_ENTITY_HEALTH))
            appendArmor((LivingEntity) accessor.getEntity(), tooltip);
    }

    public void appendTail(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        if (!config.get(CorePlugin.CONFIG_MOD_NAME) || accessor.getEntity() instanceof ItemEntity)
            return;
        tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModName(accessor.getEntity()))));
    }

    private void appendHealth(LivingEntity living, ITooltip tooltip) {
        float health = living.getHealth();
        float maxHealth = living.getMaxHealth();

        if (living.getMaxHealth() > Waila.CONFIG.get().getGeneral().getMaxHealthForRender()) {
            HealthElement icon = new HealthElement(1, 1);
            ITextComponent text = new StringTextComponent(String.format("  %s/%s", Jade.dfCommas.format(health), Jade.dfCommas.format(maxHealth)));
            tooltip.add(icon);
            tooltip.append(text);
        } else {
            tooltip.add(new HealthElement(maxHealth * 0.5F, health * 0.5F));
        }
    }

    private void appendArmor(LivingEntity living, ITooltip tooltip) {
        float armor = living.getTotalArmorValue();
        if (armor == 0)
            return;
        if (armor > Waila.CONFIG.get().getGeneral().getMaxHealthForRender()) {
            ArmorElement icon = new ArmorElement(-1);
            ITextComponent text = new StringTextComponent(Jade.dfCommas.format(armor));
            tooltip.add(icon);
            tooltip.append(text);
        } else {
            tooltip.add(new ArmorElement(armor * 0.5F));
        }
    }

    @Override
    public ItemStack getDisplayItem(IEntityAccessor accessor, IPluginConfig config) {
        if (accessor.getEntity() instanceof ItemEntity) {
            return ((ItemEntity) accessor.getEntity()).getItem();
        }
        ItemStack stack = accessor.getEntity().getPickedResult(accessor.getHitResult());
        if (stack.getItem() instanceof SpawnEggItem && accessor.getEntity() instanceof LivingEntity) {
            return ItemStack.EMPTY;
        }
        return stack;
    }
}
