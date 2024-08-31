package snownee.jade.gui.config.value;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationThunk;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.gui.config.OptionsList;

public abstract class OptionValue<T> extends OptionsList.Entry {

	private static final Component SERVER_FEATURE = Component.literal("*").withStyle(ChatFormatting.GRAY);
	protected final Supplier<T> getter;
	protected final Consumer<T> setter;
	private final Component title;
	protected ResourceLocation id;
	public boolean serverFeature;
	protected T value;
	protected int indent;
	private int x;

	public OptionValue(String optionName, Supplier<T> getter, Consumer<T> setter) {
		this.title = makeTitle(optionName);
		this.getter = getter;
		this.setter = setter;
		addMessage(title.getString());
		addMessageKey(optionName);
		String key = makeKey(optionName + "_desc");
		if (I18n.exists(key)) {
			appendDescription(Component.translatable(key));
		}
	}

	@Override
	public final void render(
			GuiGraphics guiGraphics,
			int index,
			int rowTop,
			int rowLeft,
			int width,
			int height,
			int mouseX,
			int mouseY,
			boolean hovered,
			float deltaTime) {
		AbstractWidget widget = getFirstWidget();
		Component title0 = widget.active ? title : title.copy().withStyle(ChatFormatting.STRIKETHROUGH, ChatFormatting.GRAY);
		int left = rowLeft + indent + 10;
		int top = rowTop + (height / 2) - (client.font.lineHeight / 2);
		guiGraphics.drawString(client.font, title0, left, top, 16777215);
		if (serverFeature) {
			guiGraphics.drawString(client.font, SERVER_FEATURE, left + getTextWidth() + 1, top, 16777215);
		}
		super.render(guiGraphics, index, rowTop, rowLeft, width, height, mouseX, mouseY, hovered, deltaTime);
		this.x = rowLeft;
	}

	public void save() {
		setter.accept(value);
	}

	public Component getTitle() {
		return title;
	}

	public void appendDescription(Component description) {
		if (this.description.isEmpty()) {
			this.description = Lists.newArrayList(description);
		} else {
			this.description.add(description);
		}
		addMessage(description.getString());
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
		super.updateNarration(output);
		if (!description.isEmpty()) {
			output.add(NarratedElementType.HINT, NarrationThunk.from(description));
		}
	}

	public boolean isValidValue() {
		return true;
	}

	@Override
	public OptionsList.Entry parent(OptionsList.Entry parent) {
		super.parent(parent);
		if (parent instanceof OptionValue) {
			indent = ((OptionValue<?>) parent).indent + 12;
		}
		return this;
	}

	public abstract void setValue(T value);

	public void setId(ResourceLocation id) {
		this.id = id;
	}

	@Override
	public List<Component> getDescriptionOnShift() {
		if (id == null) {
			return List.of();
		}
		return List.of(Component.literal(id.toString()).withStyle(ChatFormatting.GRAY));
	}
}
