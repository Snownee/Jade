package mcp.mobius.waila.network;

import java.util.List;
import java.util.function.Supplier;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class RequestTilePacket {

    public BlockPos pos;

    public RequestTilePacket(TileEntity tile) {
        this.pos = tile.getPos();
    }

    private RequestTilePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static RequestTilePacket read(PacketBuffer buffer) {
        return new RequestTilePacket(buffer.readBlockPos());
    }

    public static void write(RequestTilePacket message, PacketBuffer buffer) {
        buffer.writeBlockPos(message.pos);
    }

    public static class Handler {

        public static void onMessage(RequestTilePacket message, Supplier<NetworkEvent.Context> context) {
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null)
                return;

            server.execute(() -> {
                ServerPlayerEntity player = context.get().getSender();
                World world = player.world;
                if (!world.isBlockPresent(message.pos))
                    return;

                TileEntity tile = world.getTileEntity(message.pos);
                if (tile == null)
                    return;

                CompoundNBT tag = new CompoundNBT();
                List<IServerDataProvider<TileEntity>> providers = WailaRegistrar.INSTANCE.getBlockNBTProviders(tile);
                if (!providers.isEmpty()) {
                    for (IServerDataProvider<TileEntity> provider : providers) {
                        provider.appendServerData(tag, player, world, tile);
                    }
                } else {
                    tile.write(tag);
                }

                tag.putInt("x", message.pos.getX());
                tag.putInt("y", message.pos.getY());
                tag.putInt("z", message.pos.getZ());
                tag.putString("id", tile.getType().getRegistryName().toString());

                Waila.NETWORK.sendTo(new ReceiveDataPacket(tag), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            });
            context.get().setPacketHandled(true);
        }
    }
}
