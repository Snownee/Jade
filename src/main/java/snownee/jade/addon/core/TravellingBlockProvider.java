package snownee.jade.addon.core;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.MineTravellingBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

public enum TravellingBlockProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, ResourceLocation> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		decodeFromData(accessor).ifPresent(id -> tooltip.add(Component.translatable("jade.travellingBlock", id)));
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.CORE_TRAVELLING_BLOCK;
	}

	@Override
	public @Nullable ResourceLocation streamData(BlockAccessor accessor) {
		if (accessor.getBlockEntity() instanceof MineTravellingBlockEntity be) {
			return be.getTargetDimension().location();
		}
		return null;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ResourceLocation> streamCodec() {
		return ResourceLocation.STREAM_CODEC.cast();
	}
}
