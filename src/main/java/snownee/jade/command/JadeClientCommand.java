package snownee.jade.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.util.DumpGenerator;

public class JadeClientCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal(Jade.MODID + "c").then(Commands.literal("handlers").executes(context -> {
			File file = new File("jade_handlers.md");
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(DumpGenerator.generateInfoDump());
				context.getSource().sendSuccess(Component.translatable("command.jade.dump.success"), false);
				return 1;
			} catch (IOException e) {
				context.getSource().sendFailure(Component.literal(e.getClass().getSimpleName() + ": " + e.getMessage()));
				return 0;
			}
		})).then(Commands.literal("config").executes(context -> {
			Minecraft.getInstance().tell(() -> {
				Jade.CONFIG.invalidate();
				Minecraft.getInstance().setScreen(new HomeConfigScreen(null));
			});
			return 1;
		})));
	}
}
