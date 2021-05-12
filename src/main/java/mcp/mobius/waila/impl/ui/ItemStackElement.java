package mcp.mobius.waila.impl.ui;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;

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
	public Vector2f getSize() {
		int size = MathHelper.floor(18 * scale);
		return new Vector2f(size, size);
	}

	@Override
	public void render(MatrixStack matrixStack, float x, float y, float maxX, float maxY) {
		if (stack.isEmpty())
			return;
		RenderHelper.enableStandardItemLighting();
		DisplayHelper.INSTANCE.drawItem(matrixStack, x + 1, y + 1, stack, scale, null);
		RenderHelper.disableStandardItemLighting();
	}

}
