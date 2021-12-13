package mcp.mobius.waila.gui.config.value;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.gui.config.OptionsListWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class OptionValue<T> extends OptionsListWidget.Entry {

	private final Component title;
	private final String description;
	protected final Consumer<T> setter;
	protected T value;
	private int x;

	public OptionValue(String optionName, Consumer<T> setter) {
		this.title = makeTitle(optionName);
		this.description = makeKey(optionName + "_desc");
		this.setter = setter;
	}

	@Override
	public final void render(PoseStack matrixStack, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime) {
		client.font.drawShadow(matrixStack, title, rowLeft + 10, rowTop + (height / 4) + (client.font.lineHeight / 2), 16777215);
		drawValue(matrixStack, width, height, rowLeft + width - 110, rowTop, mouseX, mouseY, hovered, deltaTime);
		this.x = rowLeft;
	}

	public void save() {
		setter.accept(value);
	}

	public Component getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public int getX() {
		return x;
	}

	@Override
	public void updateNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, getTitle());
		if (I18n.exists(getDescription())) {
			output.add(NarratedElementType.HINT, new TranslatableComponent(getDescription()));
		}
	}

	protected abstract void drawValue(PoseStack matrixStack, int entryWidth, int entryHeight, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks);
}
