package snownee.jade.util;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;

public final class PlatformProxy {

	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		return UsernameCache.getLastKnownUsername(uuid);
	}

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
		if (stack.hasTag() && stack.getTag().contains("id")) {
			String s = stack.getTag().getString("id");
			if (s.contains(":")) {
				ResourceLocation id = ResourceLocation.tryParse(s);
				if (id != null) {
					return id.getNamespace();
				}
			}
		}
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

	public static List<ViewGroup<ItemStack>> wrapItemStorage(Object target, @Nullable Player player) {
		int size = 54;
		if (target instanceof CapabilityProvider<?> capProvider) {
			if (!(target instanceof Entity) || target instanceof AbstractChestedHorse) {
				LazyOptional<IItemHandler> optional = capProvider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
				if (optional.isPresent()) {
					return List.of(optional.map($ -> ItemView.fromItemHandler($, size, target instanceof AbstractChestedHorse ? 2 : 0)).get());
				}
			}
		}
		if (target instanceof Container container) {
			return List.of(ItemView.fromContainer(container, size, 0));
		}
		if (player != null && target instanceof EnderChestBlockEntity) {
			return List.of(ItemView.fromContainer(player.getEnderChestInventory(), size, 0));
		}
		return null;
	}

	public static List<ViewGroup<CompoundTag>> wrapFluidStorage(Object target, @Nullable Player player) {
		if (target instanceof CapabilityProvider<?> capProvider) {
			IFluidHandler fluidHandler = capProvider.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
			if (fluidHandler != null) {
				return FluidView.fromFluidHandler(fluidHandler);
			}
		}
		return null;
	}

	public static List<ViewGroup<CompoundTag>> wrapEnergyStorage(Object target, @Nullable Player player) {
		if (target instanceof CapabilityProvider<?> capProvider) {
			IEnergyStorage storage = capProvider.getCapability(CapabilityEnergy.ENERGY).orElse(null);
			if (storage != null && storage.getMaxEnergyStored() > 0)) {
				var group = new ViewGroup<>(List.of(EnergyView.fromForgeEnergy(storage)));
				group.getExtraData().putString("Unit", "FE");
				return List.of(group);
			}
		}
		return null;
	}

	public static boolean isDevEnv() {
		return !FMLEnvironment.production;
	}

	public static float getEnchantPowerBonus(BlockState state, Level world, BlockPos pos) {
		return state.getEnchantPowerBonus(world, pos);
	}

	@SuppressWarnings("deprecation")
	public static ResourceLocation getId(Block block) {
		return Registry.BLOCK.getKey(block);
	}

	@SuppressWarnings("deprecation")
	public static ResourceLocation getId(EntityType<?> entityType) {
		return Registry.ENTITY_TYPE.getKey(entityType);
	}

	@SuppressWarnings("deprecation")
	public static ResourceLocation getId(BlockEntityType<?> blockEntityType) {
		return Registry.BLOCK_ENTITY_TYPE.getKey(blockEntityType);
	}

	@SuppressWarnings("deprecation")
	public static ResourceLocation getId(PaintingVariant motive) {
		return Registry.PAINTING_VARIANT.getKey(motive);
	}

	public static String getPlatformIdentifier() {
		return "forge";
	}

	public static MutableComponent getProfressionName(VillagerProfession profession) {
		ResourceLocation profName = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession);
		return Component.translatable(EntityType.VILLAGER.getDescriptionId() + '.' + (!ResourceLocation.DEFAULT_NAMESPACE.equals(profName.getNamespace()) ? profName.getNamespace() + '.' : "") + profName.getPath());
	}
}
