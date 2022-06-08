package snownee.jade.util;

import java.io.File;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.addon.fabric.BlockInventoryProvider;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.mixin.AbstractHorseAccess;

public final class PlatformProxy {

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
		return Registry.ITEM.getKey(stack.getItem()).getNamespace();
	}

	public static boolean isPhysicallyClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	public static void init() {
		if (isPhysicallyClient()) {
			ClientPlatformProxy.init();
		}
	}

	public static void putHorseInvData(AbstractChestedHorse horse, CompoundTag data, int size) {
		SimpleContainer container = ((AbstractHorseAccess) horse).getInventory();
		BlockInventoryProvider.putInvData(data, container, size, 2);
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

	public static ResourceLocation getId(Motive motive) {
		return Registry.MOTIVE.getKey(motive);
	}

}
