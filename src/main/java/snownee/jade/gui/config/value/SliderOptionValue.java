package snownee.jade.gui.config.value;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class SliderOptionValue extends OptionValue<Float> {

	private final Slider slider;
	private float min;
	private float max;
	private FloatUnaryOperator aligner;

	public SliderOptionValue(
			String optionName,
			Supplier<Float> getter,
			Consumer<Float> setter,
			float min,
			float max,
			FloatUnaryOperator aligner) {
		super(optionName, getter, setter);
		value = getter.get();
		this.min = min;
		this.max = max;
		this.aligner = aligner;
		slider = new Slider(this, 0, 0, 100, 20, getTitle());
		updateValue();
		addWidget(slider, 0);
	}

	@Override
	public void setValue(Float value) {
		slider.setValue(value, true);
	}

	@Override
	public void updateValue() {
		slider.setValue(value = getter.get(), false);
	}

	public static class Slider extends AbstractSliderButton {
		private static final DecimalFormat fmt = new DecimalFormat("##.##");
		private final SliderOptionValue parent;

		public Slider(SliderOptionValue parent, int x, int y, int width, int height, Component message) {
			super(x, y, width, height, message, fromScaled(parent.value, parent.min, parent.max));
			this.parent = parent;
			updateMessage();
		}

		public static double fromScaled(float f, float min, float max) {
			return Mth.clamp((f - min) / (max - min), 0, 1);
		}

		public float toScaled() {
			float f = parent.aligner.apply(parent.min + (parent.max - parent.min) * (float) value);
			String s = fmt.format(f);
			try {
				return fmt.parse(s).floatValue();
			} catch (ParseException e) {
				return f;
			}
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.literal(fmt.format(toScaled())));
		}

		@Override
		protected void applyValue() {
			float scaled = toScaled();
			if (parent.value != scaled) {
				parent.value = scaled;
				parent.save();
			}
		}

		private void setValue(float value, boolean applyValue) {
			if (value != toScaled()) {
				this.value = fromScaled(value, parent.min, parent.max);
				if (applyValue) {
					applyValue();
				}
			}
			updateMessage();
		}

		@Override
		protected MutableComponent createNarrationMessage() {
			return CommonComponents.joinForNarration(parent.getTitle(), super.createNarrationMessage());
		}
	}
}
