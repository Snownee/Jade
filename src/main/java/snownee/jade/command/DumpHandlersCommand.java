package snownee.jade.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.util.DumpGenerator;

public class DumpHandlersCommand {

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommandManager.literal(Jade.MODID).then(ClientCommandManager.literal("handlers").requires(source -> source.hasPermission(2)).executes(context -> {
			File file = new File("jade_handlers.md");
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(DumpGenerator.generateInfoDump());
				context.getSource().sendFeedback(Component.translatable("command.jade.dump.success"));
				return 1;
			} catch (IOException e) {
				context.getSource().sendError(Component.literal(e.getClass().getSimpleName() + ": " + e.getMessage()));
				return 0;
			}
		})));
	}
}
