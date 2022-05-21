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
public class ExamplePlugin implements IWailaPlugin {

	public static final ResourceLocation UID_TEST = new ResourceLocation(Jade.MODID, "test");
	private static IWailaClientRegistration client;

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ExampleComponentProvider.INSTANCE, AbstractFurnaceBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		ExamplePlugin.client = registration;
		registration.registerBlockComponent(ExampleComponentProvider.INSTANCE, AbstractFurnaceBlock.class);
		registration.hideTarget(EntityType.AREA_EFFECT_CLOUD);

		registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
			if (accessor instanceof BlockAccessor blockAccessor) {
				if (blockAccessor.getBlock() == Blocks.GRASS_BLOCK) {
					return client.createBlockAccessor(Blocks.TNT.defaultBlockState(), null, accessor.getLevel(), accessor.getPlayer(), null, blockAccessor.getHitResult(), accessor.isServerConnected());
				}
			}
			return accessor;
		});
	}

}
