package snownee.jade.gui;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import snownee.jade.JadeClient;
import snownee.jade.gui.config.WailaOptionsList;

public abstract class BaseOptionsScreen extends Screen {

	private final Screen parent;
	private final Runnable saver;
	private final Runnable canceller;
	protected WailaOptionsList options;
	private final Set<GuiEventListener> entryWidgets = Sets.newIdentityHashSet();
	public Button saveButton;

	public BaseOptionsScreen(Screen parent, Component title, Runnable saver, Runnable canceller) {
		super(title);

		this.parent = parent;
		this.saver = saver;
		this.canceller = canceller;
	}

	public BaseOptionsScreen(Screen parent, String title, Runnable saver, Runnable canceller) {
		this(parent, WailaOptionsList.Entry.makeTitle(title), saver, canceller);
	}

	public BaseOptionsScreen(Screen parent, String title) {
		this(parent, title, null, null);
	}

	@Override
	protected void init() {
		super.init();
		entryWidgets.clear();
		options = createOptions();
		addRenderableWidget(options);

		if (saver != null && canceller != null) {
			saveButton = addRenderableWidget(Button.builder(Component.translatable("gui.done"), w -> {
				options.save();
				saver.run();
				minecraft.setScreen(parent);
			}).bounds(width / 2 - 100, height - 25, 100, 20).build());
			addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), w -> {
				canceller.run();
				minecraft.setScreen(parent);
			}).bounds(width / 2 + 5, height - 25, 100, 20).build());
		} else {
			saveButton = addRenderableWidget(Button.builder(Component.translatable("gui.done"), w -> {
				options.save();
				minecraft.setScreen(parent);
			}).bounds(width / 2 - 50, height - 25, 100, 20).build());
		}

		options.updateSaveState();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		if (options.serverFeatures > 0) {
			Component component = JadeClient.format("gui.jade.server_features", options.serverFeatures);
			guiGraphics.drawString(font, component, 4, height - 18, ChatFormatting.GRAY.getColor());
			if (mouseY >= height - 18 && mouseY <= height - 18 + font.lineHeight && mouseX >= 4 && mouseX <= 4 + font.width(component)) {
				guiGraphics.renderTooltip(font, font.split(JadeClient.format("gui.jade.server_features.tip", options.serverFeatures), 300), mouseX, mouseY);
			}
		}

		if (options.getSelected() != null && mouseY >= 32 && mouseY <= height - 32) {
			WailaOptionsList.Entry entry = options.getSelected();
			if (!Strings.isNullOrEmpty(entry.getDescription())) {
				int valueX = entry.getTextX(options.getRowWidth());
				if (mouseX >= valueX && mouseX < valueX + entry.getTextWidth()) {
					List<FormattedCharSequence> tooltip = font.split(Component.literal(entry.getDescription()), 200);
					guiGraphics.pose().pushPose();
					guiGraphics.pose().translate(0, 0, 100);
					guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
					RenderSystem.enableDepthTest();
					guiGraphics.pose().popPose();
				}
			}
		}
	}

	public abstract WailaOptionsList createOptions();

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		return options.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		if (canceller != null)
			canceller.run();
		options.onClose();
		super.onClose();
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
