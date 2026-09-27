package snownee.jade.gui.config.value;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.components.CycleButton;
import snownee.jade.gui.ToggleButton;

public class BooleanOptionValue extends CycleOptionValue<Boolean> {

	public final ToggleButton toggle;

	public BooleanOptionValue(
			String namespace,
			String optionName,
			CycleButton.Builder<Boolean> cycleBtn,
			Supplier<Boolean> getter,
			Consumer<Boolean> setter) {
		super(namespace, optionName, true, cycleBtn, getter, setter);
		toggle = ToggleButton.create(0, 0, title(), getter.get(), (btn, v) -> {
			this.value = v;
			save();
		});
		toggle.visible = false;
		addWidget(toggle, 0);
	}

	@Override
	public void setCardMode(boolean cardMode) {
		super.setCardMode(cardMode);
		button.visible = !cardMode;
		toggle.visible = cardMode;
	}

	@Override
	public void updateValue() {
		super.updateValue();
		toggle.setValue(value);
	}
}
