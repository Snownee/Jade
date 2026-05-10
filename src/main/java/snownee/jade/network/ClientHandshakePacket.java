package snownee.jade.network;

import java.util.List;
import java.util.Map;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import snownee.jade.Jade;
import snownee.jade.addon.harvest.LootTableMineableCollector;
import snownee.jade.api.JadeIds;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.ServerPluginConfig;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JadeServerPlayer;

// This class of structure should not be changed
public record ClientHandshakePacket(String protocolVersion) implements CustomPacketPayload {
	public static final Type<ClientHandshakePacket> TYPE = new Type<>(JadeIds.PACKET_CLIENT_HANDSHAKE);
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientHandshakePacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			ClientHandshakePacket::protocolVersion,
			ClientHandshakePacket::new);

	public static void handle(ClientHandshakePacket message, ServerPayloadContext context) {
		context.execute(() -> {
			ServerPlayer player = context.player();
			if (!Jade.PROTOCOL_VERSION.equals(message.protocolVersion)) {
				String version = CommonProxy.getModVersion(Jade.ID).orElse("UNKNOWN");
				player.sendSystemMessage(Component.translatable("jade.protocolMismatch", version), false);
				return;
			}
			((JadeServerPlayer) player).jade$setConnected(true);
			Map<Identifier, Object> configs = ServerPluginConfig.instance().values();
			List<Block> shearableBlocks = LootTableMineableCollector.getShearableBlocks();
			if (!configs.isEmpty()) {
				Jade.LOGGER.debug("Syncing config to {} ({})", player.getPlainTextName(), player.getGameProfile().id());
			}
			List<Identifier> blockProviderIds = WailaCommonRegistration.instance().blockDataProviders.mappedIds();
			List<Identifier> entityProviderIds = WailaCommonRegistration.instance().entityDataProviders.mappedIds();
			CommonProxy.sendPacket(player, new ServerHandshakePacket(configs, shearableBlocks, blockProviderIds, entityProviderIds));
		});
	}

	@Override
	public Type<ClientHandshakePacket> type() {
		return TYPE;
	}
}