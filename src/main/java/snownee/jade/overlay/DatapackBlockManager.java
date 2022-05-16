package snownee.jade.overlay;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public class DatapackBlockManager {

	private static final Set<BlockPos> itemFrames = Sets.newConcurrentHashSet();

	@SubscribeEvent
	public static void onEntityJoin(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME) {
			itemFrames.add(entity.blockPosition());
		}
	}

	@SubscribeEvent
	public static void onEntityLeave(EntityLeaveWorldEvent event) {
		Entity entity = event.getEntity();
		if (entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME) {
			BlockPos pos = entity.blockPosition();
			getFakeBlock(event.getWorld(), pos);
		} else if (entity == Minecraft.getInstance().player) {
			itemFrames.clear();
		}
	}

	public static ItemStack getFakeBlock(Level level, BlockPos pos) {
		if (itemFrames.contains(pos)) {
			List<ItemFrame> entities = level.getEntitiesOfClass(ItemFrame.class, new AABB(pos), $ -> {
				return $.isInvisible() && $.isAlive();
			});
			if (entities.isEmpty()) {
				itemFrames.remove(pos);
			} else {
				ItemStack stack = entities.get(0).getItem();
				if (stack.hasTag() && stack.getTag().contains("storedItem")) {
					stack = ItemStack.of(stack.getTagElement("storedItem"));
				}
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

}
