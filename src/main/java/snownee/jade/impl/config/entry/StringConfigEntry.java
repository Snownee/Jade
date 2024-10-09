package snownee.jade.impl.config.entry;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;

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
	public OptionValue<?> createUI(
			OptionsList options,
			String optionName,
			IPluginConfig config,
			BiConsumer<ResourceLocation, Object> setter) {
		return options.input(optionName, () -> config.getString(id), s -> setter.accept(id, s), validator);
	}

}
