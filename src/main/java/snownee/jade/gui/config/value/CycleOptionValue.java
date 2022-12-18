package snownee.jade.gui.config.value;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;

public class CycleOptionValue<T> extends OptionValue<T> {

	private final CycleButton<T> button;

	public CycleOptionValue(String optionName, CycleButton.Builder<T> cycleBtn, T value, Consumer<T> setter) {
		super(optionName, setter);
		this.button = cycleBtn.displayOnlyValue().withInitialValue(value).create(0, 0, 100, 20, getTitle(), (btn, v) -> {
			this.value = v;
			save();
		});
		this.value = value;
	}

	@Override
	protected void drawValue(PoseStack matrixStack, int entryWidth, int entryHeight, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks) {
		this.button.setX(x);
		this.button.setY(y + entryHeight / 6);
		this.button.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public AbstractWidget getListener() {
		return button;
	}
}
