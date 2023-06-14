package snownee.jade.gui.config.value;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class InputOptionValue<T> extends OptionValue<T> {

	public static final Predicate<String> INTEGER = s -> s.matches("[-+]?[0-9]+");
	public static final Predicate<String> FLOAT = s -> s.matches("[-+]?([0-9]*\\.[0-9]+|[0-9]+)");

	private final EditBox textField;
	private final Predicate<String> validator;

	public InputOptionValue(Runnable responder, String optionName, T value, Consumer<T> setter, Predicate<String> validator) {
		super(optionName, setter);

		this.value = value;
		this.validator = validator;
		textField = new EditBox(client.font, 0, 0, 98, 18, Component.literal(""));
		textField.setValue(String.valueOf(value));
		textField.setResponder(s -> {
			if (this.validator.test(s)) {
				setValue(s);
				textField.setTextColor(ChatFormatting.WHITE.getColor());
			} else {
				textField.setTextColor(ChatFormatting.RED.getColor());
			}
			responder.run();
		});
	}

	@Override
	protected void drawValue(GuiGraphics guiGraphics, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean selected, float partialTicks) {
		textField.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public AbstractWidget getListener() {
		return textField;
	}

	private void setValue(String text) {
		if (value instanceof String)
			value = (T) text;

		try {
			if (value instanceof Integer)
				value = (T) Integer.valueOf(text);
			else if (value instanceof Short)
				value = (T) Short.valueOf(text);
			else if (value instanceof Byte)
				value = (T) Byte.valueOf(text);
			else if (value instanceof Long)
				value = (T) Long.valueOf(text);
			else if (value instanceof Double)
				value = (T) Double.valueOf(text);
			else if (value instanceof Float)
				value = (T) Float.valueOf(text);
		} catch (NumberFormatException e) {
			// no-op
		}

		save();
	}

	@Override
	public void setDisabled(boolean b) {
		super.setDisabled(b);
		textField.setEditable(!b);
	}

	@Override
	public boolean isValidValue() {
		return validator.test(textField.getValue());
	}

}
