package snownee.jade.addon.debug;

import org.jspecify.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.RandomizableContainer;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

public class BlockLootTableProvider implements StreamServerDataProvider<BlockAccessor, Pair<Identifier, Long>> {
	public static final BlockLootTableProvider INSTANCE = new BlockLootTableProvider();
	public static final StreamCodec<RegistryFriendlyByteBuf, Pair<Identifier, Long>> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC,
			Pair::getFirst,
			ByteBufCodecs.LONG,
			Pair::getSecond,
			Pair::new);

	@Override
	public @Nullable Pair<Identifier, Long> streamData(BlockAccessor accessor) {
		if (!shouldRequestData(accessor)) {
			return null;
		}
		if (accessor.getBlockEntity() instanceof RandomizableContainer blockEntity && blockEntity.getLootTable() != null) {
			return Pair.of(blockEntity.getLootTable().identifier(), blockEntity.getLootTableSeed());
		}
		return null;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Pair<Identifier, Long>> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public boolean shouldRequestData(BlockAccessor accessor) {
		return accessor.getPlayer().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public static class Client extends BlockLootTableProvider implements IBlockComponentProvider {
		public static final Client INSTANCE = new Client();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			Pair<Identifier, Long> pair = decodeFromData(accessor).orElse(null);
			if (pair != null) {
				tooltip.add(Component.translatable("jade.lootTable", pair.getFirst()));
				tooltip.add(Component.translatable("jade.lootTableSeed", pair.getSecond()));
			}
		}

		@Override
		public boolean enabledByDefault() {
			return false;
		}
	}

	@Override
	public Identifier getUid() {
		return JadeIds.DEBUG_LOOT_TABLE;
	}
}
