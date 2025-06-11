package snownee.jade.impl.ui;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.NarratableComponent;
import snownee.jade.api.ui.TextElement;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.JadeLanguages;

public class TextElementImpl extends TextElement {

	protected final FormattedText text;
	protected float scale = 1;
	private int textWidth;

	public TextElementImpl(Component component) {
		this((FormattedText) component);
	}

	public TextElementImpl(FormattedText text) {
		this.text = text;
		width = textWidth = Math.max(DisplayHelper.font().width(text), 0);
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
		int x = getX();
		if (JadeLanguages.INSTANCE.isRTL()) {
			x += width - textWidth;
		}
		if (scale == 1) {
			DisplayHelper.INSTANCE.drawText(graphics, text, x, getY(), IThemeHelper.get().getNormalColor());
		} else {
			Matrix3x2fStack matrixStack = graphics.pose();
			matrixStack.pushMatrix();
			matrixStack.translate(x, getY() + scale);
			matrixStack.scale(scale);
			DisplayHelper.INSTANCE.drawText(graphics, text, 0, 0, IThemeHelper.get().getNormalColor());
			matrixStack.popMatrix();
		}
	}

	@Override
	public Component getNarration() {
		return NarratableComponent.getNarration(text);
	}

	@Override
	public String getString() {
		return text.getString();
	}

	@Override
	public void setFreeSpace(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
