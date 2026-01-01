package snownee.jade.gui.config;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class OptionButton extends OptionsList.Entry {

	public OptionButton(String titleKey, @Nullable Button button) {
		this(makeTitle(titleKey), button);
	}

	public OptionButton(Component title, @Nullable Button button) {
		super(title);
		if (button != null) {
			if (button.getMessage().getString().isEmpty()) {
				button.setMessage(title);
			} else {
				addMessage(button.getMessage().getString());
			}
			addWidget(button, 0);
		}
	}

	public OptionButton(Component title, Button.Builder builder) {
		this(title, builder.createNarration($ -> CommonComponents.joinForNarration(title, $.get())).build());
	}

}
