package snownee.jade.gui.config;

import org.joml.Vector2ic;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;

public record FixedTooltipPositioner(Vector2ic pos) implements ClientTooltipPositioner {
	@Override
	public Vector2ic positionTooltip(int i, int j, int mouseX, int mouseY, int m, int n) {
		return pos;
	}
}
