package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Color;
import snownee.jade.api.ui.ResizeableElement;
import snownee.jade.overlay.DisplayHelper;

public class HorizontalLineElement extends ResizeableElement {

	public int color = IThemeHelper.get().getNormalColor();

	public HorizontalLineElement() {
		width = 10;
		height = 4;
	}

	@Override
	public @Nullable Component getNarration() {
		return null;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = getX();
		int y = getY() + height / 2;
		DisplayHelper.fill(graphics, x + 2, y, width, y + 0.5F, color);
		if (IThemeHelper.get().theme().text.shadow()) {
			++x;
			++y; //FIXME
			var shadow = Color.rgb(color);
			shadow = Color.rgb(shadow.getRed() / 4, shadow.getGreen() / 4, shadow.getBlue() / 4, shadow.getOpacity());
			DisplayHelper.fill(graphics, x + 2, y, width, y + 0.5F, shadow.toInt());
		}
	}

	@Override
	public void setFreeSpace(int width, int height) {
		this.width = width;
	}
}
