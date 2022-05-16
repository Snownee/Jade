package snownee.jade.gui.config;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class OptionButton extends WailaOptionsList.Entry {

	private final Component title;
	private final Button button;

	public OptionButton(String titleKey, Button button) {
		this(makeTitle(titleKey), button);
	}

	public OptionButton(Component title, Button button) {
		this.title = title;
		this.button = button;
		button.setMessage(this.title);
	}

	@Override
	public AbstractWidget getListener() {
		return button;
	}

	@Override
	public void render(PoseStack matrixStack, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime) {
		client.font.drawShadow(matrixStack, title, rowLeft + 10, rowTop + (height / 4) + (client.font.lineHeight / 2), 16777215);
		button.x = rowLeft + width - 110;
		button.y = rowTop + height / 6;
		button.render(matrixStack, mouseX, mouseY, deltaTime);
	}

	@Override
	public List<? extends AbstractWidget> children() {
		return Lists.newArrayList(button);
	}

}
