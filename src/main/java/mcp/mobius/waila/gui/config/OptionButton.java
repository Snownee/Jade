package mcp.mobius.waila.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class OptionButton extends OptionsListWidget.Entry {

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
	public void render(PoseStack matrixStack, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime) {
		client.font.drawShadow(matrixStack, title.getString(), rowLeft + 10, rowTop + (height / 4) + (client.font.lineHeight / 2), 16777215);
		button.x = rowLeft + 135;
		button.y = rowTop + height / 6;
		button.render(matrixStack, mouseX, mouseY, deltaTime);
	}

	@Override
	public boolean mouseClicked(double mouseY, double mouseX, int button) {
		if (button == 0) {
			this.button.playDownSound(Minecraft.getInstance().getSoundManager());
			this.button.onPress();
			return true;
		}

		return false;
	}

	@Override
	public void updateNarration(NarrationElementOutput output) {
		button.updateNarration(output);
	}
}
