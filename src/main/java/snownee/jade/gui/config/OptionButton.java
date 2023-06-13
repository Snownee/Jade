package snownee.jade.gui.config;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class OptionButton extends OptionsList.Entry {

	protected final Component title;
	protected Button button;

	public OptionButton(String titleKey, Button button) {
		this(makeTitle(titleKey), button);
	}

	public OptionButton(Component title, Button button) {
		this.title = title;
		this.button = button;
		if (button != null && button.getMessage().getString().isEmpty()) {
			button.setMessage(title);
		}
	}

	@Override
	public AbstractWidget getListener() {
		return button;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime) {
		guiGraphics.drawString(client.font, title, rowLeft + 10, rowTop + (height / 2) - (client.font.lineHeight / 2), 16777215);
		button.setX(rowLeft + width - 110);
		button.setY(rowTop + height / 2 - button.getHeight() / 2);
		button.render(guiGraphics, mouseX, mouseY, deltaTime);
	}

	@Override
	public List<String> getMessages() {
		return List.of(title.getString(), button.getMessage().getString());
	}

	@Override
	public List<? extends AbstractWidget> children() {
		return Lists.newArrayList(button);
	}

}
