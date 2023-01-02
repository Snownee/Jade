package snownee.jade.gui;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import snownee.jade.gui.config.WailaOptionsList;

public abstract class BaseOptionsScreen extends Screen {

	private final Screen parent;
	private final Runnable saver;
	private final Runnable canceller;
	protected WailaOptionsList options;
	private final Set<GuiEventListener> entryWidgets = Sets.newIdentityHashSet();

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
			addRenderableWidget(new Button(width / 2 - 100, height - 25, 100, 20, Component.translatable("gui.done"), w -> {
				options.save();
				saver.run();
				minecraft.setScreen(parent);
			}));
			addRenderableWidget(new Button(width / 2 + 5, height - 25, 100, 20, Component.translatable("gui.cancel"), w -> {
				canceller.run();
				minecraft.setScreen(parent);
			}));
		} else {
			addRenderableWidget(new Button(width / 2 - 50, height - 25, 100, 20, Component.translatable("gui.done"), w -> {
				options.save();
				minecraft.setScreen(parent);
			}));
		}
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);

		if (options.getSelected() != null && mouseY >= 32 && mouseY <= height - 32) {
			WailaOptionsList.Entry entry = options.getSelected();
			AbstractWidget widget = entry.getListener();
			boolean inWidget = widget != null && widget.visible && mouseX >= widget.x && mouseY >= widget.y && mouseX < widget.x + widget.getWidth() && mouseY < widget.y + widget.getHeight();
			if (inWidget && widget instanceof TooltipAccessor accessor && !accessor.getTooltip().isEmpty()) {
				renderTooltip(matrixStack, accessor.getTooltip(), mouseX, mouseY);
			} else if (!Strings.isNullOrEmpty(entry.getDescription())) {
				int valueX = entry.getTextX(options.getRowWidth());
				if ((inWidget && !widget.isActive()) || mouseX >= valueX && mouseX < valueX + entry.getTextWidth()) {
					List<FormattedCharSequence> tooltip = font.split(Component.literal(entry.getDescription()), 200);
					matrixStack.pushPose();
					matrixStack.translate(0, 0, 100);
					renderTooltip(matrixStack, tooltip, mouseX, mouseY);
					RenderSystem.enableDepthTest();
					matrixStack.popPose();
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
