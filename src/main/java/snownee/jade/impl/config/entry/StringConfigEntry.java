package snownee.jade.impl.config.entry;

import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.config.PluginConfig;

public class StringConfigEntry extends ConfigEntry<String> {

	private Predicate<String> validator;

	public StringConfigEntry(ResourceLocation id, String defaultValue, Predicate<String> validator) {
		super(id, defaultValue);
		this.validator = validator;
	}

	@Override
	public boolean isValidValue(Object value) {
		return value.getClass() == String.class && validator.test((String) value);
	}

	@Override
	public OptionValue<?> createUI(OptionsList options, String optionName) {
		return options.input(optionName, getValue(), s -> PluginConfig.INSTANCE.set(id, s), validator);
	}

}
