package snownee.jade.addon.vanilla;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

public enum CommandBlockProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, String> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		String command = decodeFromData(accessor).orElse("");
		if (command.isBlank()) {
			return;
		}
		tooltip.add(Component.literal("> " + command));
	}

	@Override
	@Nullable
	public String streamData(BlockAccessor accessor) {
		if (!accessor.getPlayer().canUseGameMasterBlocks()) {
			return null;
		}
		String command = ((CommandBlockEntity) accessor.getBlockEntity()).getCommandBlock().getCommand();
		if (command.length() > 40) {
			command = command.substring(0, 37) + "...";
		}
		return command;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, String> streamCodec() {
		return ByteBufCodecs.STRING_UTF8.cast();
	}

	@Override
	public boolean shouldRequestData(BlockAccessor accessor) {
		return accessor.getPlayer().canUseGameMasterBlocks();
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_COMMAND_BLOCK;
	}

}
