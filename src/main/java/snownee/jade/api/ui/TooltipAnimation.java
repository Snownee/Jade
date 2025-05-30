package snownee.jade.api.ui;

import net.minecraft.client.renderer.Rect2i;

public class TooltipAnimation {
	public final Rect2i expectedRect = new Rect2i(0, 0, 0, 0);
	public final Rect2i rect = new Rect2i(0, 0, 0, 0); //TODO rename to roundedRect
	//	public final Rect2f rect = new Rect2f(0, 0, 0, 0);
	public float scale = 1;
	public float alpha;
}
