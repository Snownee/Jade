package snownee.jade.gui.config;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;

public class OptionsNav extends ObjectSelectionList<OptionsNav.Entry> {

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
		double top = getY() + 4 - this.getScrollAmount() + current * this.itemHeight + this.headerHeight;
		int left = getRowLeft() + 2;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, top, 0);
		guiGraphics.fill(left, 0, left + 2, itemHeight - 4, 0xFFFFFFFF);
		guiGraphics.pose().popPose();
	}

	@Override
	protected void renderListSeparators(GuiGraphics guiGraphics) {
	}

	@Override
	protected void renderSelection(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
	}

	public void addEntry(OptionsList.Title entry) {
		super.addEntry(new Entry(this, entry));
	}

	@Override
	public int getRowWidth() {
		return width;
	}

	@Override
	protected int getScrollbarPosition() {
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
		public void render(
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
			guiGraphics.drawString(
					title.client.font,
					title.getTitle().getString(),
					rowLeft + 10,
					rowTop + (height / 2) - (title.client.font.lineHeight / 2),
					0xFFFFFF);
			if (isFocused() && parent.minecraft.getLastInputType().isKeyboard()) {
				int color = 0xFFAAAAAA;
				int left = rowLeft + 2;
				int right = rowLeft + width - 2;
				int bottom = rowTop + height;
				guiGraphics.fill(left, rowTop, right, rowTop + 1, color);
				guiGraphics.fill(left, bottom, right, bottom - 1, color);
				guiGraphics.fill(left, rowTop, left + 1, bottom, color);
				guiGraphics.fill(right, rowTop, right - 1, bottom, color);
			} else if (parent.options.currentTitle == title) {
				if (!parent.isMouseOver(mouseX, mouseY)) {
					parent.ensureVisible(this);
				}
				parent.current = index;
			}
			if (tooltip != null) {
				tooltip.refreshTooltipForNextRenderPass(
						isMouseOver(mouseX, mouseY),
						isFocused(),
						new ScreenRectangle(rowLeft, rowTop, width, height));
			}
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == 0) {
				onPress();
			}
			return true;
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			if (CommonInputs.selected(i)) {
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
