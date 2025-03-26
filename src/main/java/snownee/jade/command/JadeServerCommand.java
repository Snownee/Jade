package snownee.jade.command;

import java.util.Collection;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import snownee.jade.Jade;
import snownee.jade.network.ShowOverlayPacket;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JadeServerPlayer;

public class JadeServerCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal(Jade.ID)
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("show")
						.then(Commands.argument("targets", EntityArgument.players())
								.executes(context -> showOrHideFromServer(EntityArgument.getPlayers(context, "targets"), true))))
				.then(Commands.literal("hide")
						.then(Commands.argument("targets", EntityArgument.players())
								.executes(context -> showOrHideFromServer(EntityArgument.getPlayers(context, "targets"), false)))));
	}

	public static int showOrHideFromServer(Collection<ServerPlayer> players, boolean show) {
		ShowOverlayPacket msg = new ShowOverlayPacket(show);
		players = players.stream().filter((player -> ((JadeServerPlayer) player).jade$isConnected())).toList();
		for (ServerPlayer player : players) {
			CommonProxy.sendPacket(player, msg);
		}
		return players.size();
	}
}
