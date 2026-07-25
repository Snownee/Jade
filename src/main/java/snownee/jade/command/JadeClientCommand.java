package snownee.jade.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.gui.PinScreen;
import snownee.jade.impl.config.ServerPluginConfig;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.DumpGenerator;
import snownee.jade.util.JsonConfig;

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
		})).then(literalFactory.apply("config").executes(_ -> {
			// execute in the next tick because when we press enter in chat, after the command is executed, any screen will be closed
			Minecraft.getInstance().schedule(() -> {
				IWailaConfig.get().invalidate();
				Minecraft.getInstance().gui.setScreen(new HomeConfigScreen(null));
			});
			return 1;
		})).then(literalFactory.apply("use_profile")
				.then(argumentFactory.apply("index", IntegerArgumentType.integer(0, 3)).executes(context -> {
					int index = IntegerArgumentType.getInteger(context, "index");
					Minecraft.getInstance().execute(() -> Jade.useProfile(index));
					return 1;
				}))
		).then(literalFactory.apply("pin").executes(_ -> {
			Minecraft.getInstance().schedule(() -> Minecraft.getInstance().gui.setScreen(new PinScreen()));
			return 1;
		})).then(literalFactory.apply("generate_server_overrides")
				.executes(context -> generate(context.getSource(), sendSuccess, sendFailure, null))
				.then(argumentFactory.apply("pattern", StringArgumentType.greedyString())
						.executes(context -> {
							String pattern = StringArgumentType.getString(context, "pattern");
							return generate(context.getSource(), sendSuccess, sendFailure, pattern);
						})));
	}

	private static <T> int generate(
			T source,
			BiConsumer<T, Component> sendSuccess,
			BiConsumer<T, Component> sendFailure,
			@Nullable String pattern) {
		Pattern regex = null;
		if (pattern != null) {
			try {
				regex = Pattern.compile(pattern);
			} catch (PatternSyntaxException e) {
				sendFailure.accept(
						source,
						Component.translatable("command.jade.generate_server_overrides.invalid_pattern", e.getMessage()));
				return 0;
			}
		}

		Map<Identifier, Object> allValues = IWailaConfig.get().plugin().values();
		Map<Identifier, Object> filteredValues;
		if (regex != null) {
			filteredValues = Maps.newHashMap();
			for (var entry : allValues.entrySet()) {
				if (regex.matcher(entry.getKey().toString()).matches()) {
					filteredValues.put(entry.getKey(), entry.getValue());
				}
			}
			if (filteredValues.isEmpty()) {
				sendFailure.accept(source, Component.translatable("command.jade.generate_server_overrides.no_match"));
				return 0;
			}
		} else {
			filteredValues = allValues;
		}

		File jadeDir = new File(CommonProxy.getConfigDirectory(), Jade.ID);
		jadeDir.mkdirs();
		File file = new File(jadeDir, "server-plugin-overrides.json");
		int i = 1;
		while (file.exists()) {
			file = new File(jadeDir, "server-plugin-overrides." + i + ".json");
			i++;
		}

		try {
			String json = JsonConfig.GSON.toJson(
					ServerPluginConfig.DATA_CODEC.encodeStart(JsonOps.INSTANCE, filteredValues).getOrThrow());
			try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
				writer.write(json);
			}
		} catch (Exception e) {
			sendFailure.accept(source, Component.literal(e.getClass().getSimpleName() + ": " + e.getMessage()));
			return 0;
		}

		Component msg = Component.translatable(
				"command.jade.generate_server_overrides.success",
				Component.literal(file.getName())
						.withStyle(Style.EMPTY.withUnderlined(true).withClickEvent(new ClickEvent.OpenFile(file.getAbsolutePath()))));
		sendSuccess.accept(source, msg);
		return 1;
	}
}
