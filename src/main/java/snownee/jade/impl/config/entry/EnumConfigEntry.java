package snownee.jade.impl.config.entry;

import java.util.Locale;
import java.util.function.BiConsumer;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;

public class EnumConfigEntry<E extends Enum<E>> extends ConfigEntry<E> {

	public EnumConfigEntry(ResourceLocation id, E defaultValue) {
		super(id, defaultValue);
	}

	@Override
	public boolean isValidValue(Object value) {
		return value.getClass() == getDefaultValue().getClass();
	}

	@Override
	public E convertValue(Object value) {
		if (value.getClass() == String.class) {
			//noinspection unchecked
			return (E) Enum.valueOf(getDefaultValue().getClass(), (String) value);
		}
		//noinspection unchecked
		return (E) value;
	}

	@Override
	public OptionValue<?> createUI(
			OptionsList options,
			String optionName,
			IPluginConfig config,
			BiConsumer<ResourceLocation, Object> setter) {
		//noinspection unchecked
		return options.choices(optionName, () -> (E) config.getEnum(id), e -> setter.accept(id, e), builder -> {
			builder.withTooltip(e -> {
				String key = OptionsList.Entry.makeKey(optionName + "_" + e.name().toLowerCase(Locale.ENGLISH) + "_desc");
				if (!I18n.exists(key)) {
					return null;
				}
				return Tooltip.create(Component.translatable(key));
			});
		});
	}

}
