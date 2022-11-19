package snownee.jade.util;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.mixin.AbstractHorseAccess;

public final class PlatformProxy {

	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		return UsernameCache.getLastKnownUsername(uuid);
	}

	public static File getConfigDirectory() {
		return FabricLoader.getInstance().getConfigDir().toFile();
	}

	public static boolean isShears(ItemStack tool) {
		return tool.getItem() instanceof ShearsItem;
	}

	public static boolean isShearable(BlockState state) {
		return state.is(FabricMineableTags.SHEARS_MINEABLE);
	}

	public static boolean isCorrectToolForDrops(BlockState state, Player player) {
		return player.hasCorrectToolForDrops(state);
	}

	public static String getModIdFromItem(ItemStack stack) {
		if (stack.hasTag() && stack.getTag().contains("id")) {
			String s = stack.getTag().getString("id");
			if (s.contains(":")) {
				ResourceLocation id = ResourceLocation.tryParse(s);
				if (id != null) {
					return id.getNamespace();
				}
			}
		}
		return Registry.ITEM.getKey(stack.getItem()).getNamespace();
	}

	public static boolean isPhysicallyClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	public static void init() {
	}

	public static List<ViewGroup<ItemStack>> wrapItemStorage(Object target, ServerPlayer player) {
		int size = 54;
		if (target instanceof AbstractHorseAccess horse) {
			return List.of(ItemView.fromContainer(horse.getInventory(), size, 2));
		}
		if (target instanceof Container container) {
			if (target instanceof ChestBlockEntity be) {
				if (be.getBlockState().getBlock() instanceof ChestBlock chestBlock) {
					Container compound = ChestBlock.getContainer(chestBlock, be.getBlockState(), be.getLevel(), be.getBlockPos(), false);
					if (compound != null) {
						container = compound;
					}
				}
			}
			return List.of(ItemView.fromContainer(container, size, 0));
		}
		if (player != null && target instanceof EnderChestBlockEntity) {
			return List.of(ItemView.fromContainer(player.getEnderChestInventory(), size, 0));
		}
		if (target instanceof SidedStorageBlockEntity be) {
			var storage = be.getItemStorage(null);
			if (storage != null) {
				return List.of(ItemView.fromStorage(storage, size, 0));
			}
		}
		var storage = lookupBlock(ItemStorage.SIDED, target);
		if (storage != null) {
			return List.of(ItemView.fromStorage(storage, size, 0));
		}
		return null;
	}

	public static List<ViewGroup<CompoundTag>> wrapFluidStorage(Object target, ServerPlayer player) {
		if (target instanceof SidedStorageBlockEntity be) {
			var storage = be.getFluidStorage(null);
			if (storage != null) {
				return FluidView.fromStorage(storage);
			}
		}
		var storage = lookupBlock(FluidStorage.SIDED, target);
		if (storage != null) {
			return FluidView.fromStorage(storage);
		}
		return null;
	}

	private static final Direction[] DIRECTIONS = Direction.values();

	public static <T> T lookupBlock(BlockApiLookup<T, Direction> sided, Object target) {
		T found = null;
		if (target instanceof BlockEntity be) {
			for (Direction direction : DIRECTIONS) {
				T t = sided.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, direction);
				if (found != t && found != null && t != null) {
					return null;
				}
				if (t != null) {
					found = t;
				}
			}
		}
		return found;
	}

	public static List<ViewGroup<CompoundTag>> wrapEnergyStorage(Object target, ServerPlayer player) {
		return null;
	}

	public static boolean isDevEnv() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	public static float getEnchantPowerBonus(BlockState state, Level world, BlockPos pos) {
		if (WailaClientRegistration.INSTANCE.customEnchantPowers.containsKey(state.getBlock())) {
			return WailaClientRegistration.INSTANCE.customEnchantPowers.get(state.getBlock()).getEnchantPowerBonus(state, world, pos);
		}
		return state.is(Blocks.BOOKSHELF) ? 1 : 0;
	}

	public static ResourceLocation getId(Block block) {
		return Registry.BLOCK.getKey(block);
	}

	public static ResourceLocation getId(EntityType<?> entityType) {
		return Registry.ENTITY_TYPE.getKey(entityType);
	}

	public static ResourceLocation getId(BlockEntityType<?> blockEntityType) {
		return Registry.BLOCK_ENTITY_TYPE.getKey(blockEntityType);
	}

	public static ResourceLocation getId(PaintingVariant motive) {
		return Registry.PAINTING_VARIANT.getKey(motive);
	}

	public static String getPlatformIdentifier() {
		return "fabric";
	}
}
