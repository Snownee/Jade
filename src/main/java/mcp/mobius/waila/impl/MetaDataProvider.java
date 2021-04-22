package mcp.mobius.waila.impl;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.utils.WailaExceptionHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

public class MetaDataProvider {

	public static int rateLimiter = 250;

	public void gatherComponents(Entity entity, DataAccessor accessor, Tooltip tooltip, TooltipPosition position) {
		accessor.tooltipPosition = position;
		if (entity == null) {
			gatherBlockComponents(accessor, tooltip, position);
		} else {
			gatherEntityComponents(entity, accessor, tooltip, position);
		}
		accessor.tooltipPosition = null;
	}

	private void gatherBlockComponents(DataAccessor accessor, Tooltip tooltip, TooltipPosition position) {
		Block block = accessor.getBlock();
		List<IComponentProvider> providers = WailaRegistrar.INSTANCE.getBlockProviders(block, position);
		for (IComponentProvider provider : providers) {
			try {
				provider.append(tooltip, accessor, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider.getClass().toString(), tooltip);
			}
		}
	}

	private void gatherEntityComponents(Entity entity, DataAccessor accessor, Tooltip tooltip, TooltipPosition position) {
		List<IEntityComponentProvider> providers = WailaRegistrar.INSTANCE.getEntityProviders(entity, position);
		for (IEntityComponentProvider provider : providers) {
			try {
				provider.append(tooltip, accessor, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider.getClass().toString(), tooltip);
			}
		}
	}
}
