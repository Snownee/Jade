package snownee.jade.impl.ui;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.TextElement;
import snownee.jade.overlay.DisplayHelper;

public class TextElementImpl extends TextElement {

	protected final FormattedText text;
	protected float scale = 1;

	public TextElementImpl(Component component) {
		this((FormattedText) component);
	}

	public TextElementImpl(FormattedText text) {
		this.text = text;
		width = Math.max(DisplayHelper.font().width(text), 0);
		height = DisplayHelper.font().lineHeight - 1;
	}

	@Override
	public TextElement scale(float scale) {
		this.scale = scale;
		width = Math.max(Math.round(DisplayHelper.font().width(text) * scale), 0);
		height = Math.round(DisplayHelper.font().lineHeight * scale) - 1;
		return this;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (scale == 1) {
			DisplayHelper.INSTANCE.drawText(graphics, text, getX(), getY(), IThemeHelper.get().getNormalColor());
		} else {
			Matrix3x2fStack matrixStack = graphics.pose();
			matrixStack.pushMatrix();
			matrixStack.translate(getX(), getY() + scale);
			matrixStack.scale(scale);
			DisplayHelper.INSTANCE.drawText(graphics, text, 0, 0, IThemeHelper.get().getNormalColor());
			matrixStack.popMatrix();
		}
	}

	@Override
	public Component getNarration() {
		return Component.literal(getString());
	}

	@Override
	public String getString() {
		return text.getString();
	}
}
