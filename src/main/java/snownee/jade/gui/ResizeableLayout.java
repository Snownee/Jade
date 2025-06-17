package snownee.jade.gui;

import net.minecraft.client.gui.layouts.LayoutElement;

public interface ResizeableLayout extends LayoutElement {
	void setFreeSpace(int width, int height);

	void setFlexGrow(int flexGrow);

	int getFlexGrow();
}
