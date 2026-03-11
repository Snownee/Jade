package snownee.jade.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemButton extends Button {
	private ItemStack icon;

	public ItemButton(
			int i,
			int j,
			int k,
			int l,
			ItemStack icon,
			Component component,
			OnPress onPress,
			CreateNarration createNarration) {
		super(i, j, k, l, component, onPress, createNarration);
		this.icon = icon;
	}

	public void setIcon(ItemStack icon) {
		this.icon = icon;
	}

	public ItemStack getIcon() {
		return icon;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
		extractDefaultSprite(guiGraphics);
		guiGraphics.item(icon, getX() + 2, getY() + 2);
	}
}
