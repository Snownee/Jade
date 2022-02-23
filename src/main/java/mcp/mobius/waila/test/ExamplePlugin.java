package mcp.mobius.waila.test;

import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.event.WailaRayTraceEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.common.MinecraftForge;

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
