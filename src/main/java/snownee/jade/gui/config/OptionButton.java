package snownee.jade.gui.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class OptionButton extends OptionsList.Entry {

	protected Component title;

	public OptionButton(String titleKey, Button button) {
		this(makeTitle(titleKey), button);
	}

	public OptionButton(Component title, Button button) {
		this.title = title;
		addMessage(title.getString());
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

	@Override
	public void render(
			GuiGraphics guiGraphics,
			int index,
			int rowTop,
			int rowLeft,
			int width,
			int height,
			int mouseX,
			int mouseY,
			boolean hovered,
			float deltaTime) {
		guiGraphics.drawString(client.font, title, rowLeft + 10, rowTop + (height / 2) - (client.font.lineHeight / 2), 16777215);
		super.render(guiGraphics, index, rowTop, rowLeft, width, height, mouseX, mouseY, hovered, deltaTime);
	}

}
