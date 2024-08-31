package snownee.jade.gui.config.value;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;

public class InputOptionValue<T> extends OptionValue<T> {

	public static final Predicate<String> INTEGER = s -> s.matches("[-+]?[0-9]+");
	public static final Predicate<String> FLOAT = s -> s.matches("[-+]?([0-9]*[.,][0-9]+|[0-9]+)");

	private final EditBox textField;
	private final Predicate<String> validator;

	public InputOptionValue(Runnable responder, String optionName, Supplier<T> getter, Consumer<T> setter, Predicate<String> validator) {
		super(optionName, getter, setter);
		this.validator = validator;
		textField = new EditBox(client.font, 0, 0, 98, 18, getTitle());
		updateValue();
		textField.setResponder(s -> {
			if (this.validator.test(s)) {
				textField.setTextColor(ChatFormatting.WHITE.getColor());
			} else {
				textField.setTextColor(ChatFormatting.RED.getColor());
			}
			responder.run();
		});
		addWidget(textField, 0);
	}

	private void setValue(String text) {
		if (value instanceof String) {
			value = (T) text;
		}

		try {
			if (value instanceof Integer) {
				value = (T) Integer.valueOf(text);
			} else if (value instanceof Short) {
				value = (T) Short.valueOf(text);
			} else if (value instanceof Byte) {
				value = (T) Byte.valueOf(text);
			} else if (value instanceof Long) {
				value = (T) Long.valueOf(text);
			} else if (value instanceof Double) {
				value = (T) Double.valueOf(text);
			} else if (value instanceof Float) {
				value = (T) Float.valueOf(text);
			}
		} catch (NumberFormatException e) {
			// no-op
		}

		save();
	}

	@Override
	public boolean isValidValue() {
		return validator.test(textField.getValue());
	}

	@Override
	public void setValue(T value) {
		textField.setValue(String.valueOf(value));
	}

	@Override
	public void updateValue() {
		value = getter.get();
		textField.setValue(String.valueOf(value));
	}

}
