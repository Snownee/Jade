package snownee.jade.impl.config.entry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.InputOptionValue;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.config.PluginConfig;

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
	public OptionValue<?> createUI(OptionsList options, String optionName) {
		if (slider) {
			return options.slider(optionName, getValue(), f -> PluginConfig.INSTANCE.set(id, (int) (float) f), min, max, f -> (float) Math.round(f));
		} else {
			return options.input(optionName, getValue(), i -> PluginConfig.INSTANCE.set(id, Mth.clamp(i, min, max)), InputOptionValue.INTEGER.and($ -> isValidValue(Integer.valueOf($))));
		}
	}

}
