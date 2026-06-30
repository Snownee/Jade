package snownee.jade.test;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.harvest.ToolTier;
import snownee.jade.api.view.HideThingsExtensionProvider;

public class ExamplePlugin implements IWailaPlugin {

	public static final Identifier UID_TEST_FUEL = Identifier.parse("debug:furnace_fuel");
	public static final Identifier UID_TEST_BREWING = Identifier.parse("debug:item_storage");
	public static final Identifier UID_TEST_FLUIDS = Identifier.parse("debug:fluid_storage");
	public static final Identifier UID_TEST_ENERGY = Identifier.parse("debug:energy_storage");
	public static final Identifier UID_TEST_PROGRESS = Identifier.parse("debug:progress");
	public static final Identifier UID_TEST_STR_CFG = Identifier.parse("debug:furnace_fuel.str_cfg");
	public static final Identifier UID_TEST_FLOAT_CFG = Identifier.parse("debug:furnace_fuel.float_cfg");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ExampleDataProvider.INSTANCE, AbstractFurnaceBlockEntity.class);
		registration.registerItemStorage(ExampleItemStorageProvider.INSTANCE, BrewingStandBlockEntity.class);
		registration.registerItemStorage(HideThingsExtensionProvider.instance(), DispenserBlockEntity.class);
		registration.registerFluidStorage(ExampleFluidStorageProvider.INSTANCE, Slime.class);
		registration.registerEnergyStorage(ExampleEnergyStorageProvider.INSTANCE, Sheep.class);
		registration.registerProgress(ExampleProgressProvider.INSTANCE, AbstractFurnaceBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(ExampleComponentProvider.INSTANCE, AbstractFurnaceBlock.class);
		registration.addConfig(UID_TEST_STR_CFG, "", $ -> Identifier.tryParse($) != null);
		registration.addConfigListener(UID_TEST_STR_CFG, $ -> Jade.LOGGER.info("Changed: $: " + IWailaConfig.get().plugin().getString($)));
		registration.addConfig(UID_TEST_FLOAT_CFG, 0F, 0F, 100F, false);

		registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
			if (IWailaConfig.get().general().isDebug() && accessor instanceof BlockAccessor blockAccessor) {
				if (blockAccessor.getBlock() == Blocks.GRASS_BLOCK) {
					return registration.blockAccessor().from(blockAccessor).blockState(Blocks.TNT.defaultBlockState()).build();
				}
			}
			return accessor;
		});

		registration.addRayTraceCallback((
				(hitResult, accessor, accessor1) -> {
					if (accessor instanceof BlockAccessor blockAccessor) {
						if (blockAccessor.getBlock().equals(Blocks.FURNACE)) {
							BlockPos newPos = blockAccessor.getPosition().below();
							return registration.blockAccessor()
									.from(blockAccessor)
									.hit(blockAccessor.getHitResult().withPosition(newPos))
									.blockState(blockAccessor.getLevel().getBlockState(newPos))
									.blockEntity(blockAccessor.getLevel().getBlockEntity(newPos))
									.build();
						}
					}
					return accessor;
				}));

		registration.registerItemStorageClient(ExampleItemStorageProvider.INSTANCE);
		registration.registerFluidStorageClient(ExampleFluidStorageProvider.INSTANCE);
		registration.registerEnergyStorageClient(ExampleEnergyStorageProvider.INSTANCE);
		registration.registerProgressClient(ExampleProgressProvider.INSTANCE);

		// expected behavior: shows copper pickaxe on stone
		registration.addHarvestPlugin(registry -> {
			registry.insertTierBefore(
					JadeIds.JADE("pickaxe"),
					Identifier.withDefaultNamespace("test_pickaxe"),
					ToolTier.item(Items.COPPER_PICKAXE));
			registry.type(Identifier.parse("aaa"));
			registry.type(Identifier.parse("bbb")).addTier(ToolTier.alwaysFail(Items.DIAMOND)
					.addExtraBlocks(List.of(Blocks.CRYING_OBSIDIAN)));
		});
		registration.addHarvestPlugin(registry -> {
			registry.insertTierBefore(
					JadeIds.JADE("pickaxe"),
					Identifier.withDefaultNamespace("wooden_pickaxe"),
					ToolTier.item(Identifier.withDefaultNamespace("test_pickaxe"), Items.DIAMOND_PICKAXE));
		});
	}

}
