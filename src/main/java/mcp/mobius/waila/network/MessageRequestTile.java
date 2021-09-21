package mcp.mobius.waila.network;

import java.util.function.Supplier;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageRequestTile {

	public static int MAX_DISTANCE_SQR = 900;
	public BlockPos pos;

	public MessageRequestTile(TileEntity tile) {
		this.pos = tile.getPos();
	}

	private MessageRequestTile(BlockPos pos) {
		this.pos = pos;
	}

	public static MessageRequestTile read(PacketBuffer buffer) {
		return new MessageRequestTile(buffer.readBlockPos());
	}

	public static void write(MessageRequestTile message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
	}

	public static class Handler {

		public static void onMessage(MessageRequestTile message, Supplier<NetworkEvent.Context> context) {
			context.get().enqueueWork(() -> {
				ServerPlayerEntity player = context.get().getSender();
				World world = player.world;
				if (message.pos.distanceSq(player.getPosition()) > MAX_DISTANCE_SQR || !world.isBlockPresent(message.pos))
					return;

				TileEntity tile = world.getTileEntity(message.pos);
				BlockState state = world.getBlockState(message.pos);

				if (tile == null)
					return;

				CompoundNBT tag = new CompoundNBT();
				if (WailaRegistrar.INSTANCE.hasNBTProviders(tile) || WailaRegistrar.INSTANCE.hasNBTProviders(state.getBlock())) {
					WailaRegistrar.INSTANCE.getNBTProviders(tile).values().forEach(l -> l.forEach(p -> p.appendServerData(tag, player, world, tile)));
					WailaRegistrar.INSTANCE.getNBTProviders(state.getBlock()).values().forEach(l -> l.forEach(p -> p.appendServerData(tag, player, world, tile)));
				} else {
					tile.write(tag);
				}

				tag.putInt("x", message.pos.getX());
				tag.putInt("y", message.pos.getY());
				tag.putInt("z", message.pos.getZ());
				tag.putString("id", tile.getType().getRegistryName().toString());

				Waila.NETWORK.sendTo(new MessageReceiveData(tag), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
			});
			context.get().setPacketHandled(true);
		}
	}
}
