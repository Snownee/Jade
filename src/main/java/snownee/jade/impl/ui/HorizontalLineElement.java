package snownee.jade.impl.ui;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
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
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		int x = getX();
		int y = getY() + height / 2;
		Theme theme = IThemeHelper.get().theme();
		if (theme.text.shadow() && !theme.lightColorScheme) {
			var shadow = Color.rgb(color);
			shadow = Color.rgb(shadow.getRed() / 4, shadow.getGreen() / 4, shadow.getBlue() / 4, shadow.getOpacity());
			DisplayHelper.fill(graphics, x + 2.5F, y + 0.5F, x + width - 1.5F, y + 1F, shadow.toInt());

		}
		DisplayHelper.fill(graphics, x + 2, y, x + width - 2, y + 0.5F, color);
	}

	@Override
	public void setFreeSpace(int width, int height) {
		this.width = width;
	}
}
