package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;

public class MiscEntityNameProvider implements IEntityComponentProvider {

	public static final MiscEntityNameProvider INSTANCE = new MiscEntityNameProvider();

	//    @Override
	//    public void appendHead(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
	//        if (!tooltip.isEmpty() || shouldExclude(accessor.getEntity(), config)) {
	//            return;
	//        }
	//        tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getEntityName(), accessor.getEntity().getDisplayName().getString())));
	//    }
	//
	//    @Override
	//    public void appendTail(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
	//        if (config.get(JadePlugin.HIDE_MOD_NAME)) {
	//            tooltip.clear();
	//            return;
	//        }
	//        if (!shouldExclude(accessor.getEntity(), config) && !(accessor.getEntity() instanceof ArmorStandEntity)) {
	//            tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModInfo(accessor.getEntity()).getName())));
	//        }
	//    }

	@Override
	public ItemStack getDisplayItem(IEntityAccessor accessor, IPluginConfig config) {
		//        if (shouldExclude(accessor.getEntity(), config)) {
		//            return ItemStack.EMPTY;
		//        }
		if (accessor.getEntity() instanceof ItemEntity) {
			return ((ItemEntity) accessor.getEntity()).getItem();
		}
		ItemStack stack = accessor.getEntity().getPickedResult(accessor.getHitResult());
		if (stack.getItem() instanceof SpawnEggItem && accessor.getEntity() instanceof LivingEntity) {
			return ItemStack.EMPTY;
		}
		return stack;
	}

	//    public static boolean shouldExclude(Entity entity, IPluginConfig config) {
	//        if (!config.get(JadePlugin.MISC_ENTITY)) {
	//            return true;
	//        }
	//        if (entity instanceof ArmorStandEntity) {
	//            return false;
	//        }
	//        return entity instanceof LivingEntity || entity instanceof AbstractMinecartEntity || entity instanceof HangingEntity || entity instanceof ItemFrameEntity;
	//    }

}
