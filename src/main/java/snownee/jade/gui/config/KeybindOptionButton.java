package snownee.jade.gui.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class KeybindOptionButton extends OptionButton {

	private final KeyMapping keybind;

	public KeybindOptionButton(OptionsList owner, KeyMapping keybind) {
		super(Component.translatable(keybind.getName()), null);
		this.keybind = keybind;
		var button = Button.builder(keybind.getTranslatedKeyMessage(), b -> {
			owner.selectedKey = this.keybind;
			owner.resetMappingAndUpdateButtons();
		}).size(100, 20).createNarration(supplier -> {
			if (this.keybind.isUnbound()) {
				return Component.translatable("narrator.controls.unbound", title);
			}
			return Component.translatable("narrator.controls.bound", title, supplier.get());
		}).build();
		addWidget(button, 0);
	}

	public void refresh(KeyMapping selectedKey) {
		var button = getFirstWidget();
		if (selectedKey == keybind) {
			button.setMessage(Component.literal("> ").append(button.getMessage()
					.copy()
					.withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <").withStyle(ChatFormatting.YELLOW));
		} else {
			button.setMessage(keybind.getTranslatedKeyMessage());
		}
	}
}
