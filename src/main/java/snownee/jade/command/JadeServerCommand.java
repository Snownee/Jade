package snownee.jade.command;

import java.util.Collection;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import snownee.jade.Jade;
import snownee.jade.network.ShowOverlayPacket;
import snownee.jade.util.CommonProxy;

public class JadeServerCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal(Jade.MODID).requires(source -> source.hasPermission(2)).then(Commands.literal("show").then(Commands.argument("targets", EntityArgument.players()).executes(context -> {
			return showOrHide(EntityArgument.getPlayers(context, "targets"), true);
		}))).then(Commands.literal("hide").then(Commands.argument("targets", EntityArgument.players()).executes(context -> {
			return showOrHide(EntityArgument.getPlayers(context, "targets"), false);
		}))));
	}

	//TODO: proxy
	private static int showOrHide(Collection<ServerPlayer> players, boolean show) {
		ShowOverlayPacket msg = new ShowOverlayPacket(show);
		for (ServerPlayer player : players) {
			CommonProxy.NETWORK.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
		return players.size();
	}
}
