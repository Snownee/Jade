package snownee.jade.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.util.DumpGenerator;

public class JadeClientCommand {

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommandManager.literal(Jade.MODID + "c").then(ClientCommandManager.literal("handlers").executes(context -> {
			File file = new File("jade_handlers.md");
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(DumpGenerator.generateInfoDump());
				context.getSource().sendFeedback(Component.translatable("command.jade.dump.success"));
				return 1;
			} catch (IOException e) {
				context.getSource().sendError(Component.literal(e.getClass().getSimpleName() + ": " + e.getMessage()));
				return 0;
			}
		})).then(ClientCommandManager.literal("config").executes(context -> {
			Minecraft.getInstance().tell(() -> {
				Jade.CONFIG.invalidate();
				Minecraft.getInstance().setScreen(new HomeConfigScreen(null));
			});
			return 1;
		})));
	}
}
