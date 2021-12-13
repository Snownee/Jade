package mcp.mobius.waila.gui.config.value;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class CycleOptionValue<T> extends OptionValue<T> {

	private final Button button;
	private final List<Component> names;
	private final List<T> values;

	public CycleOptionValue(String optionName, List<Component> names, List<T> values, T value, Consumer<T> setter) {
		super(optionName, setter);
		this.names = names;
		this.values = values;
		this.button = new Button(0, 0, 100, 20, TextComponent.EMPTY, w -> {
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
	protected void drawValue(PoseStack matrixStack, int entryWidth, int entryHeight, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks) {
		this.button.x = x;
		this.button.y = y + entryHeight / 6;
		this.button.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public AbstractWidget getListener() {
		return button;
	}
}
