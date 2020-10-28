package mcp.mobius.waila.network;

import mcp.mobius.waila.Waila;
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

import java.util.function.Supplier;

public class MessageRequestEntity {

    public int entityId;

    public MessageRequestEntity(Entity entity) {
        this.entityId = entity.getEntityId();
    }

    private MessageRequestEntity(int entityId) {
        this.entityId = entityId;
    }

    public static MessageRequestEntity read(PacketBuffer buffer) {
        return new MessageRequestEntity(buffer.readInt());
    }

    public static void write(MessageRequestEntity message, PacketBuffer buffer) {
        buffer.writeInt(message.entityId);
    }

    public static class Handler {

        public static void onMessage(final MessageRequestEntity message, Supplier<NetworkEvent.Context> context) {
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
                if (WailaRegistrar.INSTANCE.hasNBTEntityProviders(entity)) {
                    WailaRegistrar.INSTANCE.getNBTEntityProviders(entity).values().forEach(l -> l.forEach(p -> p.appendServerData(tag, player, world, entity)));
                } else {
                    entity.writeWithoutTypeId(tag);
                }

                tag.putInt("WailaEntityID", entity.getEntityId());

                Waila.NETWORK.sendTo(new MessageReceiveData(tag), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            });
            context.get().setPacketHandled(true);
        }
    }
}
