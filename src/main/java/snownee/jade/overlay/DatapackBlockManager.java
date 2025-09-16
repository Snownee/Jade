package snownee.jade.overlay;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import snownee.jade.Jade;
import snownee.jade.api.Accessor;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.util.ModIdentification;

public class DatapackBlockManager {
	public static final Logger LOGGER = Jade.LOGGER;
	private static final IntSet displays = IntSets.synchronize(IntOpenHashSet.of());

	public static void onEntityJoin(Entity entity) {
		if (isAcceptableEntity(entity)) {
			displays.add(entity.getId());
		}
	}

	public static void onEntityLeave(Entity entity) {
		if (isAcceptableEntity(entity)) {
			displays.remove(entity.getId());
		}
	}

	public static ItemStack getFakeBlock(LevelAccessor level, BlockPos pos) {
		if (displays.isEmpty()) {
			return ItemStack.EMPTY;
		}
		List<Display> entities = level.getEntitiesOfClass(Display.class, new AABB(pos), DatapackBlockManager::isAcceptableEntity);
		if (!entities.isEmpty()) {
			ItemStack selected = ItemStack.EMPTY;
			float selectedScore = 0f;
			for (Display display : entities) {
				if (!displays.contains(display.getId())) {
					continue;
				}
				ItemStack itemStack = ItemStack.EMPTY;
				if (display instanceof Display.BlockDisplay blockDisplay) {
					itemStack = blockDisplay.getBlockState().getCloneItemStack(level, pos, false);
				} else if (display instanceof Display.ItemDisplay itemDisplay) {
					itemStack = itemDisplay.getItemStack();
				}
				if (itemStack.isEmpty()) {
					continue;
				}
				float score = 0f;
				DataComponentPatch componentsPatch = itemStack.getComponentsPatch();
				Optional<? extends CustomData> customData = componentsPatch.get(DataComponents.CUSTOM_DATA);
				if (customData != null && customData.isPresent()) {
					CustomData data = customData.get();
					if (data.tag.contains(ModIdentification.JADE_STACK)) {
						score += 10f;
					} else if (data.tag.contains(ModIdentification.POLYMER_STACK)) {
						score += 2f;
					}
				}
				Optional<? extends ResourceLocation> itemModel = componentsPatch.get(DataComponents.ITEM_MODEL);
				if (itemModel != null && itemModel.isPresent()) {
					score += 1f;
				}
				if (score > selectedScore) {
					selected = itemStack;
					selectedScore = score;
				}
			}
			return selected;
		}
		return ItemStack.EMPTY;
	}

	@Nullable
	public static Accessor<?> override(HitResult hitResult, @Nullable Accessor<?> accessor, @Nullable Accessor<?> originalAccessor) {
		if (accessor instanceof BlockAccessorImpl target && target.getServersideRep().isEmpty()) {
			target.setServersideRep(getFakeBlock(target.getLevel(), target.getPosition()));
		}
		return accessor;
	}

	public static boolean isAcceptableEntity(Entity entity) {
		return entity.getType() == EntityType.BLOCK_DISPLAY || entity.getType() == EntityType.ITEM_DISPLAY;
	}

}
