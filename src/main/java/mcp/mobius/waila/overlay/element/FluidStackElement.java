package mcp.mobius.waila.overlay.element;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.Element;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackElement extends Element {

    private static final Size DEFAULT_SIZE = new Size(16, 16);
    private final FluidStack fluidStack;

    public FluidStackElement(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
        Preconditions.checkNotNull(fluidStack);
    }

    @Override
    public Size getSize() {
        return DEFAULT_SIZE;
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, int maxX, int maxY) {
        Size size = getCachedSize();
        DisplayHelper.INSTANCE.drawFluid(matrixStack, x, y, fluidStack, size.width, size.height, fluidStack.getAmount());
    }
}
