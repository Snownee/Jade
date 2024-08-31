package snownee.jade.impl.config.entry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
	public void setValue(Object value) {
		super.setValue(((Number) value).intValue());
	}

	@Override
	public OptionValue<?> createUI(OptionsList options, String optionName, BiConsumer<ResourceLocation, Object> setter) {
		if (slider) {
			return options.slider(
					optionName,
					() -> Float.valueOf(getValue()),
					f -> setter.accept(id, (int) (float) f),
					min,
					max,
					f -> (float) Math.round(f));
		} else {
			return options.input(
					optionName,
					this::getValue,
					i -> setter.accept(id, Mth.clamp(i, min, max)),
					InputOptionValue.INTEGER.and($ -> isValidValue(Integer.valueOf($))));
		}
	}

}
