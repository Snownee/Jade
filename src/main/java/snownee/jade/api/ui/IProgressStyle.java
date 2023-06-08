package snownee.jade.api.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface IProgressStyle {

	default IProgressStyle color(int color) {
		return color(color, color);
	}

	IProgressStyle color(int color, int color2);

	IProgressStyle textColor(int color);

	IProgressStyle vertical(boolean vertical);

	IProgressStyle overlay(IElement overlay);

	void render(GuiGraphics guiGraphics, float x, float y, float w, float h, float progress, Component text);
}
