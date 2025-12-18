package snownee.jade.impl.ui;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

public class ItemStackElement extends Element {

	private final ItemStack item;
	private final float scale;
	private final @Nullable String text;

	private ItemStackElement(ItemStack item, float scale, @Nullable String text) {
		this.item = item;
		this.scale = scale == 0 ? 1 : scale;
		this.text = text;
		width = height = Mth.floor(18 * scale);
	}

	public static ItemStackElement of(ItemStack stack) {
		return of(stack, 1);
	}

	public static ItemStackElement of(ItemStack stack, float scale) {
		return of(stack, scale, null);
	}

	public static ItemStackElement of(ItemStack stack, float scale, @Nullable String text) {
		return new ItemStackElement(stack, scale, text);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (item.isEmpty()) {
			return;
		}
		if (mouseX != -1 && getRectangle().containsPoint(mouseX, mouseY)) {
			setHoverEffect(graphics, new HoverEvent.ShowItem(item));
		}
		DisplayHelper.INSTANCE.drawItem(graphics, getX() + 1, getY() + 1, item, scale, text);
	}

	@Override
	public @Nullable Component getNarration() {
		if (item.isEmpty()) {
			return null;
		}
		return Component.literal("%s %s".formatted(item.getCount(), item.getHoverName().getString()));
	}

	public ItemStack getItem() {
		return item;
	}

	@Override
	public boolean copyToClipboard(KeyboardHandler keyboardHandler) {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (item.isEmpty() || connection == null) {
			return false;
		}
		ItemInput itemInput = new ItemInput(item.typeHolder(), item.getComponentsPatch());
		keyboardHandler.setClipboard(itemInput.serialize(connection.registryAccess()));
		return true;
	}
}
