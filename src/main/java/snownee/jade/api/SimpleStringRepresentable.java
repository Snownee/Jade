package snownee.jade.api;

import net.minecraft.util.StringRepresentable;

public interface SimpleStringRepresentable extends StringRepresentable {
	@Override
	default String getSerializedName() {
		return toString();
	}
}
