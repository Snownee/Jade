package snownee.jade.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import snownee.jade.Jade;
import snownee.jade.util.CommonProxy;

public class JadeServerCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal(Jade.MODID).requires(source -> source.hasPermission(2)).then(Commands.literal("show").then(Commands.argument("targets", EntityArgument.players()).executes(context -> {
			return CommonProxy.showOrHideFromServer(EntityArgument.getPlayers(context, "targets"), true);
		}))).then(Commands.literal("hide").then(Commands.argument("targets", EntityArgument.players()).executes(context -> {
			return CommonProxy.showOrHideFromServer(EntityArgument.getPlayers(context, "targets"), false);
		}))));
	}

}
