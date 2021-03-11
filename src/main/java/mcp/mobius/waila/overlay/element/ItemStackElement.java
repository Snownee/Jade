package mcp.mobius.waila.overlay.element;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.Element;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.overlay.DisplayUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class ItemStackElement extends Element {

    private final ItemStack stack;
    private final float scale;

    public ItemStackElement(ItemStack stack, float scale) {
        this.stack = stack;
        this.scale = scale == 0 ? 1 : scale;
    }

    public ItemStackElement(ItemStack stack) {
        this(stack, 1);
    }

    @Override
    public Size getSize() {
        int size = MathHelper.floor(18 * scale);
        return new Size(size, size);
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y) {
        if (stack.isEmpty())
            return;
        DisplayUtil.renderStack(matrixStack, x, y, stack, scale);
    }

}
