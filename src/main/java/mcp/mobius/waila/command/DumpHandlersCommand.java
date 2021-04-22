package mcp.mobius.waila.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.mojang.brigadier.CommandDispatcher;

import mcp.mobius.waila.utils.DumpGenerator;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class DumpHandlersCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("dumpHandlers").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
			File file = new File("waila_handlers.md");
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(DumpGenerator.generateInfoDump());
				context.getSource().sendFeedback(new TranslationTextComponent("command.waila.dump.success"), false);
				return 1;
			} catch (IOException e) {
				context.getSource().sendErrorMessage(new StringTextComponent(e.getClass().getSimpleName() + ": " + e.getMessage()));
				return 0;
			}
		}));
	}
}
