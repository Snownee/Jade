package mcp.mobius.waila.gui.config.value;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import snownee.jade.Jade;

public class OptionsEntryValueSlider extends OptionsEntryValue<Float> {

	public static final Predicate<String> ANY = s -> true;
	public static final Predicate<String> INTEGER = s -> s.matches("^[0-9]*$");
	public static final Predicate<String> FLOAT = s -> s.matches("[-+]?([0-9]*\\.[0-9]+|[0-9]+)") || s.endsWith(".") || s.isEmpty();

	private final Slider slider;
	private float min;
	private float max;

	public OptionsEntryValueSlider(String optionName, float value, Consumer<Float> save, float min, float max) {
		super(optionName, save);
		this.value = value;
		this.min = min;
		this.max = max;
		this.slider = new Slider(this, 0, 0, 100, 20, getTitle());
	}

	public OptionsEntryValueSlider(String optionName, float value, Consumer<Float> save) {
		this(optionName, value, save, 0, 1);
	}

	@Override
	protected void drawValue(MatrixStack matrixStack, int entryWidth, int entryHeight, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks) {
		slider.x = x + 135;
		slider.y = y + entryHeight / 6;
		slider.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public IGuiEventListener getListener() {
		return slider;
	}

	public static class Slider extends AbstractSlider {
		private final OptionsEntryValueSlider value;

		public Slider(OptionsEntryValueSlider value, int x, int y, int width, int height, ITextComponent message) {
			super(x, y, width, height, message, fromScaled(value.value, value.min, value.max));
			this.value = value;
			func_230972_a_();
		}

		public float toScaled() {
			return value.min + (value.max - value.min) * (float) sliderValue;
		}

		public static double fromScaled(float f, float min, float max) {
			return MathHelper.clamp((f - min) / (max - min), 0, 1);
		}

		//save?
		@Override
		protected void func_230979_b_() {
			value.value = toScaled();
			value.save();
		}

		//get title
		@Override
		protected void func_230972_a_() {
			setMessage(new StringTextComponent(Jade.dfCommas.format(toScaled())));
		}
	}
}