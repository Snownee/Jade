package snownee.jade.impl.ui;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.NarratableComponent;
import snownee.jade.api.ui.TextElement;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.JadeLanguages;

public class TextElementImpl extends TextElement {

	protected final FormattedText text;
	protected float scale = 1;
	protected float alpha = 1;
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
	public TextElement alpha(float alpha) {
		this.alpha = alpha;
		return this;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = textLeft();
		int normalColor = IWailaConfig.Overlay.applyAlpha(IThemeHelper.get().getNormalColor(), alpha);
		if (scale == 1) {
			DisplayHelper.INSTANCE.drawText(graphics, text, x, getY(), normalColor);
		} else {
			Matrix3x2fStack matrixStack = graphics.pose();
			matrixStack.pushMatrix();
			matrixStack.translate(x, getY() + scale);
			matrixStack.scale(scale);
			DisplayHelper.INSTANCE.drawText(graphics, text, 0, 0, normalColor);
			matrixStack.popMatrix();
		}
		if (mouseX != -1 && getRectangle().containsPoint(mouseX, mouseY)) {
			//TODO scale
			Style style = DisplayHelper.font().getSplitter().componentStyleAtWidth(text, Mth.floor(mouseX - x));
			graphics.renderComponentHoverEffect(DisplayHelper.font(), style, mouseX, mouseY);
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

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		Screen screen = Minecraft.getInstance().screen;
		if (screen != null) {
			//TODO scale
			Style style = DisplayHelper.font().getSplitter().componentStyleAtWidth(text, Mth.floor(event.x() - textLeft()));
			if (style != null) {
				return screen.handleComponentClicked(style);
			}
		}
		return false;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= textLeft() && mouseX < textLeft() + textWidth && mouseY >= getY() && mouseY < getY() + height;
	}

	private int textLeft() {
		int x = getX();
		if (JadeLanguages.INSTANCE.isRTL()) {
			x += width - textWidth;
		}
		return x;
	}
}
