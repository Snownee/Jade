package mcp.mobius.waila.gui.config.value;

import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.gui.config.OptionsListWidget;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.text.ITextComponent;

public abstract class OptionsEntryValue<T> extends OptionsListWidget.Entry {

	private final ITextComponent title;
	private final String description;
	protected final Consumer<T> save;
	protected T value;
	private int x;

	public OptionsEntryValue(String optionName, Consumer<T> save) {
		this.title = makeTitle(optionName);
		this.description = makeKey(optionName + "_desc");
		this.save = save;
	}

	@Override
	public final void render(MatrixStack matrixStack, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime) {
		client.fontRenderer.drawStringWithShadow(matrixStack, title.getString(), rowLeft + 10, rowTop + (height / 4) + (client.fontRenderer.FONT_HEIGHT / 2), 16777215);
		drawValue(matrixStack, width, height, rowLeft, rowTop, mouseX, mouseY, hovered, deltaTime);
		this.x = rowLeft;
	}

	public void save() {
		save.accept(value);
	}

	public IGuiEventListener getListener() {
		return null;
	}

	public ITextComponent getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public int getX() {
		return x;
	}

	protected abstract void drawValue(MatrixStack matrixStack, int entryWidth, int entryHeight, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks);
}
