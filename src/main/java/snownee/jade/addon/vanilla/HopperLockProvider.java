package snownee.jade.addon.vanilla;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import snownee.jade.addon.access.AccessibilityPlugin;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;

public enum HopperLockProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, Boolean> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (decodeFromData(accessor).orElse(false)) {
			if (config.get(JadeIds.MC_REDSTONE) ||
					(IWailaConfig.get().accessibility().getEnableAccessibilityPlugin() && config.get(JadeIds.ACCESS_BLOCK_DETAILS))) {
				String objectName = tooltip.getMessage(JadeIds.CORE_OBJECT_NAME);
				AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.locked");
			}
		}
	}

	@Override
	public Boolean streamData(BlockAccessor accessor) {
		return !accessor.getBlockState().getValue(BlockStateProperties.ENABLED);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Boolean> streamCodec() {
		return ByteBufCodecs.BOOL.cast();
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
