package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.ITextElement;
import snownee.jade.overlay.DisplayHelper;

public class TextElement extends Element implements ITextElement {

	public final FormattedText text;

	public TextElement(Component component) {
		this.text = component;
	}

	public TextElement(FormattedText text) {
		this.text = text;
	}

	@Override
	public Vec2 getSize() {
		return new Vec2(Math.max(DisplayHelper.font().width(text), 0), DisplayHelper.font().lineHeight - 1);
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		DisplayHelper.INSTANCE.drawText(guiGraphics, text, x, y, IThemeHelper.get().getNormalColor());
	}

	@Override
	public @Nullable String getMessage() {
		return text.getString();
	}

	public SpecialTextElement toSpecial() {
		return new SpecialTextElement(text);
	}

	@Override
	public ITextElement scale(float scale) {
		return toSpecial().scale(scale);
	}

	@Override
	public ITextElement zOffset(int zOffset) {
		return toSpecial().zOffset(zOffset);
	}
}
