package snownee.jade.gui.config.value;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationThunk;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.gui.config.OptionsList;

public abstract class OptionValue<T> extends OptionsList.Entry {

	private static final Component SERVER_FEATURE = Component.literal("* ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
			.withHoverEvent(new HoverEvent.ShowText(Component.translatable("gui.jade.server_feature"))));
	protected final Supplier<T> getter;
	protected final Consumer<T> setter;
	protected @Nullable Identifier id;
	protected T value;
	protected int indent;
	private Component rawTitle;

	public OptionValue(String optionName, Supplier<T> getter, Consumer<T> setter) {
		super(makeTitle(optionName));
		this.getter = getter;
		this.setter = setter;
		rawTitle = title();
		addMessageKey(optionName);
		String key = makeKey(optionName + "_desc");
		if (JadeUI.hasTranslation(key)) {
			appendDescription(Component.translatable(key));
		}
	}

	@Override
	public void setDisabled(boolean disabled) {
		super.setDisabled(disabled);
		if (disabled) {
			setTitle(rawTitle.copy().withStyle(ChatFormatting.GRAY));
		} else {
			setTitle(rawTitle);
		}
	}

	public void save() {
		setter.accept(value);
	}

	@Override
	public int getTextX() {
		return indent + 10;
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

	public abstract void updateValue();

	public void setId(Identifier id) {
		this.id = id;
	}

	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public List<Component> getDescriptionOnShift() {
		if (id == null) {
			return List.of();
		}
		return List.of(Component.literal(id.toString()).withStyle(ChatFormatting.GRAY));
	}

	public void setServerFeature() {
		setTitle(title().copy().append(SERVER_FEATURE));
	}
}
