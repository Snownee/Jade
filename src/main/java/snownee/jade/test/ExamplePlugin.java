package snownee.jade.test;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.config.IWailaConfig;

public class ExamplePlugin implements IWailaPlugin {

	public static final ResourceLocation UID_TEST_FUEL = ResourceLocation.parse("debug:furnace_fuel");
	public static final ResourceLocation UID_TEST_BREWING = ResourceLocation.parse("debug:item_storage");
	public static final ResourceLocation UID_TEST_FLUIDS = ResourceLocation.parse("debug:fluid_storage");
	public static final ResourceLocation UID_TEST_ENERGY = ResourceLocation.parse("debug:energy_storage");
	public static final ResourceLocation UID_TEST_PROGRESS = ResourceLocation.parse("debug:progress");
	public static final ResourceLocation UID_TEST_STR_CFG = ResourceLocation.parse("debug:furnace_fuel.str_cfg");
	public static final ResourceLocation UID_TEST_FLOAT_CFG = ResourceLocation.parse("debug:furnace_fuel.float_cfg");
	private static IWailaClientRegistration client;

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ExampleComponentProvider.INSTANCE, AbstractFurnaceBlockEntity.class);
		registration.registerItemStorage(ExampleItemStorageProvider.INSTANCE, BrewingStandBlockEntity.class);
		registration.registerFluidStorage(ExampleFluidStorageProvider.INSTANCE, Slime.class);
		registration.registerEnergyStorage(ExampleEnergyStorageProvider.INSTANCE, Sheep.class);
		registration.registerProgress(ExampleProgressProvider.INSTANCE, AbstractFurnaceBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		ExamplePlugin.client = registration;
		registration.registerBlockComponent(ExampleComponentProvider.INSTANCE, AbstractFurnaceBlock.class);
		registration.addConfig(UID_TEST_STR_CFG, "", $ -> ResourceLocation.tryParse($) != null);
		registration.addConfigListener(UID_TEST_STR_CFG, $ -> Jade.LOGGER.info("Changed: $: " + IWailaConfig.get().plugin().getString($)));
		registration.addConfig(UID_TEST_FLOAT_CFG, 0F, 0F, 100F, false);

		registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
			if (IWailaConfig.get().general().isDebug() && accessor instanceof BlockAccessor blockAccessor) {
				if (blockAccessor.getBlock() == Blocks.GRASS_BLOCK) {
					return client.blockAccessor().from(blockAccessor).blockState(Blocks.TNT.defaultBlockState()).build();
				}
			}
			return accessor;
		});

		registration.addRayTraceCallback((
				(hitResult, accessor, accessor1) -> {
					if (accessor instanceof BlockAccessor blockAccessor) {
						if (blockAccessor.getBlock().equals(Blocks.FURNACE)) {
							BlockPos newPos = blockAccessor.getPosition().below();
							var t = registration.blockAccessor()
									.from(blockAccessor)
									.hit(blockAccessor.getHitResult().withPosition(newPos))
									.blockState(blockAccessor.getLevel().getBlockState(newPos))
									.blockEntity(blockAccessor.getLevel().getBlockEntity(newPos))
									.build();
							return t;
						}
					}
					return accessor;
				}));

		registration.registerItemStorageClient(ExampleItemStorageProvider.INSTANCE);
		registration.registerFluidStorageClient(ExampleFluidStorageProvider.INSTANCE);
		registration.registerEnergyStorageClient(ExampleEnergyStorageProvider.INSTANCE);
		registration.registerProgressClient(ExampleProgressProvider.INSTANCE);
	}

}
