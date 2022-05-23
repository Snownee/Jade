package snownee.jade.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import snownee.jade.Jade;
import snownee.jade.util.DumpGenerator;

public class DumpHandlersCommand {

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommandManager.literal(Jade.MODID).then(ClientCommandManager.literal("handlers").requires(source -> source.hasPermission(2)).executes(context -> {
			File file = new File("jade_handlers.md");
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(DumpGenerator.generateInfoDump());
				context.getSource().sendFeedback(new TranslatableComponent("command.jade.dump.success"));
				return 1;
			} catch (IOException e) {
				context.getSource().sendError(new TextComponent(e.getClass().getSimpleName() + ": " + e.getMessage()));
				return 0;
			}
		})));
	}
}
