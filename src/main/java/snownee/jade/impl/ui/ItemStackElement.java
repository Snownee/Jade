package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

public class ItemStackElement extends Element {

	private final ItemStack stack;
	private final float scale;
	private final String text;
	public static final ItemStackElement EMPTY = new ItemStackElement(ItemStack.EMPTY, 1, null);

	private ItemStackElement(ItemStack stack, float scale, @Nullable String text) {
		this.stack = stack;
		this.scale = scale == 0 ? 1 : scale;
		this.text = text;
	}

	public static ItemStackElement of(ItemStack stack) {
		return of(stack, 1);
	}

	public static ItemStackElement of(ItemStack stack, float scale) {
		return of(stack, scale, null);
	}

	public static ItemStackElement of(ItemStack stack, float scale, @Nullable String text) {
		if (scale == 1 && stack.isEmpty()) {
			return EMPTY;
		}
		return new ItemStackElement(stack, scale, text);
	}

	@Override
	public Vec2 getSize() {
		int size = Mth.floor(18 * scale);
		return new Vec2(size, size);
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		if (stack.isEmpty()) {
			return;
		}
		DisplayHelper.INSTANCE.drawItem(guiGraphics, x + 1, y + 1, stack, scale, text);
	}

	@Override
	public @Nullable String getMessage() {
		if (stack.isEmpty()) {
			return null;
		}
		return "%s %s".formatted(stack.getCount(), stack.getHoverName().getString());
	}

}
