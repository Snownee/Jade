package snownee.jade.impl.config.entry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;

public class BooleanConfigEntry extends ConfigEntry<Boolean> {

	public BooleanConfigEntry(ResourceLocation id, boolean defaultValue) {
		super(id, defaultValue);
	}

	@Override
	public boolean isValidValue(Object value) {
		return value.getClass() == Boolean.class;
	}

	@Override
	public OptionValue<?> createUI(OptionsList options, String optionName, BiConsumer<ResourceLocation, Object> setter) {
		return options.choices(optionName, this::getValue, b -> setter.accept(id, b));
	}

}
