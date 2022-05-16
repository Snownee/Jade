package snownee.jade.test;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.common.MinecraftForge;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.event.WailaRayTraceEvent;

@WailaPlugin
public class ExamplePlugin implements IWailaPlugin {

	private static IWailaClientRegistration client;

	public ExamplePlugin() {
		MinecraftForge.EVENT_BUS.addListener(this::overrideGrass);
	}

	public void overrideGrass(WailaRayTraceEvent event) {
		Accessor<?> accessor = event.getAccessor();
		if (accessor instanceof BlockAccessor blockAccessor) {
			if (blockAccessor.getBlock() == Blocks.GRASS_BLOCK) {
				accessor = client.createBlockAccessor(Blocks.TNT.defaultBlockState(), null, accessor.getLevel(), accessor.getPlayer(), null, blockAccessor.getHitResult(), accessor.isServerConnected());
				event.setAccessor(accessor);
			}
		}
	}

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ExampleComponentProvider.INSTANCE, AbstractFurnaceBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		ExamplePlugin.client = registration;
		registration.registerComponentProvider(ExampleComponentProvider.INSTANCE, TooltipPosition.BODY, AbstractFurnaceBlock.class);
		registration.hideTarget(EntityType.AREA_EFFECT_CLOUD);
	}

}
