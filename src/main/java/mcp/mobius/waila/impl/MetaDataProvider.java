package mcp.mobius.waila.impl;

import java.util.List;

import mcp.mobius.waila.api.IAccessor;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.utils.WailaExceptionHandler;
import net.minecraft.block.Block;
import net.minecraft.util.math.RayTraceResult;

public class MetaDataProvider {

	public static int rateLimiter = 250;

	public void gatherComponents(IAccessor accessor, Tooltip tooltip, TooltipPosition position) {
		accessor.setTooltipPosition(position);
		if (accessor instanceof IBlockAccessor && accessor.getHitResult().getType() == RayTraceResult.Type.BLOCK) {
			gatherBlockComponents((IBlockAccessor) accessor, tooltip, position);
		} else if (accessor instanceof IEntityAccessor && accessor.getHitResult().getType() == RayTraceResult.Type.ENTITY) {
			gatherEntityComponents((IEntityAccessor) accessor, tooltip, position);
		}
		accessor.setTooltipPosition(null);
	}

	private void gatherBlockComponents(IBlockAccessor accessor, Tooltip tooltip, TooltipPosition position) {
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

	private void gatherEntityComponents(IEntityAccessor accessor, Tooltip tooltip, TooltipPosition position) {
		List<IEntityComponentProvider> providers = WailaRegistrar.INSTANCE.getEntityProviders(accessor.getEntity(), position);
		for (IEntityComponentProvider provider : providers) {
			try {
				provider.append(tooltip, accessor, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider.getClass().toString(), tooltip);
			}
		}
	}
}
