package snownee.jade.compat;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screens.Screen;

public record RecipeLookupResult(float score, Consumer<@Nullable Screen> action) {
	public static final RecipeLookupResult FAIL = new RecipeLookupResult(0, s -> {});

	public boolean isFail() {
		return score <= 0;
	}
}
