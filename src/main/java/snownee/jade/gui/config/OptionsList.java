package snownee.jade.gui.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.config.ConfigIcon;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.gui.JadeMultiLineTextWidget;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.gui.SmoothScrollableList;
import snownee.jade.gui.config.value.BooleanOptionValue;
import snownee.jade.gui.config.value.CycleOptionValue;
import snownee.jade.gui.config.value.InputOptionValue;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.gui.config.value.SliderOptionValue;
import snownee.jade.util.ItemStacks;

public class OptionsList extends SmoothScrollableList<OptionsList.Entry> {

	public enum DisplayMode {
		LIST,
		CARD
	}

	public static final String DEFAULT_NAMESPACE = "jade";
	public static final int CARD_GAP = 8;
	public static final int CARD_HEIGHT = 42;
	public static final int CARD_HEIGHT_TALL = 46;
	public static final int CARD_BLEED = 3;
	public static final int CARD_SHADOW = 4;
	public static final int CARD_ICON_SIZE = 16;
	public static final int CARD_ARROW_WIDTH = 15;
	public static final int CARD_ARROW_HEIGHT = 15;
	public static final int CARD_WIDGET_WIDTH = 29;
	public static final int MIN_CARD_WIDTH = 168;
	public static final int MAX_CARDS_PER_ROW = 3;
	public static final Component OPTION_ON = CommonComponents.OPTION_ON.copy().withColor(0xFFB9F6CA);
	public static final Component OPTION_OFF = CommonComponents.OPTION_OFF.copy().withColor(0xFFFF8A80);
	public final Set<OptionsList.Entry> forcePreview = Sets.newIdentityHashSet();
	protected final List<Entry> entries = Lists.newArrayList();
	private final BuiltInVariables builtInVariables = new BuiltInVariables();
	private final @Nullable Runnable diskWriter;
	public @Nullable Title currentTitle;
	public @Nullable OptionValue<?> invalidEntry;
	public @Nullable KeyMapping selectedKey;
	private final BaseOptionsScreen owner;
	private @Nullable Entry defaultParent;
	private String namespace = DEFAULT_NAMESPACE;
	private DisplayMode displayMode = DisplayMode.LIST;
	private int cardContentHeight;
	private final SecondaryPopup popup = new SecondaryPopup(this);

	public String namespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String makeKey(String key) {
		return Entry.makeKey(namespace, key);
	}

	public MutableComponent makeTitle(String key) {
		return Entry.makeTitle(namespace, key);
	}

	public BuiltInVariables builtInVariables() {
		return builtInVariables;
	}

	public DisplayMode displayMode() {
		return displayMode;
	}

	public void setDisplayMode(DisplayMode displayMode) {
		if (displayMode == DisplayMode.CARD && cardsPerRow() <= 1) {
			displayMode = DisplayMode.LIST;
		}
		if (this.displayMode == displayMode) {
			return;
		}
		this.displayMode = displayMode;
		layoutCards();
	}

	public int cardsPerRow() {
		return Math.max(1, Math.min(MAX_CARDS_PER_ROW, cardRowWidth() / MIN_CARD_WIDTH));
	}

	private int cardRowWidth() {
		return Math.min(width - 16, 900);
	}

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
		if (displayMode == DisplayMode.CARD) {
			return cardRowWidth();
		}
		return Math.min(width, 300);
	}

	@Override
	protected int contentHeight() {
		if (displayMode == DisplayMode.CARD) {
			return cardContentHeight;
		}
		return super.contentHeight();
	}

	@Override
	protected int addEntry(Entry entry) {
		int index = super.addEntry(entry);
		layoutCards();
		return index;
	}

	@Override
	public void setScrollAmount(double scroll) {
		super.setScrollAmount(scroll);
		layoutCards();
	}

	@Override
	public void forceSetScrollAmount(double scroll) {
		super.forceSetScrollAmount(scroll);
		layoutCards();
	}

	@Override
	public void updateSizeAndPosition(int width, int height, int x, int y) {
		super.updateSizeAndPosition(width, height, x, y);
		layoutCards();
	}

	void layoutCards() {
		if (displayMode != DisplayMode.CARD) {
			return;
		}
		int rowLeft = getRowLeft();
		int rowWidth = getRowWidth();
		int cardsPerRow = cardsPerRow();
		int cardWidth = (rowWidth - CARD_GAP * (cardsPerRow - 1)) / cardsPerRow;
		int contentTop = getY() + 2 + CARD_BLEED - (int) scrollAmount();
		int y = contentTop;
		List<Entry> row = Lists.newArrayListWithCapacity(cardsPerRow);
		for (Entry entry : children()) {
			if (entry.isSecondary()) {
				continue;
			}
			if (entry instanceof Title) {
				if (!row.isEmpty()) {
					y = placeCardRow(row, rowLeft, cardWidth, y);
					row.clear();
				}
				entry.setCardMode(false);
				entry.setX(rowLeft);
				entry.setWidth(rowWidth);
				entry.setHeight(defaultEntryHeight);
				entry.setY(y);
				y += defaultEntryHeight + CARD_GAP;
				continue;
			}
			entry.setCardMode(true);
			row.add(entry);
			if (row.size() >= cardsPerRow) {
				y = placeCardRow(row, rowLeft, cardWidth, y);
				row.clear();
			}
		}
		if (!row.isEmpty()) {
			y = placeCardRow(row, rowLeft, cardWidth, y);
		}
		layoutSecondaryPopup(rowLeft, rowWidth);
		cardContentHeight = y - contentTop + 4;
	}

	private void layoutSecondaryPopup(int rowLeft, int rowWidth) {
		Entry popupEntry = popup.effectiveEntry();
		List<Entry> options = popupEntry == null ? List.of() : popupEntry.secondaryOptions();
		for (Entry entry : children()) {
			if (entry.isSecondary() && !options.contains(entry)) {
				entry.setY(-100000);
				entry.setHeight(0);
			}
		}
		popup.setBounds(0, 0, 0, 0);
		if (popupEntry == null || options.isEmpty()) {
			return;
		}
		int padX = 0;
		int padY = 3;
		int panelWidth = Math.min(rowWidth, Math.max(popupEntry.getWidth(), 220));
		int panelX = Math.min(popupEntry.getX(), rowLeft + rowWidth - panelWidth);
		panelX = Math.max(panelX, rowLeft);
		int panelY = popupEntry.getY() + popupEntry.getHeight() + CARD_BLEED * 2;
		int panelHeight = padY * 2 + options.size() * defaultEntryHeight;
		int bottom = getBottom();
		if (panelY + panelHeight > bottom) {
			panelY = Math.max(getY(), bottom - panelHeight);
		}
		popup.setBounds(panelX, panelY, panelWidth, panelHeight);
		int itemY = panelY + padY + popup.slideOffset();
		for (Entry option : options) {
			option.setCardMode(false);
			option.setPopupMode(true);
			option.applyPopupWidgets();
			if (option instanceof OptionValue<?> value) {
				value.setIndent(0);
			}
			option.setX(panelX + padX);
			option.setWidth(panelWidth - padX * 2);
			option.setHeight(defaultEntryHeight);
			option.setY(itemY);
			itemY += defaultEntryHeight;
		}
	}

	private int placeCardRow(List<Entry> row, int rowLeft, int cardWidth, int y) {
		int height = 0;
		for (Entry entry : row) {
			height = Math.max(height, entry.cardHeight());
		}
		for (int i = 0; i < row.size(); i++) {
			Entry entry = row.get(i);
			entry.setX(rowLeft + i * (cardWidth + CARD_GAP));
			entry.setWidth(cardWidth);
			entry.setHeight(height);
			entry.setY(y);
		}
		return y + height + CARD_GAP;
	}

	//TODO: check if it is still needed
	@Override
	protected int scrollBarX() {
		return owner.width - 6;
	}

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent event) {
		if (event instanceof FocusNavigationEvent.TabNavigation tabNavigation) {
			return nextTabFocusPath(tabNavigation);
		}
		if (event instanceof FocusNavigationEvent.ArrowNavigation arrowNavigation && displayMode == DisplayMode.CARD) {
			Entry anchor = cardAnchor();
			if (anchor != null) {
				ComponentPath cardPath = nextCardFocusPath(anchor, arrowNavigation.direction());
				if (cardPath != null) {
					return cardPath;
				}
				if (arrowNavigation.direction() == ScreenDirection.LEFT) {
					OptionsNav.Entry navEntry = owner.optionsNav().getCurrentEntry();
					return navEntry == null ? null : ComponentPath.path(navEntry, owner.optionsNav());
				}
				return null;
			}
		}
		ComponentPath componentPath = super.nextFocusPath(event);
		OptionsNav.Entry navEntry = owner.optionsNav().getCurrentEntry();
		if (componentPath != null) {
			return componentPath;
		}
		if (navEntry != null && event instanceof FocusNavigationEvent.ArrowNavigation(ScreenDirection direction, ScreenRectangle _) &&
				direction == ScreenDirection.LEFT) {
			return ComponentPath.path(navEntry, owner.optionsNav());
		}
		return null;
	}

	@Nullable
	private ComponentPath nextTabFocusPath(FocusNavigationEvent.TabNavigation tabNavigation) {
		boolean forward = tabNavigation.forward();
		List<Entry> entryOrder = Lists.newArrayList();
		List<AbstractWidget> widgetOrder = Lists.newArrayList();
		for (Entry entry : children()) {
			for (AbstractWidget widget : entry.focusableWidgets()) {
				entryOrder.add(entry);
				widgetOrder.add(widget);
			}
		}
		if (entryOrder.isEmpty()) {
			return null;
		}
		Entry focusedEntry = getFocused();
		GuiEventListener focusedWidget = focusedEntry == null ? null : focusedEntry.getFocused();
		int current = -1;
		for (int i = 0; i < entryOrder.size(); i++) {
			if (entryOrder.get(i) == focusedEntry && widgetOrder.get(i) == focusedWidget) {
				current = i;
				break;
			}
		}
		int step = forward ? 1 : -1;
		int start;
		if (current >= 0) {
			start = current + step;
		} else if (focusedEntry != null) {
			int index = entryOrder.indexOf(focusedEntry);
			if (index < 0) {
				start = forward ? 0 : entryOrder.size() - 1;
			} else {
				start = forward ? index : index - 1;
			}
		} else {
			start = forward ? 0 : entryOrder.size() - 1;
		}
		if (start < 0 || start >= entryOrder.size()) {
			return null;
		}
		return ComponentPath.path(this, ComponentPath.path(entryOrder.get(start), ComponentPath.leaf(widgetOrder.get(start))));
	}

	@Nullable
	private Entry cardAnchor() {
		Entry focused = getFocused();
		if (focused == null) {
			return null;
		}
		Entry anchor = focused.isSecondary() ? focused.parent() : focused;
		return anchor != null && anchor.isCardMode() ? anchor : null;
	}

	@Nullable
	private ComponentPath nextCardFocusPath(Entry anchor, ScreenDirection direction) {
		List<Entry> cards = Lists.newArrayList();
		for (Entry entry : children()) {
			if (entry.isCardMode()) {
				cards.add(entry);
			}
		}
		int index = cards.indexOf(anchor);
		if (index < 0) {
			return null;
		}
		Entry target;
		if (direction.getAxis() == ScreenAxis.HORIZONTAL) {
			index += direction == ScreenDirection.RIGHT ? 1 : -1;
			if (index < 0 || index >= cards.size()) {
				return null;
			}
			target = cards.get(index);
		} else {
			List<List<Entry>> rows = Lists.newArrayList();
			int lastY = Integer.MIN_VALUE;
			for (Entry card : cards) {
				if (rows.isEmpty() || card.getY() != lastY) {
					rows.add(Lists.newArrayList());
					lastY = card.getY();
				}
				rows.getLast().add(card);
			}
			int row = -1;
			int column = -1;
			for (int i = 0; i < rows.size(); i++) {
				int c = rows.get(i).indexOf(anchor);
				if (c >= 0) {
					row = i;
					column = c;
					break;
				}
			}
			int step = direction == ScreenDirection.DOWN ? 1 : -1;
			target = null;
			for (int r = row + step; r >= 0 && r < rows.size(); r += step) {
				if (column < rows.get(r).size()) {
					target = rows.get(r).get(column);
					break;
				}
			}
			if (target == null) {
				return null;
			}
		}
		List<AbstractWidget> widgets = target.focusableWidgets();
		if (widgets.isEmpty()) {
			return null;
		}
		return ComponentPath.path(this, ComponentPath.path(target, ComponentPath.leaf(widgets.getFirst())));
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
	protected void extractListSeparators(GuiGraphicsExtractor guiGraphics) {
		Identifier Identifier2 = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier2, 0, this.getBottom(), 0.0F, 0.0F, owner.width, 2, 32, 2);
	}

	@Override
	protected void extractSelection(GuiGraphicsExtractor guiGraphics, Entry entry, int i) {
		if (displayMode == DisplayMode.CARD) {
			return;
		}
		int outlineX0 = getX();
		int outlineY0 = entry.getY();
		int outlineX1 = outlineX0 + getWidth();
		int outlineY1 = outlineY0 + entry.getHeight();
		guiGraphics.fill(outlineX0, outlineY0, outlineX1, outlineY1, 0x33FFFFFF);
	}

	@Override
	protected void extractListItems(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		Entry popupEntry = popup.effectiveEntry();
		if (displayMode != DisplayMode.CARD || popupEntry == null) {
			super.extractListItems(guiGraphics, mouseX, mouseY, partialTicks);
			return;
		}
		List<Entry> options = popupEntry.secondaryOptions();
		for (Entry child : children()) {
			if (options.contains(child)) {
				continue;
			}
			if (child.getY() + child.getHeight() >= getY() && child.getY() <= getBottom()) {
				extractItem(guiGraphics, mouseX, mouseY, partialTicks, child);
			}
		}
		if (!options.isEmpty() && popup.width() > 0) {
			int slideOffset = popup.slideOffset();
			guiGraphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					Identifier.fromNamespaceAndPath(namespace, "popup_background"),
					popup.x() - CARD_BLEED,
					popup.y() - CARD_BLEED + slideOffset,
					popup.width() + CARD_BLEED * 2,
					popup.height() + CARD_BLEED * 2,
					ARGB.white(popup.alpha()));
			for (Entry child : options) {
				if (child.getY() + child.getHeight() >= getY() && child.getY() <= getBottom()) {
					extractItem(guiGraphics, mouseX, mouseY, partialTicks, child);
				}
			}
		}
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		tickSmoothScroll();
		layoutCards();
		popup.commitPending();
		popup.tick();
		popup.applyAlpha();
		InputType lastInputType = minecraft.getLastInputType();
		Entry expandedEntry = popup.expanded();
		if (expandedEntry != null && !popup.sticky()) {
			Entry focused = getFocused();
			boolean keyboardFocusInside = lastInputType.isKeyboard() && focused != null &&
					(focused == expandedEntry || focused.parent() == expandedEntry);
			if (!keyboardFocusInside && !popup.isMouseOver(expandedEntry, mouseX, mouseY)) {
				popup.collapse();
			}
		}
		hovered = null;
		if (!PreviewOptionsScreen.isAdjustingPosition()) {
			mouseY = Math.min(mouseY, getRowRight());
			if (lastInputType.isMouse() && isMouseOver(mouseX, mouseY)) {
				if (expandedEntry != null && popup.width() > 0 && popup.isInside(mouseX, mouseY)) {
					for (Entry option : expandedEntry.secondaryOptions()) {
						if (option.isMouseOver(mouseX, mouseY)) {
							hovered = option;
							break;
						}
					}
				} else {
					hovered = getEntryAtPosition(mouseX, mouseY);
				}
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
		extractListItems(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.disableScissor();
		extractListSeparators(guiGraphics);
		extractScrollbar(guiGraphics, mouseX, mouseY);
	}

	public void save() {
		entries.stream().filter(e -> e instanceof OptionValue).map(e -> (OptionValue<?>) e).forEach(OptionValue::save);
		if (diskWriter != null) {
			diskWriter.run();
		}
	}

	public <T extends Entry> T add(T entry) {
		entry.setOptionsList(this);
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

	public Title title(String string) {
		return add(new Title(namespace, string));
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
		return add(new SliderOptionValue(namespace, optionName, getter, setter, min, max, aligner));
	}

	public <T> OptionValue<T> input(String optionName, Supplier<T> getter, Consumer<T> setter, Predicate<String> validator) {
		return add(new InputOptionValue<>(this::updateSaveState, namespace, optionName, getter, setter, validator));
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
		return add(new BooleanOptionValue(namespace, optionName, builder, getter, setter));
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
						default -> Entry.makeTitle(namespace, optionName + "_" + name);
					};
				}, getter.get()).withValues(values);
		builder.withTooltip(v -> {
			String key = makeKey(optionName + "_" + v.name().toLowerCase(Locale.ENGLISH) + "_desc");
			if (!JadeUI.hasTranslation(key)) {
				return null;
			}
			return Tooltip.create(builtInVariables.process(Component.translatable(key)));
		});
		if (builderConsumer != null) {
			builderConsumer.accept(builder);
		}
		return add(new CycleOptionValue<>(namespace, optionName, false, builder, getter, setter));
	}

	public <T> OptionValue<T> choices(
			String optionName,
			Supplier<T> getter,
			List<T> values,
			Consumer<T> setter,
			Function<T, Component> nameProvider) {
		return add(new CycleOptionValue<>(
				namespace,
				optionName,
				false,
				CycleButton.builder(nameProvider, getter.get()).withValues(values),
				getter,
				setter));
	}

	public KeybindOptionButton keybind(KeyMapping keybind) {
		return add(new KeybindOptionButton(this, keybind));
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
		popup.reset();
		if (displayMode == DisplayMode.CARD) {
			updateSearchCards(search);
			layoutCards();
			return;
		}
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
			addEntry(new Title(namespace, Component.translatable("gui.jade.no_results").withStyle(ChatFormatting.GRAY)));
		}
	}

	private void updateSearchCards(String search) {
		if (search.isBlank()) {
			for (Entry entry : entries) {
				if (entry.isSecondary()) {
					continue;
				}
				addEntry(entry);
				if (entry instanceof Title) {
					continue;
				}
				entry.secondaryOptions().forEach(this::addEntry);
			}
			return;
		}
		String[] keywords = search.toLowerCase(Locale.ENGLISH).split("\\s+");
		List<Entry> results = Lists.newArrayList();
		for (Entry entry : entries) {
			if (entry.isSecondary() || entry instanceof Title) {
				continue;
			}
			if (matches(entry, keywords) || entry.secondaryOptions().stream().anyMatch($ -> matches($, keywords))) {
				results.add(entry);
			}
		}
		if (results.isEmpty()) {
			addEntry(new Title(namespace, Component.translatable("gui.jade.no_results").withStyle(ChatFormatting.GRAY)));
			return;
		}
		Entry lastTitle = null;
		for (Entry entry : results) {
			if (entry.parent() instanceof Title title && title != lastTitle) {
				addEntry(title);
				lastTitle = title;
			}
			addEntry(entry);
			entry.secondaryOptions().forEach(this::addEntry);
		}
		if (results.size() == 1 && !results.getFirst().secondaryOptions().isEmpty()) {
			popup.expand(results.getFirst(), true);
		}
	}

	private static boolean matches(Entry entry, String[] keywords) {
		List<String> messages = entry.getMessages();
		for (String keyword : keywords) {
			boolean found = false;
			for (String message : messages) {
				if (message.contains(keyword)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		popup.clearPendingIfDragged(event.x(), event.y());
		return super.mouseDragged(event, dx, dy);
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
		if (displayMode == DisplayMode.CARD) {
			setScrollAmount(scrollAmount() + entry.getY() - (getY() + 2));
		} else {
			setScrollAmount(defaultEntryHeight * children().indexOf(entry) + 1);
		}
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
		Entry expanded = popup.expanded();
		if (expanded != null && popup.width() > 0 && popup.isInside(event.x(), event.y())) {
			for (Entry option : expanded.secondaryOptions()) {
				if (option.isMouseOver(event.x(), event.y())) {
					return super.mouseClicked(event, bl);
				}
			}
			return true;
		}
		return super.mouseClicked(event, bl) || isMouseOver(event.x(), event.y());
	}

	@Override
	public Optional<GuiEventListener> getChildAt(double x, double y) {
		Entry expanded = popup.expanded();
		if (expanded != null && popup.width() > 0 && popup.isInside(x, y)) {
			for (Entry option : expanded.secondaryOptions()) {
				if (option.isMouseOver(x, y)) {
					return Optional.of(option);
				}
			}
			return Optional.empty();
		}
		return super.getChildAt(x, y);
	}

	@Override
	public void setSelected(OptionsList.@Nullable Entry entry) {
		if (selected == entry) {
			return;
		}
		selected = entry;
		if (!minecraft.getLastInputType().isKeyboard() || entry == null) {
			return;
		}
		Entry popupTarget = entry.isSecondary() ? entry.parent() : entry.isExpandable() ? entry : null;
		if (popupTarget != null) {
			if (popup.expanded() != popupTarget) {
				popup.expand(popupTarget, false);
				layoutCards();
			}
		} else if (popup.expanded() != null) {
			popup.collapse();
		}
		scrollToEntry(entry);
	}

	public static class EntryWidget {
		public final AbstractWidget widget;
		public int offsetX;

		public EntryWidget(AbstractWidget widget) {
			this.widget = widget;
		}

		public EntryWidget(AbstractWidget widget, int offsetX, int offsetY, boolean floatRight) {
			this(widget);
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.floatRight = floatRight;
		}

		public int offsetY;
		public boolean floatRight;
	}

	public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

		protected final List<String> messages = Lists.newArrayList();
		private final List<AbstractWidget> rawWidgets = Lists.newArrayList();
		protected final List<EntryWidget> widgets = Lists.newArrayList();
		protected List<Component> description = List.of();
		protected final String namespace;
		private @Nullable Entry parent;
		private List<Entry> children = List.of();
		protected AbstractStringWidget title;
		protected Font font;
		private @Nullable AbstractWidget mainWidget;
		private final List<Consumer<Entry>> resizeListeners = Lists.newArrayList();
		private @Nullable ConfigIcon icon;
		private @Nullable ItemStack resolvedIcon;
		private boolean cardMode;
		private boolean secondary;
		private boolean popupMode;
		private boolean popupWidgetsSized;
		private @Nullable OptionsList optionsList;

		public Entry(String namespace, AbstractStringWidget title) {
			this.namespace = namespace;
			this.title = title;
			font = title.getFont();
			addWidget(new EntryWidget(title, getTextX(), getTextY(), false));
			addMessage(title.getMessage().getString());
		}

		public Entry(AbstractStringWidget title) {
			this(DEFAULT_NAMESPACE, title);
		}

		public Entry(String namespace, Component component) {
			this(namespace, new JadeMultiLineTextWidget(component, Minecraft.getInstance().font));
			JadeMultiLineTextWidget widget = (JadeMultiLineTextWidget) title;
			addResizeListener($ -> {
				widget.setMaxWidth($.getContentWidth() - $.getTextX() - $.getWidgetZone(), StringWidget.TextOverflow.SCROLLING);
			});
		}

		public Entry(Component component) {
			this(DEFAULT_NAMESPACE, component);
		}

		public static MutableComponent makeTitle(String namespace, String key) {
			return Component.translatable(makeKey(namespace, key));
		}

		public static String makeKey(String namespace, String key) {
			return Util.makeDescriptionId("config", Identifier.fromNamespaceAndPath(namespace, key));
		}

		public Entry setIcon(@Nullable Identifier sprite) {
			return setIcon(sprite == null ? null : ConfigIcon.sprite(sprite));
		}

		public Entry setIcon(@Nullable ConfigIcon icon) {
			this.icon = icon == null || icon.isNone() ? null : icon;
			resolvedIcon = null;
			return this;
		}

		public @Nullable ConfigIcon icon() {
			return icon;
		}

		private ItemStack resolveIcon(ConfigIcon.Item icon) {
			if (resolvedIcon == null) {
				resolvedIcon = ItemStacks.of(icon.item());
			}
			return resolvedIcon;
		}

		public boolean isCardMode() {
			return cardMode;
		}

		public void setCardMode(boolean cardMode) {
			this.cardMode = cardMode;
		}

		void setOptionsList(OptionsList optionsList) {
			this.optionsList = optionsList;
		}

		public boolean isSecondary() {
			return secondary;
		}

		public void setSecondary(boolean secondary) {
			this.secondary = secondary;
		}

		public void setPopupMode(boolean popupMode) {
			this.popupMode = popupMode;
		}

		public void setPopupAlpha(float alpha) {
			for (EntryWidget widget : widgets) {
				widget.widget.setAlpha(alpha);
			}
		}

		public void applyPopupWidgets() {
			if (popupWidgetsSized) {
				return;
			}
			popupWidgetsSized = true;
			for (EntryWidget widget : widgets) {
				if (widget.widget != title) {
					widget.widget.setWidth(Math.max(20, widget.widget.getWidth() - 10));
				}
			}
		}

		public List<Entry> secondaryOptions() {
			return children;
		}

		public boolean isExpandable() {
			return !secondary && !(this instanceof Title) && !children.isEmpty();
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			if (cardMode) {
				return mouseX >= getX() - CARD_BLEED &&
						mouseX < getX() + getWidth() + CARD_BLEED &&
						mouseY >= getY() - CARD_BLEED &&
						mouseY < getY() + getHeight() + CARD_BLEED;
			}
			return super.isMouseOver(mouseX, mouseY);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			if (cardMode &&
					isExpandable() &&
					optionsList != null &&
					!isOverWidget(event.x(), event.y())) {
				optionsList.popup.setPending(this, event.x(), event.y());
				return true;
			}
			return super.mouseClicked(event, doubleClick);
		}

		private boolean isOverWidget(double mouseX, double mouseY) {
			for (AbstractWidget widget : rawWidgets) {
				if (widget.isMouseOver(mouseX, mouseY)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void setWidth(int width) {
			super.setWidth(width);
			notifyResizeListeners();
		}

		@Override
		public void setHeight(int height) {
			super.setHeight(height);
			notifyResizeListeners();
		}

		public void addResizeListener(Consumer<Entry> listener) {
			resizeListeners.add(listener);
		}

		public void notifyResizeListeners() {
			for (EntryWidget widget : widgets) {
				if (widget.widget == title) {
					widget.offsetX = getTextX();
					widget.offsetY = getTextY();
					break;
				}
			}
			for (Consumer<Entry> listener : resizeListeners) {
				listener.accept(this);
			}
		}

		public @Nullable AbstractWidget mainWidget() {
			return mainWidget;
		}

		@SuppressWarnings("UnusedReturnValue")
		public EntryWidget addWidget(AbstractWidget widget, int offsetX) {
			return addWidget(new EntryWidget(widget, offsetX, -widget.getHeight() / 2, true));
		}

		public EntryWidget addWidget(EntryWidget widget) {
			widgets.add(widget);
			rawWidgets.add(widget.widget);
			if (mainWidget == null && !(widget.widget instanceof AbstractStringWidget)) {
				mainWidget = widget.widget;
			}
			return widget;
		}

		@Override
		public List<? extends AbstractWidget> children() {
			return rawWidgets;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return rawWidgets.stream().filter(widget -> widget.visible).toList();
		}

		@Override
		public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTime) {
			if (cardMode) {
				extractCardContent(guiGraphics, mouseX, mouseY, hovered, deltaTime);
				return;
			}
			for (EntryWidget widget : widgets) {
				AbstractWidget rawWidget = widget.widget;
				int x;
				if (widget.floatRight) {
					x = getContentWidth() - getWidgetZone() + widget.offsetX;
				} else {
					x = widget.offsetX;
				}
				rawWidget.setX(getContentX() + x);
				rawWidget.setY(getContentY() + getContentHeight() / 2 + widget.offsetY);
				rawWidget.extractRenderState(guiGraphics, mouseX, mouseY, deltaTime);
			}
		}

		private void extractCardContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTime) {
			boolean expandable = isExpandable();
			String backgroundName;
			if (expandable) {
				backgroundName = hovered ? "card_background_expandable_hover" : "card_background_expandable";
			} else {
				backgroundName = hovered ? "card_background_hover" : "card_background";
			}
			guiGraphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					Identifier.fromNamespaceAndPath(namespace, backgroundName),
					getX() - CARD_BLEED - CARD_SHADOW,
					getY() - CARD_BLEED - CARD_SHADOW,
					getWidth() + (CARD_BLEED + CARD_SHADOW) * 2,
					getHeight() + (CARD_BLEED + CARD_SHADOW) * 2);
			int iconX = getContentX() + 4;
			int iconY;
			if (isInlineWidget()) {
				int centerY = getContentYMiddle();
				iconY = centerY - CARD_ICON_SIZE / 2;
				for (EntryWidget widget : widgets) {
					AbstractWidget rawWidget = widget.widget;
					int x;
					if (rawWidget == title) {
						x = getContentX() + getTextX();
					} else if (widget.floatRight) {
						x = getContentRight() - rawWidget.getWidth() - 4;
					} else {
						x = getContentX() + widget.offsetX;
					}
					rawWidget.setX(x);
					rawWidget.setY(centerY - rawWidget.getHeight() / 2);
					rawWidget.extractRenderState(guiGraphics, mouseX, mouseY, deltaTime);
				}
			} else {
				iconY = getContentY() + 1;
				int titleY = getContentY() + 4;
				int widgetBottom = getContentBottom() - 2;
				for (EntryWidget widget : widgets) {
					AbstractWidget rawWidget = widget.widget;
					if (rawWidget == title) {
						rawWidget.setX(getContentX() + getTextX());
						rawWidget.setY(titleY);
					} else {
						rawWidget.setWidth(getContentWidth() - 8);
						rawWidget.setX(getContentX() + 4);
						rawWidget.setY(widgetBottom - rawWidget.getHeight());
					}
					rawWidget.extractRenderState(guiGraphics, mouseX, mouseY, deltaTime);
				}
			}
			if (icon instanceof ConfigIcon.Sprite(Identifier sprite)) {
				guiGraphics.blitSprite(
						RenderPipelines.GUI_TEXTURED,
						sprite,
						iconX,
						iconY,
						CARD_ICON_SIZE,
						CARD_ICON_SIZE);
			} else if (icon instanceof ConfigIcon.Item itemIcon) {
				guiGraphics.fakeItem(resolveIcon(itemIcon), iconX, iconY);
			}
			if (expandable) {
				guiGraphics.blitSprite(
						RenderPipelines.GUI_TEXTURED,
						Identifier.fromNamespaceAndPath(namespace, "card_arrow"),
						getX() + (getWidth() - CARD_ARROW_WIDTH) / 2,
						getY() + getHeight() + CARD_BLEED - CARD_ARROW_HEIGHT - 2,
						CARD_ARROW_WIDTH,
						CARD_ARROW_HEIGHT);
			}
		}

		public void setDisabled(boolean disabled) {
			for (AbstractWidget widget : rawWidgets) {
				if (widget instanceof AbstractStringWidget) {
					continue;
				}
				widget.active = !disabled;
				if (widget instanceof EditBox editBox) {
					editBox.setEditable(!disabled);
				}
			}
		}

		public List<Component> getDescription() {
			return description;
		}

		public Entry appendDescription(Component description) {
			if (this.description.isEmpty()) {
				this.description = Lists.newArrayList(description);
			} else {
				this.description.add(description);
			}
			addMessage(description.getString());
			return this;
		}

		public List<Component> getDescriptionOnShift() {
			return List.of();
		}

		public int getTextX() {
			if (cardMode) {
				return icon == null ? 6 : CARD_ICON_SIZE + 10;
			}
			return 10;
		}

		public int getWidgetZone() {
			if (!cardMode) {
				AbstractWidget widget = mainWidget();
				if (popupMode && widget != null) {
					return widget.getWidth() + 10;
				}
				return 110;
			}
			return isInlineWidget() ? CARD_WIDGET_WIDTH + 8 : 8;
		}

		public boolean isInlineWidget() {
			return false;
		}

		public void setTitleMaxRows(int maxRows) {
			if (title instanceof JadeMultiLineTextWidget widget) {
				widget.setMaxRows(maxRows);
			}
		}

		public int cardHeight() {
			return isInlineWidget() ? CARD_HEIGHT : CARD_HEIGHT_TALL;
		}

		public int getTextY() {
			return -3;
		}

		public int getTextWidth() {
			return title.getWidth();
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
			key = makeKey(namespace, key + "_extra_msg");
			if (JadeUI.hasTranslation(key)) {
				addMessage(I18n.get(key));
			}
		}

		public Component title() {
			return title.getMessage();
		}

		public void setTitle(Component title) {
			this.title.setMessage(title);
		}

		public List<AbstractWidget> focusableWidgets() {
			List<AbstractWidget> result = Lists.newArrayListWithCapacity(children().size());
			for (AbstractWidget widget : children()) {
				if (widget != title && widget.visible && widget.isActive()) {
					result.add(widget);
				}
			}
			return result;
		}

		@Override
		public @Nullable ComponentPath focusPathAtIndex(FocusNavigationEvent navigationEvent, int currentIndex) {
			if (children().isEmpty()) {
				return null;
			}
			Set<AbstractWidget> widgets = Sets.newLinkedHashSet();
			widgets.add(children().get(Math.min(currentIndex, children().size() - 1)));
			if (mainWidget != null) {
				widgets.add(mainWidget);
			}
			widgets.addAll(children());
			for (AbstractWidget widget : widgets) {
				if (!widget.isActive() || widget == title) {
					continue;
				}
				ComponentPath componentPath = widget.nextFocusPath(navigationEvent);
				if (componentPath != null) {
					return ComponentPath.path(this, componentPath);
				}
			}
			return null;
		}
	}

	public static class Title extends Entry {

		public Component narration;

		public Title(String namespace, String key) {
			this(namespace, makeTitle(namespace, key));
			addMessageKey(key);
			key = makeKey(namespace, key + "_desc");
			if (JadeUI.hasTranslation(key)) {
				description = List.of(Component.translatable(key));
				addMessage(description.getFirst().getString());
			}
			narration = Component.translatable("narration.jade.category", title());
		}

		public Title(String key) {
			this(DEFAULT_NAMESPACE, key);
		}

		public Title(String namespace, Component title) {
			super(namespace, new StringWidget(title, Minecraft.getInstance().font));
			narration = title;
		}

		public Title(Component title) {
			this(DEFAULT_NAMESPACE, title);
		}

		@Override
		public int getTextX() {
			return (getContentWidth() - getTextWidth()) / 2 - 10;
		}

		@Override
		public int getTextY() {
			return 0;
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
