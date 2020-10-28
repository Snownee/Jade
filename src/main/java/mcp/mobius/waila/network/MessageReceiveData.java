package mcp.mobius.waila.network;

import mcp.mobius.waila.api.impl.DataAccessor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageReceiveData {

    public CompoundNBT tag;

    public MessageReceiveData(CompoundNBT tag) {
        this.tag = tag;
    }

    public static MessageReceiveData read(PacketBuffer buffer) {
        return new MessageReceiveData(buffer.readCompoundTag());
    }

    public static void write(MessageReceiveData message, PacketBuffer buffer) {
        buffer.writeCompoundTag(message.tag);
    }

    public static class Handler {

        public static void onMessage(MessageReceiveData message, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                DataAccessor.INSTANCE.setServerData(message.tag);
            });
            context.get().setPacketHandled(true);
        }
    }
}
