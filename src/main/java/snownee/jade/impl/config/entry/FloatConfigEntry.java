package snownee.jade.impl.config.entry;

import java.util.function.BiConsumer;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.InputOptionValue;
import snownee.jade.gui.config.value.OptionValue;

public class FloatConfigEntry extends ConfigEntry<Float> {

	private boolean slider;
	private float min;
	private float max;

	public FloatConfigEntry(ResourceLocation id, float defaultValue, float min, float max, boolean slider) {
		super(id, defaultValue);
		this.slider = slider;
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean isValidValue(Object value) {
		return value instanceof Number && ((Number) value).floatValue() >= min && ((Number) value).floatValue() <= max;
	}

	@Override
	public void setValue(Object value) {
		super.setValue(((Number) value).floatValue());
	}

	@Override
	public OptionValue<?> createUI(OptionsList options, String optionName, BiConsumer<ResourceLocation, Object> setter) {
		if (slider) {
			return options.slider(
					optionName,
					this::getValue,
					f -> setter.accept(id, f),
					min,
					max,
					FloatUnaryOperator.identity());
		} else {
			return options.input(
					optionName,
					this::getValue,
					f -> setter.accept(id, Mth.clamp(f, min, max)),
					InputOptionValue.FLOAT.and($ -> isValidValue(Float.valueOf($.replace(",", ".")))));
		}
	}

}
