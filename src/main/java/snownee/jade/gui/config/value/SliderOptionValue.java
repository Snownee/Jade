package snownee.jade.gui.config.value;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.function.Consumer;

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

	public SliderOptionValue(String optionName, float value, Consumer<Float> save, float min, float max, FloatUnaryOperator aligner) {
		super(optionName, save);
		this.value = value;
		this.min = min;
		this.max = max;
		this.aligner = aligner;
		slider = new Slider(this, 0, 0, 100, 20, getTitle());
		addWidget(slider, 0);
	}

	@Override
	public void setValue(Float value) {
		slider.setValue(value);
	}

	public static class Slider extends AbstractSliderButton {
		private static final DecimalFormat fmt = new DecimalFormat("##.##");
		private final SliderOptionValue parent;

		public Slider(SliderOptionValue parent, int x, int y, int width, int height, Component message) {
			super(x, y, width, height, message, fromScaled(parent.value, parent.min, parent.max));
			this.parent = parent;
			applyValue();
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

		//save?
		@Override
		protected void updateMessage() {
			parent.value = toScaled();
			parent.save();
		}

		//get title
		@Override
		protected void applyValue() {
			setMessage(Component.literal(fmt.format(toScaled())));
		}

		private void setValue(float value) {
			this.value = fromScaled(value, parent.min, parent.max);
			applyValue();
			updateMessage();
		}

		@Override
		protected MutableComponent createNarrationMessage() {
			return CommonComponents.joinForNarration(parent.getTitle(), super.createNarrationMessage());
		}
	}
}
