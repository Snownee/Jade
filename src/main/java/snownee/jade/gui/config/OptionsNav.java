package snownee.jade.gui.config;

import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.JadeIds;

public class OptionsNav extends ObjectSelectionList<OptionsNav.Entry> {
	private static final Identifier NAVBAR_BACKGROUND = JadeIds.JADE("navbar_background");
	private static final Identifier INWORLD_NAVBAR_BACKGROUND = JadeIds.JADE("inworld_navbar_background");
	private final OptionsList options;
	private int current;

	public OptionsNav(OptionsList options, int width, int height, int top, int itemHeight) {
		super(Minecraft.getInstance(), width, height, top, itemHeight);
		this.options = options;
	}

	@Override
	protected void renderListItems(GuiGraphics guiGraphics, int i, int j, float f) {
		super.renderListItems(guiGraphics, i, j, f);
		if (children().isEmpty()) {
			return;
		}
		Entry focused = getFocused();
		if (focused != null && minecraft.getLastInputType().isKeyboard()) {
			current = children().indexOf(focused);
		}
		float top = getY() + 4 - (float) this.scrollAmount() + current * this.defaultEntryHeight;
		int left = getRowLeft() + 2;
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(0, top);
		guiGraphics.fill(left, 0, left + 2, defaultEntryHeight - 4, 0xFFFFFFFF);
		guiGraphics.pose().popMatrix();
	}

	@Override
	protected void renderListBackground(GuiGraphics guiGraphics) {
		Identifier Identifier = minecraft.level == null ? NAVBAR_BACKGROUND : INWORLD_NAVBAR_BACKGROUND;
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier, getX(), getY(), getWidth(), getHeight());
	}

	@Override
	protected void renderListSeparators(GuiGraphics guiGraphics) {
	}

	@Override
	protected void renderSelection(GuiGraphics guiGraphics, Entry entry, int i) {
		// NO-OP
	}

	public void addEntry(OptionsList.Title entry) {
		super.addEntry(new Entry(this, entry));
	}

	@Override
	public int getRowWidth() {
		return width;
	}

	//TODO: check if it is still needed
	@Override
	protected int scrollBarX() {
		return getRowLeft() + getRowWidth() - 8;
	}

	public void refresh() {
		clearEntries();
		if (options.children().size() <= 1) {
			return; // only the "no results" entry
		}
		for (OptionsList.Entry child : options.children()) {
			if (child instanceof OptionsList.Title titleEntry) {
				addEntry(titleEntry);
			}
		}
	}

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent event) {
		if (!isFocused() && event instanceof FocusNavigationEvent.ArrowNavigation nav && nav.direction() == ScreenDirection.LEFT) {
			for (Entry entry : children()) {
				if (entry.title == options.currentTitle) {
					return ComponentPath.path(entry, this);
				}
			}
		}
		return super.nextFocusPath(event);
	}

	@Override
	public void setFocused(@Nullable GuiEventListener listener) {
		super.setFocused(listener);
		if (minecraft.getLastInputType().isKeyboard() && getFocused() instanceof Entry entry) {
			options.showOnTop(entry.title);
		}
	}

	public static class Entry extends ObjectSelectionList.Entry<Entry> {

		private final OptionsList.Title title;
		private final OptionsNav parent;
		@Nullable
		private WidgetTooltipHolder tooltip;

		public Entry(OptionsNav parent, OptionsList.Title title) {
			this.parent = parent;
			this.title = title;
			refreshTooltip();
		}

		protected void refreshTooltip() {
			if (10 + title.getTextWidth() > parent.getRowWidth()) {
				tooltip = new WidgetTooltipHolder() {
					@Override
					public ClientTooltipPositioner createTooltipPositioner(ScreenRectangle screenRectangle, boolean bl, boolean bl2) {
						return new FixedTooltipPositioner(new Vector2i(
								screenRectangle.left() + 10,
								screenRectangle.top() + (screenRectangle.height() / 2) - (title.client.font.lineHeight / 2)));
					}
				};
				tooltip.set(Tooltip.create(title.getTitle()));
			} else {
				tooltip = null;
			}
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTime) {
			guiGraphics.drawString(
					title.client.font,
					title.getTitle().getString(),
					getContentX() + 10,
					getContentYMiddle() - (title.client.font.lineHeight / 2),
					0xFFFFFFFF);
			if (isFocused() && parent.minecraft.getLastInputType().isKeyboard()) {
				int color = 0xFFAAAAAA;
				int left = getContentX() + 2;
				int right = getContentRight() - 2;
				int top = getContentY();
				int bottom = getContentBottom();
				guiGraphics.fill(left, top, right, top + 1, color);
				guiGraphics.fill(left, bottom, right, bottom - 1, color);
				guiGraphics.fill(left, top, left + 1, bottom, color);
				guiGraphics.fill(right, top, right - 1, bottom, color);
			} else if (parent.options.currentTitle == title) {
				if (!parent.isMouseOver(mouseX, mouseY)) {
					parent.centerScrollOn(this);
				}
				parent.current = parent.children().indexOf(this);
			}
			if (tooltip != null) {
				tooltip.refreshTooltipForNextRenderPass(
						guiGraphics,
						mouseX,
						mouseY,
						isMouseOver(mouseX, mouseY),
						isFocused(),
						getRectangle());
			}
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
			if (event.button() == 0) {
				onPress();
				return true;
			}
			return super.mouseClicked(event, bl);
		}

		@Override
		public boolean keyPressed(KeyEvent keyEvent) {
			if (keyEvent.isSelection()) {
				this.onPress();
				return true;
			}
			return false;
		}

		@Override
		public Component getNarration() {
			return title.narration;
		}

		public void onPress() {
			parent.playDownSound(Minecraft.getInstance().getSoundManager());
			parent.options.showOnTop(title);
		}

		public OptionsList.Title getTitle() {
			return title;
		}
	}

}
