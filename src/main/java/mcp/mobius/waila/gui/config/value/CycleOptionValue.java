package mcp.mobius.waila.gui.config.value;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class CycleOptionValue<T> extends OptionValue<T> {

	private final Button button;
	private final List<ITextComponent> names;
	private final List<T> values;

	public CycleOptionValue(String optionName, List<ITextComponent> names, List<T> values, T value, Consumer<T> setter) {
		super(optionName, setter);
		this.names = names;
		this.values = values;
		this.button = new Button(0, 0, 100, 20, StringTextComponent.EMPTY, w -> {
			this.value = values.get((values.indexOf(this.value) + 1) % values.size());
			updateName();
			save();
		});
		this.value = value;
		updateName();
	}

	private void updateName() {
		button.setMessage(names.get(values.indexOf(value)));
	}

	@Override
	protected void drawValue(MatrixStack matrixStack, int entryWidth, int entryHeight, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks) {
		this.button.x = x + 135;
		this.button.y = y + entryHeight / 6;
		this.button.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public IGuiEventListener getListener() {
		return button;
	}
}
