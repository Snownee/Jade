package snownee.jade.gui.config;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;

public class BelowOrAboveListEntryTooltipPositioner implements ClientTooltipPositioner {

	private final OptionsList list;
	private final OptionsList.Entry entry;

	public BelowOrAboveListEntryTooltipPositioner(OptionsList list, OptionsList.Entry entry) {
		this.list = list;
		this.entry = entry;
	}

	@Override
	public Vector2ic positionTooltip(int i, int j, int mouseX, int mouseY, int m, int n) {
		Vector2i vector2i = new Vector2i();
		int index = list.children().indexOf(entry);
		if (index == -1) {
			vector2i.x = mouseX + 3;
			vector2i.y = mouseY + 3;
			return vector2i;
		}
		vector2i.x = entry.getTextX(list.getRowWidth());
		vector2i.y = list.getRowBottom(index) + 1;
		if (vector2i.y + n > j) {
			vector2i.y = list.getRowTop(index) - n - 1;
		}
		if (vector2i.x + m > i) {
			vector2i.x = Math.max(list.getRowLeft() + list.getRowWidth() - m, 4);
		}
		return vector2i;
	}
}
