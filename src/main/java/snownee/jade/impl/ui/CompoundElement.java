package snownee.jade.impl.ui;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import snownee.jade.api.ui.Element;

public class CompoundElement extends Element {

	protected final Element large;
	protected final Element small;

	public CompoundElement(Element large, Element small) {
		this.large = large;
		this.small = small;
		width = large.getWidth();
		height = large.getHeight();
	}

	@Override
	public @Nullable Component getNarration() {
		return large.getNarration();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		large.render(graphics, mouseX, mouseY, partialTicks);
		graphics.pose().pushMatrix();
		small.render(graphics, mouseX, mouseY, partialTicks);
		graphics.pose().popMatrix();
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		large.setX(x);
		small.setX(x + large.getWidth() - small.getWidth());
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		large.setY(y);
		small.setY(y + large.getHeight() - small.getHeight());
	}
}
