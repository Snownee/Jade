package snownee.jade.test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ExamplePlugin1 implements IWailaPlugin {

	public static final ResourceLocation FURNACE_FUEL = new ResourceLocation(Jade.MODID, "furnace_fuel");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ExampleComponentProvider.INSTANCE, AbstractFurnaceBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(ExampleComponentProvider.INSTANCE, AbstractFurnaceBlock.class);
		registration.hideTarget(EntityType.AREA_EFFECT_CLOUD);
		registration.hideTarget(Blocks.BARRIER);

		registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
			if (accessor instanceof BlockAccessor blockAccessor) {
				if (blockAccessor.getBlock() == Blocks.GRASS_BLOCK) {
					return registration.blockAccessor().from(blockAccessor).blockState(Blocks.TNT.defaultBlockState()).build();
				}
			}
			return accessor;
		});
	}

}
