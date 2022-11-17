package snownee.jade.test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ExamplePlugin implements IWailaPlugin {

	public static final ResourceLocation UID_TEST_FUEL = new ResourceLocation("debug:furnace_fuel");
	public static final ResourceLocation UID_TEST_BREWING = new ResourceLocation("debug:item_storage");
	public static final ResourceLocation UID_TEST_FLUIDS = new ResourceLocation("debug:fluid_storage");
	public static final ResourceLocation UID_TEST_ENERGY = new ResourceLocation("debug:energy_storage");
	public static final ResourceLocation UID_TEST_PROGRESS = new ResourceLocation("debug:progress");
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
		registration.hideTarget(EntityType.AREA_EFFECT_CLOUD);

		registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
			if (accessor instanceof BlockAccessor blockAccessor) {
				if (blockAccessor.getBlock() == Blocks.GRASS_BLOCK) {
					return client.blockAccessor().from(blockAccessor).blockState(Blocks.TNT.defaultBlockState()).build();
				}
			}
			return accessor;
		});

		registration.registerItemStorageClient(ExampleItemStorageProvider.INSTANCE);
		registration.registerFluidStorageClient(ExampleFluidStorageProvider.INSTANCE);
		registration.registerEnergyStorageClient(ExampleEnergyStorageProvider.INSTANCE);
		registration.registerProgressClient(ExampleProgressProvider.INSTANCE);
	}

}
