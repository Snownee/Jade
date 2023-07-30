package snownee.jade.gui.config.value;

import java.util.function.Consumer;

import net.minecraft.client.gui.components.CycleButton;

public class CycleOptionValue<T> extends OptionValue<T> {

	private final CycleButton<T> button;

	public CycleOptionValue(String optionName, CycleButton.Builder<T> cycleBtn, T value, Consumer<T> setter) {
		super(optionName, setter);
		this.value = value;
		this.button = cycleBtn.displayOnlyValue().withInitialValue(value).create(0, 0, 100, 20, getTitle(), (btn, v) -> {
			this.value = v;
			save();
		});
		addWidget(button, 0);
	}

	@Override
	public void setValue(T value) {
		button.setValue(value);
	}
}
