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
	public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTime) {
		if (shouldRenderTitle()) {
			guiGraphics.drawString(client.font, title, getContentX() + 10, getContentYMiddle() - (client.font.lineHeight / 2), 0xFFFFFFFF);
		}
		super.renderContent(guiGraphics, mouseX, mouseY, hovered, deltaTime);
	}

	protected boolean shouldRenderTitle() {
		return true;
	}

}
