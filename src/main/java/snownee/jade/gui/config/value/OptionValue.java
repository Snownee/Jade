package snownee.jade.gui.config.value;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import snownee.jade.gui.config.WailaOptionsList;

public abstract class OptionValue<T> extends WailaOptionsList.Entry {

	private final Component title;
	protected final Consumer<T> setter;
	protected T value;
	private int x;
	public int indent;

	public OptionValue(String optionName, Consumer<T> setter) {
		this.title = makeTitle(optionName);
		this.setter = setter;
		String key = makeKey(optionName + "_desc");
		if (I18n.exists(key))
			appendDescription(I18n.get(key));
	}

	@Override
	public final void render(PoseStack matrixStack, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime) {
		Component title0 = getListener().active ? title : title.copy().withStyle(ChatFormatting.STRIKETHROUGH, ChatFormatting.GRAY);
		client.font.drawShadow(matrixStack, title0, rowLeft + indent + 10, rowTop + (height / 4) + (client.font.lineHeight / 2), 16777215);
		drawValue(matrixStack, width, height, rowLeft + width - 110, rowTop, mouseX, mouseY, hovered, deltaTime);
		this.x = rowLeft;
	}

	public void save() {
		setter.accept(value);
	}

	public Component getTitle() {
		return title;
	}

	public void appendDescription(String description) {
		if (this.description == null)
			this.description = getTitle().getString();
		this.description += '\n' + description;
	}

	public int getX() {
		return x;
	}

	@Override
	public int getTextX(int width) {
		return getX() + indent + 10;
	}

	@Override
	public int getTextWidth() {
		return client.font.width(getTitle());
	}

	@Override
	public void updateNarration(NarrationElementOutput output) {
		getListener().updateNarration(output);
		if (!Strings.isNullOrEmpty(getDescription())) {
			output.add(NarratedElementType.HINT, Component.translatable(getDescription()));
		}
	}

	protected abstract void drawValue(PoseStack matrixStack, int entryWidth, int entryHeight, int x, int y, int mouseX, int mouseY, boolean selected, float partialTicks);

	@Override
	public List<? extends AbstractWidget> children() {
		return Lists.newArrayList(getListener());
	}
}
