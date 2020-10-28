package mcp.mobius.waila.gui.config.value;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class OptionsEntryValueInput<T> extends OptionsEntryValue<T> {

    public static final Predicate<String> ANY = s -> true;
    public static final Predicate<String> INTEGER = s -> s.matches("^[0-9]*$");
    public static final Predicate<String> FLOAT = s -> s.matches("[-+]?([0-9]*\\.[0-9]+|[0-9]+)") || s.endsWith(".") || s.isEmpty();

    private final TextFieldWidget textField;

    public OptionsEntryValueInput(String optionName, T value, Consumer<T> save, Predicate<String> validator) {
        super(optionName, save);

        this.value = value;
        this.textField = new WatchedTextfield(this, client.fontRenderer, 0, 0, 98, 18);
        textField.setText(String.valueOf(value));
        textField.setValidator(validator);
    }

    public OptionsEntryValueInput(String optionName, T value, Consumer<T> save) {
        this(optionName, value, save, s -> true);
    }

    @Override
    protected void drawValue(MatrixStack matrixStack, int entryWidth, int entryHeight, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks) {
        textField.x = x + 135;
        textField.y = y + entryHeight / 6;
        textField.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public IGuiEventListener getListener() {
        return textField;
    }

    @SuppressWarnings("unchecked")
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
    }

    private static class WatchedTextfield extends TextFieldWidget {
        private final OptionsEntryValueInput<?> watcher;

        public WatchedTextfield(OptionsEntryValueInput<?> watcher, FontRenderer fontRenderer, int x, int y, int width, int height) {
            super(fontRenderer, x, y, width, height, new StringTextComponent(""));

            this.watcher = watcher;
        }

        @Override
        public void writeText(String string) {
            super.writeText(string);
            watcher.setValue(getText());
        }

        @Override
        public void setText(String value) {
            super.setText(value);
            watcher.setValue(getText());
        }

        @Override
        public void deleteWords(int count) {
            super.deleteWords(count);
            watcher.setValue(getText());
        }
    }
}
