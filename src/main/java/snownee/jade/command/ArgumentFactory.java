package snownee.jade.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

@FunctionalInterface
public interface ArgumentFactory<S> {
	<T> RequiredArgumentBuilder<S, T> apply(String name, ArgumentType<T> type);
}
