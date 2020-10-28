package mcp.mobius.waila.gui.config.value;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;

public class OptionsEntryValueBoolean extends OptionsEntryValue<Boolean> {

    private final Button button;

    public OptionsEntryValueBoolean(String optionName, boolean value, Consumer<Boolean> save) {
        super(optionName, save);

        this.button = new Button(0, 0, 100, 20, new TranslationTextComponent("gui." + (value ? "yes" : "no")), w -> {
            this.value = !this.value;
        });
        this.value = value;
    }

    @Override
    protected void drawValue(MatrixStack matrixStack, int entryWidth, int entryHeight, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks) {
        this.button.x = x + 135;
        this.button.y = y + entryHeight / 6;
        this.button.setMessage(new TranslationTextComponent("gui." + (value ? "yes" : "no")));
        this.button.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public IGuiEventListener getListener() {
        return button;
    }
}
