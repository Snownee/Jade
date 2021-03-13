package mcp.mobius.waila.overlay.element;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.Element;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class ItemStackElement extends Element {

    private final ItemStack stack;
    private final float scale;
    public static final ItemStackElement EMPTY = new ItemStackElement(ItemStack.EMPTY, 1);

    private ItemStackElement(ItemStack stack, float scale) {
        this.stack = stack;
        this.scale = scale == 0 ? 1 : scale;
    }

    public static ItemStackElement of(ItemStack stack) {
        return of(stack, 1);
    }

    public static ItemStackElement of(ItemStack stack, float scale) {
        if (scale == 1 && stack.isEmpty()) {
            return EMPTY;
        }
        return new ItemStackElement(stack, scale);
    }

    @Override
    public Size getSize() {
        int size = MathHelper.floor(18 * scale);
        return new Size(size, size);
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, int maxX, int maxY) {
        if (stack.isEmpty())
            return;
        DisplayHelper.INSTANCE.drawItem(matrixStack, x, y, stack, scale);
    }

}
