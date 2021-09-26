package mcp.mobius.waila.network;

import java.util.List;
import java.util.function.Supplier;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.impl.WailaRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class RequestTilePacket {

	public static int MAX_DISTANCE_SQR = 900;

	public BlockPos pos;
	public boolean showDetails;

	public RequestTilePacket(BlockEntity tile, boolean showDetails) {
		this(tile.getBlockPos(), showDetails);
	}

	private RequestTilePacket(BlockPos pos, boolean showDetails) {
		this.pos = pos;
		this.showDetails = showDetails;
	}

	public static RequestTilePacket read(FriendlyByteBuf buffer) {
		return new RequestTilePacket(buffer.readBlockPos(), buffer.readBoolean());
	}

	public static void write(RequestTilePacket message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeBoolean(message.showDetails);
	}

	public static class Handler {

		public static void onMessage(RequestTilePacket message, Supplier<NetworkEvent.Context> context) {
			context.get().enqueueWork(() -> {
				ServerPlayer player = context.get().getSender();
				Level world = player.level;
				if (message.pos.distSqr(player.blockPosition()) > MAX_DISTANCE_SQR || !world.isLoaded(message.pos))
					return;

				BlockEntity tile = world.getBlockEntity(message.pos);
				if (tile == null)
					return;

				List<IServerDataProvider<BlockEntity>> providers = WailaRegistrar.INSTANCE.getBlockNBTProviders(tile);
				if (providers.isEmpty())
					return;

				CompoundTag tag = new CompoundTag();
				for (IServerDataProvider<BlockEntity> provider : providers) {
					provider.appendServerData(tag, player, world, tile, message.showDetails);
				}

				tag.putInt("x", message.pos.getX());
				tag.putInt("y", message.pos.getY());
				tag.putInt("z", message.pos.getZ());
				tag.putString("id", tile.getType().getRegistryName().toString());

				Waila.NETWORK.sendTo(new ReceiveDataPacket(tag), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			});
			context.get().setPacketHandled(true);
		}
	}
}
