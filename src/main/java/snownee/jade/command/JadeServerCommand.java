package snownee.jade.command;

import java.util.Collection;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.jade.Jade;
import snownee.jade.api.Identifiers;

public class JadeServerCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal(Jade.MODID).requires(source -> source.hasPermission(2)).then(Commands.literal("show").then(Commands.argument("targets", EntityArgument.players()).executes(context -> {
			return showOrHide(EntityArgument.getPlayers(context, "targets"), true);
		}))).then(Commands.literal("hide").then(Commands.argument("targets", EntityArgument.players()).executes(context -> {
			return showOrHide(EntityArgument.getPlayers(context, "targets"), false);
		}))));
	}

	private static int showOrHide(Collection<ServerPlayer> players, boolean show) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(show);
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, Identifiers.PACKET_SHOW_OVERLAY, buf);
		}
		return players.size();
	}
}
