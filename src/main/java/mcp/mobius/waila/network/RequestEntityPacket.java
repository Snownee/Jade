package mcp.mobius.waila.network;

import java.util.List;
import java.util.function.Supplier;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.impl.WailaCommonRegistration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class RequestEntityPacket {

	public int entityId;
	public boolean showDetails;

	public RequestEntityPacket(Entity entity, boolean showDetails) {
		this(entity.getId(), showDetails);
	}

	private RequestEntityPacket(int entityId, boolean showDetails) {
		this.entityId = entityId;
		this.showDetails = showDetails;
	}

	public static RequestEntityPacket read(FriendlyByteBuf buffer) {
		return new RequestEntityPacket(buffer.readVarInt(), buffer.readBoolean());
	}

	public static void write(RequestEntityPacket message, FriendlyByteBuf buffer) {
		buffer.writeVarInt(message.entityId);
		buffer.writeBoolean(message.showDetails);
	}

	public static class Handler {

		public static void onMessage(final RequestEntityPacket message, Supplier<NetworkEvent.Context> context) {
			context.get().enqueueWork(() -> {
				ServerPlayer player = context.get().getSender();
				Level world = player.level;
				Entity entity = world.getEntity(message.entityId);

				if (entity == null || player.distanceToSqr(entity) > RequestTilePacket.MAX_DISTANCE_SQR)
					return;

				List<IServerDataProvider<Entity>> providers = WailaCommonRegistration.INSTANCE.getEntityNBTProviders(entity);
				if (providers.isEmpty())
					return;

				CompoundTag tag = new CompoundTag();
				for (IServerDataProvider<Entity> provider : providers) {
					provider.appendServerData(tag, player, world, entity, message.showDetails);
				}

				tag.putInt("WailaEntityID", entity.getId());

				Waila.NETWORK.sendTo(new ReceiveDataPacket(tag), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			});
			context.get().setPacketHandled(true);
		}
	}
}
