package snownee.jade.gui;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import snownee.jade.JadeClient;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.gui.config.BelowOrAboveListEntryTooltipPositioner;
import snownee.jade.gui.config.NotUglyEditBox;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.OptionsNav;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.overlay.DisplayHelper;

public abstract class BaseOptionsScreen extends Screen {

	protected final Screen parent;
	public Button saveButton;
	protected Runnable saver;
	protected Runnable canceller;
	protected OptionsList options;
	protected OptionsNav optionsNav;
	private NotUglyEditBox searchBox;

	public BaseOptionsScreen(Screen parent, Component title) {
		super(title);
		this.parent = parent;
	}

	@Override
	protected void init() {
		Objects.requireNonNull(minecraft);
		double scroll = options == null ? 0 : options.scrollAmount();
		super.init();
		if (options != null) {
			options.removed();
		}
		options = createOptions(new OptionsList(this, minecraft, 120, 0, width - 120, height - 32, 26, IWailaConfig.get()::save));
		options.setX(120);
		optionsNav = new OptionsNav(options, 120, height - 32 - 18, 18, 18);
		searchBox = new NotUglyEditBox(font, 0, 0, 120, 18, searchBox, Component.translatable("gui.jade.search")) {
			@Override
			public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent event) {
				if (event instanceof FocusNavigationEvent.ArrowNavigation(ScreenDirection direction) &&
						direction.getAxis() == ScreenAxis.HORIZONTAL) {
					return null;
				}
				if (event instanceof FocusNavigationEvent.InitialFocus) {
					return null;
				}
				return super.nextFocusPath(event);
			}
		};
		searchBox.setHint(Component.translatable("gui.jade.search.hint"));
		searchBox.setResponder(s -> {
			options.updateSearch(s);
			optionsNav.refresh();
		});
		searchBox.fixedTextX = 12;
		searchBox.fixedTextY = 6;
		searchBox.fixedInnerWidth = searchBox.getWidth() - 12 - 18;
		ResourceLocation searchBoxBackground = JadeIds.JADE("search_box_background");
		searchBox.background = new WidgetSprites(searchBoxBackground, searchBoxBackground);
		searchBox.alwaysRenderCross = true;
		searchBox.updateTextPosition();
		addRenderableWidget(optionsNav);
		addRenderableWidget(searchBox);
		addRenderableWidget(options);

		options.updateSearch(searchBox.getValue());
		optionsNav.refresh();
		options.forceSetScrollAmount(scroll);

		saveButton = addRenderableWidget(Button.builder(
				Component.translatable("gui.jade.save_and_quit").withStyle(style -> style.withColor(0xFFB9F6CA)), w -> {
					if (options.invalidEntry == null) {
						options.save();
						saver.run();
						minecraft.setScreen(parent);
					} else {
						changeFocus(ComponentPath.path(options.invalidEntry.getFirstWidget(), options.invalidEntry, options, this));
						options.scrollToEntry(options.invalidEntry);
					}
				}).bounds(width - 100, height - 25, 90, 20).build());
		if (canceller != null) {
			addRenderableWidget(Button.builder(
					CommonComponents.GUI_CANCEL, w -> {
						onClose();
					}).bounds(saveButton.getX() - 95, height - 25, 90, 20).build());
		}

		options.updateSaveState();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		OptionsList.Entry entry = options.isMouseOver(mouseX, mouseY) ? options.getEntryAt(mouseX, mouseY) : null;
		if (entry != null) {
			int valueX = entry.getTextX();
			if (mouseX >= valueX && mouseX < valueX + entry.getTextWidth()) {
				List<Component> descs = Lists.newArrayListWithExpectedSize(3);
				descs.addAll(entry.getDescription());
				if (JadeUI.hasShiftDown()) {
					descs.addAll(entry.getDescriptionOnShift());
				}
				if (!descs.isEmpty()) {
					descs.replaceAll(BaseOptionsScreen::processBuiltInVariables);
					setTooltipForNextFrame(guiGraphics, descs, mouseX, mouseY, entry);
				}
			}
			if (entry instanceof OptionValue<?> optionValue && optionValue.serverFeature) {
				int x = entry.getTextX() + entry.getTextWidth() + 1;
				int y = entry.getContentY() + 6;
				if (mouseX >= x && mouseX < x + 4 && mouseY >= y && mouseY < y + 4) {
					setTooltipForNextFrame(guiGraphics, List.of(Component.translatable("gui.jade.server_feature")), mouseX, mouseY, entry);
				}
			}
		}
	}

	public void setTooltipForNextFrame(GuiGraphics guiGraphics, List<Component> descs, int mouseX, int mouseY, OptionsList.Entry entry) {
		Font font = DisplayHelper.font();
		List<FormattedCharSequence> list = descs.stream().flatMap($ -> font.split($, 255).stream()).toList();
		guiGraphics.setTooltipForNextFrame(font, list, new BelowOrAboveListEntryTooltipPositioner(options, entry), mouseX, mouseY, false);
	}

	public static Component processBuiltInVariables(Component component) {
		if (component.getString().contains("${SHOW_DETAILS}")) {
			List<Component> objects = Lists.newArrayListWithExpectedSize(3);
			objects.add(Component.translatable("key.jade.show_details"));
			if (!JadeClient.showDetails.isUnbound()) {
				objects.add(JadeClient.showDetails.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.AQUA));
			}
			Component keyName = Component.translatable("config.jade.key_name_n_bind_" + (objects.size() - 1), objects.toArray());
			component = replaceVariables(component, "${SHOW_DETAILS}", keyName);
		}
		if (component.getString().contains("${SHOW_OVERLAY}")) {
			List<Component> objects = Lists.newArrayListWithExpectedSize(3);
			objects.add(Component.translatable(JadeClient.showOverlay.getName()));
			if (!JadeClient.showOverlay.isUnbound()) {
				objects.add(JadeClient.showOverlay.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.AQUA));
			}
			Component keyName = Component.translatable("config.jade.key_name_n_bind_" + (objects.size() - 1), objects.toArray());
			component = replaceVariables(component, "${SHOW_OVERLAY}", keyName);
		}
		return component;
	}

	private static Component replaceVariables(Component component, String source, Component replacement) {
		MutableComponent newComponent = Component.empty().withStyle(component.getStyle());
		for (Component part : component.toFlatList()) {
			String partString = part.getString();
			if (partString.contains(source)) {
				boolean first = true;
				for (String s : StringUtils.splitByWholeSeparatorPreserveAllTokens(partString, source)) {
					if (first) {
						first = false;
					} else {
						newComponent.append(replacement);
					}
					if (!s.isEmpty()) {
						newComponent.append(Component.literal(s));
					}
				}
			} else {
				newComponent.append(part);
			}
		}
		return newComponent;
	}

	public abstract OptionsList createOptions(OptionsList optionsList);

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
		if (optionsNav.isMouseOver(mouseX, mouseY)) {
			return optionsNav.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
		}
		return options.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
	}

	@Override
	public void onClose() {
		if (canceller != null) {
			canceller.run();
		}
		Objects.requireNonNull(minecraft).setScreen(parent);
	}

	@Override
	public void removed() {
		options.removed();
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return options.selectedKey == null;
	}

	public OptionsNav getOptionsNav() {
		return optionsNav;
	}
}
