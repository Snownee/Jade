package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import snownee.jade.addon.access.AccessibilityPlugin;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;

public enum HopperLockProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getServerData().contains("HopperLocked")) {
			if (config.get(JadeIds.MC_REDSTONE)) {
				AccessibilityPlugin.replaceTitle(tooltip, "block.locked");
			} else if (IWailaConfig.get().accessibility().getEnableAccessibilityPlugin() && config.get(JadeIds.ACCESS_BLOCK_DETAILS)) {
				AccessibilityPlugin.replaceTitle(tooltip, "block.locked");
			}
		}
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		BlockState blockState = accessor.getBlockState();
		if (blockState.hasProperty(BlockStateProperties.ENABLED) && !blockState.getValue(BlockStateProperties.ENABLED)) {
			data.putBoolean("HopperLocked", true);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_HOPPER_LOCK;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return ObjectNameProvider.getBlock().getDefaultPriority() + 10;
	}
}
