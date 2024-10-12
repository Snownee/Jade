package snownee.jade.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.util.DumpGenerator;

public class JadeClientCommand {

	public static <T> LiteralArgumentBuilder<T> create(
			Function<String, LiteralArgumentBuilder<T>> literalFactory,
			ArgumentFactory<T> argumentFactory,
			BiConsumer<T, Component> sendSuccess,
			BiConsumer<T, Component> sendFailure) {
		return literalFactory.apply(Jade.ID + "c").then(literalFactory.apply("handlers").executes(context -> {
			File file = new File("jade_handlers.md");
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(DumpGenerator.generateInfoDump());
				sendSuccess.accept(context.getSource(), Component.translatable("command.jade.dump.success"));
				return 1;
			} catch (IOException e) {
				sendFailure.accept(context.getSource(), Component.literal(e.getClass().getSimpleName() + ": " + e.getMessage()));
				return 0;
			}
		})).then(literalFactory.apply("config").executes(context -> {
			// execute in the next tick because when we press enter in chat, after the command is executed, any screen will be closed
			Minecraft.getInstance().schedule(() -> {
				IWailaConfig.get().invalidate();
				Minecraft.getInstance().setScreen(new HomeConfigScreen(null));
			});
			return 1;
		})).then(literalFactory.apply("use_profile")
				.then(argumentFactory.apply("index", IntegerArgumentType.integer(0, 3)).executes(context -> {
					int index = IntegerArgumentType.getInteger(context, "index");
					Minecraft.getInstance().execute(() -> Jade.useProfile(index));
					return 1;
				}))
		);
	}
}
