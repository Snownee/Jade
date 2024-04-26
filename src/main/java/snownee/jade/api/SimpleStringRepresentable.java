package snownee.jade.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.StringRepresentable;

public interface SimpleStringRepresentable extends StringRepresentable {
	@Override
	default @NotNull String getSerializedName() {
		return toString();
	}
}
