package snownee.jade.gui.config.value;

import java.util.function.Consumer;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import snownee.jade.overlay.DisplayHelper;

public class SliderOptionValue extends OptionValue<Float> {

	public static final Predicate<String> ANY = s -> true;
	public static final Predicate<String> INTEGER = s -> s.matches("^[0-9]*$");
	public static final Predicate<String> FLOAT = s -> s.matches("[-+]?([0-9]*\\.[0-9]+|[0-9]+)") || s.endsWith(".") || s.isEmpty();

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
	}

	@Override
	protected void drawValue(GuiGraphics guiGraphics, int entryWidth, int entryHeight, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks) {
		slider.setX(x);
		slider.setY(y + entryHeight / 6);
		slider.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public AbstractWidget getListener() {
		return slider;
	}

	public static class Slider extends AbstractSliderButton {
		private final SliderOptionValue parent;

		public Slider(SliderOptionValue parent, int x, int y, int width, int height, Component message) {
			super(x, y, width, height, message, fromScaled(parent.value, parent.min, parent.max));
			this.parent = parent;
			applyValue();
		}

		public float toScaled() {
			return parent.aligner.apply(parent.min + (parent.max - parent.min) * (float) value);
		}

		public static double fromScaled(float f, float min, float max) {
			return Mth.clamp((f - min) / (max - min), 0, 1);
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
			setMessage(Component.literal(DisplayHelper.dfCommas.format(toScaled())));
		}
	}
}
