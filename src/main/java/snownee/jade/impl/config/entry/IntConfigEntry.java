package snownee.jade.impl.config.entry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.InputOptionValue;
import snownee.jade.gui.config.value.OptionValue;

public class IntConfigEntry extends ConfigEntry<Integer> {

	private boolean slider;
	private int min;
	private int max;

	public IntConfigEntry(ResourceLocation id, int defaultValue, int min, int max, boolean slider) {
		super(id, defaultValue);
		this.slider = slider;
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean isValidValue(Object value) {
		return value instanceof Number && ((Number) value).intValue() >= min && ((Number) value).intValue() <= max;
	}

	@Override
	public Integer convertValue(Object value) {
		return ((Number) value).intValue();
	}

	@Override
	public OptionValue<?> createUI(
			OptionsList options,
			String optionName,
			IPluginConfig config,
			BiConsumer<ResourceLocation, Object> setter) {
		if (slider) {
			return options.slider(
					optionName,
					() -> config.getFloat(id),
					f -> setter.accept(id, (int) (float) f),
					min,
					max,
					f -> (float) Math.round(f));
		} else {
			return options.input(
					optionName,
					() -> config.getInt(id),
					i -> setter.accept(id, Mth.clamp(i, min, max)),
					InputOptionValue.INTEGER.and($ -> isValidValue(Integer.valueOf($))));
		}
	}

}
