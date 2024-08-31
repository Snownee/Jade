package snownee.jade.gui.config.value;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.components.CycleButton;

public class CycleOptionValue<T> extends OptionValue<T> {

	private final CycleButton<T> button;

	public CycleOptionValue(String optionName, CycleButton.Builder<T> cycleBtn, Supplier<T> getter, Consumer<T> setter) {
		super(optionName, getter, setter);
		this.button = cycleBtn.displayOnlyValue().withInitialValue(value).create(0, 0, 100, 20, getTitle(), (btn, v) -> {
			this.value = v;
			save();
		});
		updateValue();
		addWidget(button, 0);
	}

	@Override
	public void setValue(T value) {
		button.onValueChange.onValueChange(button, value);
		updateValue();
	}

	@Override
	public void updateValue() {
		button.setValue(value = getter.get());
	}
}
