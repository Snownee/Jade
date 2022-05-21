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

public class DatapackBlockManager {

	private static final Set<BlockPos> itemFrames = Sets.newConcurrentHashSet();

	public static void onEntityJoin(Entity entity) {
		if (entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME) {
			itemFrames.add(entity.blockPosition());
		}
	}

	public static void onEntityLeave(Entity entity) {
		if (entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME) {
			BlockPos pos = entity.blockPosition();
			getFakeBlock(entity.level, pos);
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
