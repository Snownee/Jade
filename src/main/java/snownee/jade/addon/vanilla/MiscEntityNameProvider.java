package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import snownee.jade.JadePlugin;

public class MiscEntityNameProvider implements IEntityComponentProvider {

    public static final MiscEntityNameProvider INSTANCE = new MiscEntityNameProvider();

    @Override
    public void appendHead(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
        if (!tooltip.isEmpty() || shouldExclude(accessor.getEntity(), config)) {
            return;
        }
        tooltip.add(accessor.getEntity().getDisplayName().applyTextStyle(TextFormatting.WHITE));
    }

    @Override
    public ItemStack getDisplayItem(IEntityAccessor accessor, IPluginConfig config) {
        if (shouldExclude(accessor.getEntity(), config)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = accessor.getEntity().getPickedResult(accessor.getHitResult());
        if (stack.getItem() instanceof SpawnEggItem) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    public static boolean shouldExclude(Entity entity, IPluginConfig config) {
        if (!config.get(JadePlugin.MISC_ENTITY)) {
            return true;
        }
        if (entity instanceof ArmorStandEntity) {
            return false;
        }
        return entity instanceof LivingEntity || entity instanceof AbstractMinecartEntity || entity instanceof HangingEntity || entity instanceof ItemFrameEntity;
    }

}
