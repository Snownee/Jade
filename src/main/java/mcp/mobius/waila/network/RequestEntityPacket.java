package mcp.mobius.waila.network;

import java.util.List;
import java.util.function.Supplier;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class RequestEntityPacket {

    public int entityId;

    public RequestEntityPacket(Entity entity) {
        this.entityId = entity.getEntityId();
    }

    private RequestEntityPacket(int entityId) {
        this.entityId = entityId;
    }

    public static RequestEntityPacket read(PacketBuffer buffer) {
        return new RequestEntityPacket(buffer.readVarInt());
    }

    public static void write(RequestEntityPacket message, PacketBuffer buffer) {
        buffer.writeVarInt(message.entityId);
    }

    public static class Handler {

        public static void onMessage(final RequestEntityPacket message, Supplier<NetworkEvent.Context> context) {
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null)
                return;

            server.execute(() -> {
                ServerPlayerEntity player = context.get().getSender();
                World world = player.world;
                Entity entity = world.getEntityByID(message.entityId);

                if (entity == null)
                    return;

                CompoundNBT tag = new CompoundNBT();
                List<IServerDataProvider<Entity>> providers = WailaRegistrar.INSTANCE.getEntityNBTProviders(entity);
                if (!providers.isEmpty()) {
                    for (IServerDataProvider<Entity> provider : providers) {
                        provider.appendServerData(tag, player, world, entity);
                    }
                } else {
                    entity.writeWithoutTypeId(tag);
                }

                tag.putInt("WailaEntityID", entity.getEntityId());

                Waila.NETWORK.sendTo(new ReceiveDataPacket(tag), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            });
            context.get().setPacketHandled(true);
        }
    }
}
