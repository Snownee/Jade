package snownee.jade.gui;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import snownee.jade.gui.config.BelowOrAboveListEntryTooltipPositioner;
import snownee.jade.gui.config.NotUglyEditBox;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.OptionsNav;
import snownee.jade.gui.config.value.OptionValue;

public abstract class BaseOptionsScreen extends Screen {

	private final Screen parent;
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
		if (options != null)
			options.removed();
		options = createOptions();
		options.setLeftPos(120);
		optionsNav = new OptionsNav(options, 120, height, 18, height - 32, 18);
		searchBox = new NotUglyEditBox(font, 0, 0, 120, 18, searchBox, Component.translatable("gui.jade.search"));
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
		options.setScrollAmount(scroll);

		saveButton = addRenderableWidget(Button.builder(Component.translatable("gui.jade.save_and_quit").withStyle(style -> style.withColor(0xFFB9F6CA)), w -> {
			options.save();
			saver.run();
			minecraft.setScreen(parent);
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
		renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		OptionsList.Entry entry = options.isMouseOver(mouseX, mouseY) ? options.getEntryAt(mouseX, mouseY) : null;
		if (entry != null) {
			if (!Strings.isNullOrEmpty(entry.getDescription())) {
				int valueX = entry.getTextX(options.getRowWidth());
				if (mouseX >= valueX && mouseX < valueX + entry.getTextWidth()) {
					setTooltipForNextRenderPass(Tooltip.create(Component.literal(entry.getDescription())), new BelowOrAboveListEntryTooltipPositioner(options, entry), false);
				}
			}
			if (entry instanceof OptionValue<?> optionValue && optionValue.serverFeature) {
				int x = entry.getTextX(options.getRowWidth()) + entry.getTextWidth() + 1;
				int y = options.getRowTop(options.children().indexOf(entry)) + 7;
				if (mouseX >= x && mouseX < x + 4 && mouseY >= y && mouseY < y + 4) {
					setTooltipForNextRenderPass(Tooltip.create(Component.translatable("gui.jade.server_feature")), new BelowOrAboveListEntryTooltipPositioner(options, entry), false);
				}
			}
		}
	}

	public abstract OptionsList createOptions();

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
		if (optionsNav.isMouseOver(mouseX, mouseY))
			return optionsNav.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
		return options.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
	}

	@Override
	public void onClose() {
		if (canceller != null)
			canceller.run();
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
}
