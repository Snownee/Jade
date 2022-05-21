package snownee.jade.util;

import java.io.File;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.items.CapabilityItemHandler;
import snownee.jade.addon.forge.BlockInventoryProvider;

public final class PlatformProxy {

	public static File getConfigDirectory() {
		return FMLPaths.CONFIGDIR.get().toFile();
	}

	public static boolean isShears(ItemStack tool) {
		return tool.is(Tags.Items.SHEARS);
	}

	public static boolean isShearable(BlockState state) {
		return state.getBlock() instanceof IForgeShearable;
	}

	public static boolean isCorrectToolForDrops(BlockState state, Player player) {
		return ForgeHooks.isCorrectToolForDrops(state, player);
	}

	public static String getModIdFromItem(ItemStack stack) {
		return stack.getItem().getCreatorModId(stack);
	}

	public static boolean isPhysicallyClient() {
		return FMLEnvironment.dist.isClient();
	}

	public static void init() {
		if (isPhysicallyClient()) {
			ClientPlatformProxy.init();
		}
	}

	public static void putHorseInvData(AbstractChestedHorse horse, CompoundTag data, int size) {
		horse.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> BlockInventoryProvider.putInvData(data, h, size, 2));
	}

}
