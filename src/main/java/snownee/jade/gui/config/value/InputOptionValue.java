package snownee.jade.gui.config.value;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class InputOptionValue<T> extends OptionValue<T> {

	public static final Predicate<String> INTEGER = s -> s.matches("^[0-9]*$");
	public static final Predicate<String> FLOAT = s -> s.matches("[-+]?([0-9]*\\.[0-9]+|[0-9]+)") || s.endsWith(".") || s.isEmpty();

	private final EditBox textField;

	public InputOptionValue(String optionName, T value, Consumer<T> setter, Predicate<String> validator) {
		super(optionName, setter);

		this.value = value;
		this.textField = new WatchedTextfield(this, client.font, 0, 0, 98, 18);
		textField.setValue(String.valueOf(value));
		textField.setFilter(validator);
	}

	@Override
	protected void drawValue(PoseStack matrixStack, int entryWidth, int entryHeight, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks) {
		textField.setX(x);
		textField.setY(y + entryHeight / 6);
		textField.render(matrixStack, mouseX, mouseY, partialTicks);
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

	private static class WatchedTextfield extends EditBox {
		private final InputOptionValue<?> watcher;

		public WatchedTextfield(InputOptionValue<?> watcher, Font fontRenderer, int x, int y, int width, int height) {
			super(fontRenderer, x, y, width, height, Component.literal(""));

			this.watcher = watcher;
		}

		@Override
		public void insertText(String string) {
			super.insertText(string);
			watcher.setValue(getValue());
		}

		@Override
		public void setValue(String value) {
			super.setValue(value);
			watcher.setValue(getValue());
		}

		@Override
		public void deleteWords(int count) {
			super.deleteWords(count);
			watcher.setValue(getValue());
		}

		@Override
		public void setCursorPosition(int pos) {
			super.setCursorPosition(pos);
			watcher.setValue(getValue());
		}
	}
}
