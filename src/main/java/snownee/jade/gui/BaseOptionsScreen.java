package snownee.jade.gui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import snownee.jade.JadeClient;
import snownee.jade.gui.config.BelowOrAboveListEntryTooltipPositioner;
import snownee.jade.gui.config.NotUglyEditBox;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.OptionsNav;
import snownee.jade.gui.config.value.OptionValue;

public abstract class BaseOptionsScreen extends Screen {

	protected final Screen parent;
	private final Set<GuiEventListener> entryWidgets = Sets.newIdentityHashSet();
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
		double scroll = options == null ? 0 : options.getScrollAmount();
		super.init();
		entryWidgets.clear();
		if (options != null) {
			options.removed();
		}
		options = createOptions();
		options.setX(120);
		optionsNav = new OptionsNav(options, 120, height - 32 - 18, 18, 18);
		searchBox = new NotUglyEditBox(font, 0, 0, 120, 18, searchBox, Component.translatable("gui.jade.search")) {
			@Override
			public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent event) {
				if (event instanceof FocusNavigationEvent.ArrowNavigation arrow && arrow.direction().getAxis() == ScreenAxis.HORIZONTAL) {
					return null;
				}
				if (event instanceof FocusNavigationEvent.InitialFocus) {
					return null;
				}
				return super.nextFocusPath(event);
			}
		};
		searchBox.setBordered(false);
		searchBox.setHint(Component.translatable("gui.jade.search.hint"));
		searchBox.responder = s -> {
			options.updateSearch(s);
			optionsNav.refresh();
		};
		searchBox.paddingLeft = 12;
		searchBox.paddingTop = 6;
		searchBox.paddingRight = 18;
		addRenderableWidget(optionsNav);
		addRenderableWidget(searchBox);
		addRenderableWidget(options);

		searchBox.responder.accept(searchBox.getValue());
		options.forceSetScrollAmount(scroll);

		saveButton = addRenderableWidget(Button.builder(Component.translatable("gui.jade.save_and_quit")
				.withStyle(style -> style.withColor(0xFFB9F6CA)), w -> {
			if (options.invalidEntry == null) {
				options.save();
				saver.run();
				minecraft.setScreen(parent);
			} else {
				changeFocus(ComponentPath.path(options.invalidEntry.getFirstWidget(), options.invalidEntry, options, this));
				options.ensureVisible(options.invalidEntry);
			}
		}).bounds(width - 100, height - 25, 90, 20).build());
		if (canceller != null) {
			addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, w -> {
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
			int valueX = entry.getTextX(options.getRowWidth());
			if (mouseX >= valueX && mouseX < valueX + entry.getTextWidth()) {
				List<Component> descs = Lists.newArrayListWithExpectedSize(3);
				descs.addAll(entry.getDescription());
				if (hasShiftDown()) {
					descs.addAll(entry.getDescriptionOnShift());
				}
				if (!descs.isEmpty()) {
					descs.replaceAll(BaseOptionsScreen::processBuiltInVariables);
					setTooltipForNextRenderPass(
							MultilineTooltip.create(descs),
							new BelowOrAboveListEntryTooltipPositioner(options, entry),
							false);
				}
			}
			if (entry instanceof OptionValue<?> optionValue && optionValue.serverFeature) {
				int x = entry.getTextX(options.getRowWidth()) + entry.getTextWidth() + 1;
				int y = options.getRowTop(options.children().indexOf(entry)) + 7;
				if (mouseX >= x && mouseX < x + 4 && mouseY >= y && mouseY < y + 4) {
					setTooltipForNextRenderPass(
							Tooltip.create(Component.translatable("gui.jade.server_feature")),
							new BelowOrAboveListEntryTooltipPositioner(options, entry),
							false);
				}
			}
		}
	}

	public static Component processBuiltInVariables(Component component) {
		if (component.getString().contains("${SHOW_DETAILS}")) {
			List<Component> objects = Lists.newArrayListWithExpectedSize(3);
			objects.add(Component.translatable("key.jade.show_details"));
			if (JadeClient.showDetails.getName().contains("alternative")) {
				objects.add(InputConstants.getKey("key.keyboard.left.shift").getDisplayName().copy().withStyle(ChatFormatting.AQUA));
			}
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

	public abstract OptionsList createOptions();

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

	public <T extends GuiEventListener & NarratableEntry> T addEntryWidget(T widget) {
		entryWidgets.add(widget);
		return super.addWidget(widget);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int p_94697_) {
		boolean onList = options.isMouseOver(mouseX, mouseY);
		for (GuiEventListener guieventlistener : children()) {
			if (!onList && entryWidgets.contains(guieventlistener)) {
				continue;
			}
			if (guieventlistener.mouseClicked(mouseX, mouseY, p_94697_)) {
				setFocused(guieventlistener);
				if (p_94697_ == 0) {
					setDragging(true);
				}

				return true;
			}
		}
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return options.selectedKey == null;
	}

	@Override
	public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
		boolean onList = options != null && options.isMouseOver(mouseX, mouseY);
		for (GuiEventListener guieventlistener : children()) {
			if (!onList && entryWidgets.contains(guieventlistener)) {
				continue;
			}
			if (guieventlistener.isMouseOver(mouseX, mouseY)) {
				return Optional.of(guieventlistener);
			}
		}

		return Optional.empty();
	}

	public OptionsNav getOptionsNav() {
		return optionsNav;
	}
}
