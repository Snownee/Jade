package snownee.jade.gui.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.InputType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import snownee.jade.Jade;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.gui.WailaConfigScreen;
import snownee.jade.gui.config.value.CycleOptionValue;
import snownee.jade.gui.config.value.InputOptionValue;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.gui.config.value.SliderOptionValue;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.SmoothChasingValue;

public class OptionsList extends ContainerObjectSelectionList<OptionsList.Entry> {

	public static final Component OPTION_ON = CommonComponents.OPTION_ON.copy().withColor(0xFFB9F6CA);
	public static final Component OPTION_OFF = CommonComponents.OPTION_OFF.copy().withColor(0xFFFF8A80);
	public final Set<OptionsList.Entry> forcePreview = Sets.newIdentityHashSet();
	protected final List<Entry> entries = Lists.newArrayList();
	private final @Nullable Runnable diskWriter;
	public @Nullable Title currentTitle;
	public @Nullable OptionValue<?> invalidEntry;
	public @Nullable KeyMapping selectedKey;
	private final BaseOptionsScreen owner;
	private final SmoothChasingValue smoothScroll;
	private @Nullable Entry defaultParent;

	public OptionsList(
			BaseOptionsScreen owner,
			Minecraft client,
			int x,
			int y,
			int width,
			int height,
			int entryHeight,
			@Nullable Runnable diskWriter) {
		super(client, width, height, y, entryHeight);
		setX(x);
		this.owner = owner;
		this.diskWriter = diskWriter;
		smoothScroll = new SmoothChasingValue().withSpeed(0.6F);
	}

	public OptionsList(BaseOptionsScreen owner, Minecraft client, int x, int y, int width, int height, int entryHeight) {
		this(owner, client, x, y, width, height, entryHeight, null);
	}

	private static void walkChildren(Entry entry, Consumer<Entry> consumer) {
		consumer.accept(entry);
		for (Entry child : entry.children) {
			walkChildren(child, consumer);
		}
	}

	@Override
	public int getRowWidth() {
		return Math.min(width, 300);
	}

	//TODO: check if it is still needed
	@Override
	protected int scrollBarX() {
		return owner.width - 6;
	}

	@Override
	public void setScrollAmount(double scroll) {
		if (ClientProxy.metadata.hasSmoothScroll()) {
			super.setScrollAmount(scroll);
		} else {
			smoothScroll.target(Mth.clamp((float) scroll, 0, maxScrollAmount()));
		}
	}

	public void forceSetScrollAmount(double scroll) {
		smoothScroll.start((float) scroll);
		super.setScrollAmount(scroll);
	}

	@Override
	protected double scrollRate() {
		return defaultEntryHeight * (!ClientProxy.metadata.hasFastScroll() && JadeUI.hasControlDown() ? 4.5 : 1.5);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
		smoothScroll.value = smoothScroll.getTarget();
		super.setScrollAmount(smoothScroll.value);
		return super.mouseDragged(mouseButtonEvent, d, e);
	}

	@Override
	public boolean isFocused() {
		return owner.getFocused() == this;
	}

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent event) {
		OptionsNav.Entry navEntry = owner.optionsNav().getFocused();
		if (navEntry != null && event instanceof FocusNavigationEvent.ArrowNavigation(ScreenDirection direction) &&
				direction == ScreenDirection.RIGHT) {
			Title title = navEntry.getTitle();
			setFocused(title);
			ComponentPath path = super.nextFocusPath(new FocusNavigationEvent.ArrowNavigation(ScreenDirection.DOWN));
			setFocused(null);
			return path;
		}
		return super.nextFocusPath(event);
	}

	// public-access it
	@Override
	public void scrollToEntry(Entry entry) {
		super.scrollToEntry(entry);
	}

	@Override
	protected boolean entriesCanBeSelected() {
		return !PreviewOptionsScreen.isAdjustingPosition();
	}

	@Override
	protected void renderListSeparators(GuiGraphics guiGraphics) {
		Identifier Identifier2 = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier2, 0, this.getBottom(), 0.0F, 0.0F, owner.width, 2, 32, 2);
	}

	@Override
	protected void renderSelection(GuiGraphics guiGraphics, Entry entry, int i) {
		int outlineX0 = getX();
		int outlineY0 = entry.getY();
		int outlineX1 = outlineX0 + getWidth();
		int outlineY1 = outlineY0 + entry.getHeight();
		guiGraphics.fill(outlineX0, outlineY0, outlineX1, outlineY1, 0x33FFFFFF);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		float deltaTicks = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
		smoothScroll.tick(deltaTicks);
		if (!ClientProxy.metadata.hasSmoothScroll()) {
			super.setScrollAmount(Math.round(smoothScroll.value));
		}
		hovered = null;
		if (!PreviewOptionsScreen.isAdjustingPosition()) {
			InputType lastInputType = minecraft.getLastInputType();
			mouseY = Math.min(mouseY, getRowRight());
			if (lastInputType.isMouse() && isMouseOver(mouseX, mouseY)) {
				hovered = getEntryAtPosition(mouseX, mouseY);
			} else if (lastInputType.isKeyboard() && getFocused() != null) {
				hovered = getFocused();
			}
			if (hovered instanceof Title title) {
				setSelected(null);
				currentTitle = title;
			} else {
				setSelected(hovered);
				if (hovered != null && hovered.root() instanceof Title title) {
					currentTitle = title;
				}
			}
		}

		enableScissor(guiGraphics);
		renderListItems(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.disableScissor();
		renderListSeparators(guiGraphics);
		renderScrollbar(guiGraphics, mouseX, mouseY);
	}

	public void save() {
		children().stream().filter(e -> e instanceof OptionValue).map(e -> (OptionValue<?>) e).forEach(OptionValue::save);
		if (diskWriter != null) {
			diskWriter.run();
		}
	}

	public <T extends Entry> T add(T entry) {
		entries.add(entry);
		if (entry instanceof Title) {
			setDefaultParent(entry);
		} else if (defaultParent != null) {
			entry.parent(defaultParent);
		}
		return entry;
	}

	@Nullable
	public Entry getEntryAt(double x, double y) {
		return getEntryAtPosition(x, y);
	}

	@Override
	public int getRowTop(int i) {
		return super.getRowTop(i);
	}

	@Override
	public int getRowBottom(int i) {
		return super.getRowBottom(i);
	}

	public void setDefaultParent(Entry defaultParent) {
		this.defaultParent = defaultParent;
	}

	public MutableComponent title(String string) {
		return add(new Title(string)).getTitle();
	}

	public OptionValue<Float> slider(String optionName, Supplier<Float> getter, Consumer<Float> setter) {
		return slider(optionName, getter, setter, 0, 1, FloatUnaryOperator.identity());
	}

	public OptionValue<Float> slider(
			String optionName,
			Supplier<Float> getter,
			Consumer<Float> setter,
			float min,
			float max,
			FloatUnaryOperator aligner) {
		return add(new SliderOptionValue(optionName, getter, setter, min, max, aligner));
	}

	public <T> OptionValue<T> input(String optionName, Supplier<T> getter, Consumer<T> setter, Predicate<String> validator) {
		return add(new InputOptionValue<>(this::updateSaveState, optionName, getter, setter, validator));
	}

	public <T> OptionValue<T> input(String optionName, Supplier<T> getter, Consumer<T> setter) {
		return input(optionName, getter, setter, Predicates.alwaysTrue());
	}

	public OptionValue<Boolean> choices(String optionName, Supplier<Boolean> getter, BooleanConsumer setter) {
		return choices(optionName, getter, setter, null);
	}

	public OptionValue<Boolean> choices(
			String optionName,
			Supplier<Boolean> getter,
			BooleanConsumer setter,
			@Nullable Consumer<CycleButton.Builder<Boolean>> builderConsumer) {
		CycleButton.Builder<Boolean> builder = CycleButton.booleanBuilder(OPTION_ON, OPTION_OFF, getter.get());
		if (builderConsumer != null) {
			builderConsumer.accept(builder);
		}
		return add(new CycleOptionValue<>(optionName, builder, getter, setter));
	}

	public <T extends Enum<T>> OptionValue<T> choices(String optionName, Supplier<T> getter, Consumer<T> setter) {
		return choices(optionName, getter, setter, null);
	}

	public <T extends Enum<T>> OptionValue<T> choices(
			String optionName,
			Supplier<T> getter,
			Consumer<T> setter,
			@Nullable Consumer<CycleButton.Builder<T>> builderConsumer) {
		List<T> values = Arrays.asList(getter.get().getDeclaringClass().getEnumConstants());
		CycleButton.Builder<T> builder = CycleButton.builder(
				v -> {
					String name = v.name().toLowerCase(Locale.ENGLISH);
					return switch (name) {
						case "on" -> OPTION_ON;
						case "off" -> OPTION_OFF;
						default -> Entry.makeTitle(optionName + "_" + name);
					};
				},
				getter.get()).withValues(values);
		builder.withTooltip(v -> {
			String key = OptionsList.Entry.makeKey(optionName + "_" + v.name().toLowerCase(Locale.ENGLISH) + "_desc");
			if (!I18n.exists(key)) {
				return null;
			}
			return Tooltip.create(WailaConfigScreen.processBuiltInVariables(Component.translatable(key)));
		});
		if (builderConsumer != null) {
			builderConsumer.accept(builder);
		}
		return add(new CycleOptionValue<>(optionName, builder, getter, setter));
	}

	public <T> OptionValue<T> choices(
			String optionName,
			Supplier<T> getter,
			List<T> values,
			Consumer<T> setter,
			Function<T, Component> nameProvider) {
		return add(new CycleOptionValue<>(optionName, CycleButton.builder(nameProvider, getter.get()).withValues(values), getter, setter));
	}

	public void keybind(KeyMapping keybind) {
		add(new KeybindOptionButton(this, keybind));
	}

	public void removed() {
		forcePreview.clear();
		for (Entry entry : entries) {
			entry.parent = null;
			if (!entry.children.isEmpty()) {
				entry.children.clear();
			}
		}
		clearEntries();
	}

	public void updateSearch(String search) {
		clearEntries();
		if (search.isBlank()) {
			entries.forEach(this::addEntry);
			return;
		}
		Set<Entry> matches = Sets.newLinkedHashSet();
		String[] keywords = search.toLowerCase(Locale.ENGLISH).split("\\s+");
		for (Entry entry : entries) {
			int bingo = 0;
			List<String> messages = entry.getMessages();
			for (String keyword : keywords) {
				for (String message : messages) {
					if (message.contains(keyword)) {
						bingo++;
						break;
					}
				}
			}
			if (bingo == keywords.length) {
				walkChildren(entry, matches::add);
				while (entry.parent() != null) {
					entry = Objects.requireNonNull(entry.parent());
					matches.add(entry);
				}
			}
		}
		for (Entry entry : entries) {
			if (matches.contains(entry)) {
				addEntry(entry);
			}
		}
		if (matches.isEmpty()) {
			addEntry(new Title(Component.translatable("gui.jade.no_results").withStyle(ChatFormatting.GRAY)));
		}
	}

	public void updateSaveState() {
		invalidEntry = null;
		for (Entry entry : entries) {
			if (entry instanceof OptionValue<?> value && !value.isValidValue()) {
				invalidEntry = value;
				break;
			}
		}
		if (invalidEntry == null) {
			Objects.requireNonNull(owner.saveButton).setTooltip(null);
		} else {
			Objects.requireNonNull(owner.saveButton).setTooltip(Tooltip.create(Component.translatable("gui.jade.invalid_value_cant_save")));
		}
	}

	public void updateOptionValue(@Nullable Identifier key) {
		for (Entry entry : entries) {
			if (entry instanceof OptionValue<?> value && (key == null || key.equals(value.getId()))) {
				value.updateValue();
			}
		}
	}

	public void showOnTop(Entry entry) {
		setScrollAmount(defaultEntryHeight * children().indexOf(entry) + 1);
		if (entry instanceof Title title) {
			currentTitle = title;
		}
	}

	public void resetMappingAndUpdateButtons() {
		for (Entry entry : entries) {
			if (entry instanceof KeybindOptionButton button) {
				button.refresh(selectedKey);
			}
		}
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if (selectedKey != null) {
			if (keyEvent.isEscape()) {
				selectedKey.setKey(InputConstants.UNKNOWN);
			} else {
				selectedKey.setKey(InputConstants.getKey(keyEvent));
			}
			selectedKey = null;
			resetMappingAndUpdateButtons();
			return true;
		}
		return super.keyPressed(keyEvent);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
		if (selectedKey != null) {
			selectedKey.setKey(InputConstants.Type.MOUSE.getOrCreate(event.button()));
			this.selectedKey = null;
			resetMappingAndUpdateButtons();
			return false;
		}
		return super.mouseClicked(event, bl);
	}

	@Override
	public void setSelected(OptionsList.@Nullable Entry entry) {
		selected = entry;
		if (entry != null && minecraft.getLastInputType().isKeyboard()) {
			scrollToEntry(entry);
		}
	}

	public record EntryWidget(AbstractWidget widget, int offsetX, int offsetY, boolean floatRight) {}

	public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

		protected final Minecraft client;
		protected final List<String> messages = Lists.newArrayList();
		private final List<AbstractWidget> rawWidgets = Lists.newArrayList();
		protected final List<EntryWidget> widgets = Lists.newArrayList();
		protected List<Component> description = List.of();
		private @Nullable Entry parent;
		private List<Entry> children = List.of();

		public Entry() {
			client = Minecraft.getInstance();
		}

		public static MutableComponent makeTitle(String key) {
			return Component.translatable(makeKey(key));
		}

		public static String makeKey(String key) {
			return Util.makeDescriptionId("config", Identifier.fromNamespaceAndPath(Jade.ID, key));
		}

		public @Nullable AbstractWidget getFirstWidget() {
			return rawWidgets.isEmpty() ? null : rawWidgets.getFirst();
		}

		public void addWidget(AbstractWidget widget, int offsetX) {
			addWidget(new EntryWidget(widget, offsetX, -widget.getHeight() / 2, true));
		}

		public void addWidget(EntryWidget widget) {
			widgets.add(widget);
			rawWidgets.add(widget.widget());
		}

		@Override
		public List<? extends AbstractWidget> children() {
			return rawWidgets;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return children();
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTime) {
			for (EntryWidget widget : widgets) {
				AbstractWidget rawWidget = widget.widget();
				int x;
				if (widget.floatRight()) {
					x = getContentWidth() - 110 + widget.offsetX();
				} else {
					x = 10 + widget.offsetX();
				}
				rawWidget.setX(getContentX() + x);
				rawWidget.setY(getContentY() + getContentHeight() / 2 + widget.offsetY());
				rawWidget.render(guiGraphics, mouseX, mouseY, deltaTime);
			}
		}

		public void setDisabled(boolean disabled) {
			for (AbstractWidget widget : rawWidgets) {
				widget.active = !disabled;
				if (widget instanceof EditBox editBox) {
					editBox.setEditable(!disabled);
				}
			}
		}

		public List<Component> getDescription() {
			return description;
		}

		public List<Component> getDescriptionOnShift() {
			return List.of();
		}

		public int getTextX() {
			return 0;
		}

		public int getTextWidth() {
			return 0;
		}

		public Entry parent(Entry parent) {
			this.parent = parent;
			if (parent.children.isEmpty()) {
				parent.children = Lists.newArrayList();
			}
			parent.children.add(this);
			return this;
		}

		public @Nullable Entry parent() {
			return parent;
		}

		public Entry root() {
			Entry entry = this;
			while (entry.parent() != null) {
				entry = Objects.requireNonNull(entry.parent());
			}
			return entry;
		}

		public final List<String> getMessages() {
			return messages;
		}

		public void addMessage(String message) {
			messages.add(StringUtil.stripColor(message).toLowerCase(Locale.ENGLISH));
		}

		public void addMessageKey(String key) {
			key = makeKey(key + "_extra_msg");
			if (I18n.exists(key)) {
				addMessage(I18n.get(key));
			}
		}
	}

	public static class Title extends Entry {

		public Component narration;
		private final MutableComponent title;

		public Title(String key) {
			title = makeTitle(key);
			addMessageKey(key);
			addMessage(title.getString());
			key = makeKey(key + "_desc");
			if (I18n.exists(key)) {
				description = List.of(Component.translatable(key));
				addMessage(description.getFirst().getString());
			}
			narration = Component.translatable("narration.jade.category", title);
		}

		public Title(MutableComponent title) {
			this.title = title;
			narration = title;
		}

		public MutableComponent getTitle() {
			return title;
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTime) {
			guiGraphics.drawString(
					client.font,
					title,
					getTextX(),
					getContentY() + getContentHeight() - client.font.lineHeight,
					0xFFFFFFFF);
		}

		@Override
		public int getTextX() {
			return getContentXMiddle() - getTextWidth() / 2;
		}

		@Override
		public int getTextWidth() {
			return client.font.width(title);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of(new NarratableEntry() {

				@Override
				public NarratableEntry.NarrationPriority narrationPriority() {
					return NarratableEntry.NarrationPriority.HOVERED;
				}

				@Override
				public void updateNarration(NarrationElementOutput narrationElementOutput) {
					narrationElementOutput.add(NarratedElementType.TITLE, narration);
				}
			});
		}
	}

}
