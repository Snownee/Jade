package snownee.jade.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import snownee.jade.util.SmoothChasingValue;

public class OptionsNav extends ObjectSelectionList<OptionsNav.Entry> {

	private final OptionsList options;
	private final SmoothChasingValue anchor;

	public OptionsNav(OptionsList options, int width, int height, int top, int bottom, int itemHeight) {
		super(Minecraft.getInstance(), width, height, top, bottom, itemHeight);
		this.options = options;
		this.anchor = new SmoothChasingValue();
		setRenderSelection(false);
		setRenderBackground(false);
	}

	@Override
	protected void renderList(GuiGraphics guiGraphics, int i, int j, float f) {
		super.renderList(guiGraphics, i, j, f);
		anchor.tick(f);
		if (children().isEmpty()) {
			return;
		}
		int top = (int) (y0 + 4 - this.getScrollAmount() + anchor.value * this.itemHeight + this.headerHeight);
		int left = getRowLeft() + 2;
		guiGraphics.fill(left, top, left + 2, top + itemHeight - 4, 0xFFFFFFFF);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.setColor(0.125f, 0.125f, 0.125f, 1.0f);
        guiGraphics.blit(Screen.BACKGROUND_LOCATION, this.x0, this.y0, this.x1, this.y1 + (int)this.getScrollAmount(), this.x1 - this.x0, this.y1 - this.y0, 32, 32);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		super.render(guiGraphics, i, j, f);
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

	public static class Entry extends ObjectSelectionList.Entry<Entry> {

		private final OptionsList.Title title;
		private final OptionsNav parent;

		public Entry(OptionsNav parent, OptionsList.Title title) {
			this.parent = parent;
			this.title = title;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime) {
			guiGraphics.drawString(title.client.font, title.getTitle().getString(), rowLeft + 10, rowTop + (height / 2) - (title.client.font.lineHeight / 2), 0xFFFFFF);
			if (parent.options.currentTitle == title) {
				if (!parent.isMouseOver(mouseX, mouseY)) {
					parent.ensureVisible(this);
				}
				parent.anchor.target(index);
			}
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == 0) {
				parent.options.showOnTop(title);
			}
			return true;
		}

		@Override
		public Component getNarration() {
			return title.narration;
		}
	}

}
