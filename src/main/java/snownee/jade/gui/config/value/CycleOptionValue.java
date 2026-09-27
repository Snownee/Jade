package snownee.jade.gui.config.value;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.components.CycleButton;

public class CycleOptionValue<T> extends OptionValue<T> {

	public final CycleButton<T> button;

	public CycleOptionValue(String namespace, String optionName, boolean inline, CycleButton.Builder<T> cycleBtn, Supplier<T> getter, Consumer<T> setter) {
		super(namespace, optionName, getter, setter);
		this.inlineWidget = inline;
		if (inline) {
			setTitleMaxRows(2);
		}
		this.button = cycleBtn.displayOnlyValue().create(
				0, 0, 100, 20, title(), (btn, v) -> {
					this.value = v;
					save();
				});
		button.setValue(value = getter.get());
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
