package snownee.jade.impl.ui;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.NarratableComponent;
import snownee.jade.api.ui.TextElement;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.JadeLanguages;

public class TextElementImpl extends TextElement {

	protected final Component text;
	protected float scale = 1;
	protected float alpha = 1;
	private int textWidth;

	public TextElementImpl(Component text) {
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
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		int x = textLeft();
		int normalColor = IWailaConfig.Overlay.applyAlpha(IThemeHelper.get().getNormalColor(), alpha);
		boolean scaled = scale != 1;
		Matrix3x2fStack matrixStack = graphics.pose();
		if (scaled) {
			matrixStack.pushMatrix();
			matrixStack.translate(x, getY() + scale);
			matrixStack.scale(scale);
			DisplayHelper.INSTANCE.drawText(graphics, text, 0, 0, normalColor);
		} else {
			DisplayHelper.INSTANCE.drawText(graphics, text, x, getY(), normalColor);
		}
		if (mouseX != -1 && getRectangle().containsPoint(mouseX, mouseY)) {
//			int highlightColor = IThemeHelper.get().theme().text.colors().info();
			ActiveTextCollector collector = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
			textCollector(collector);
		}
		if (scaled) {
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

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		Minecraft mc = Minecraft.getInstance();
		Screen screen = mc.gui.screen();
		if (screen != null) {
			ActiveTextCollector.ClickableStyleFinder collector = new ActiveTextCollector.ClickableStyleFinder(
					mc.font,
					(int) event.x(),
					(int) event.y());
			textCollector(collector);
			Style style = collector.result();
			if (style != null && style.getClickEvent() != null) {
				Screen.defaultHandleGameClickEvent(style.getClickEvent(), mc, screen);
				return true;
			}
		}
		return false;
	}

	private void textCollector(ActiveTextCollector collector) {
		int x = textLeft();
		int y = getY();
		if (scale != 1) {
			x = y = 0;
		}
		collector.defaultParameters(collector.defaultParameters().withOpacity(0));
		collector.accept(x, y, text);
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
