package mcp.mobius.waila.overlay.element;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackElement extends Element {

	private static final Vector2f DEFAULT_SIZE = new Vector2f(16, 16);
	private final FluidStack fluidStack;

	public FluidStackElement(FluidStack fluidStack) {
		this.fluidStack = fluidStack;
		Preconditions.checkNotNull(fluidStack);
	}

	@Override
	public Vector2f getSize() {
		return DEFAULT_SIZE;
	}

	@Override
	public void render(MatrixStack matrixStack, float x, float y, float maxX, float maxY) {
		Vector2f size = getCachedSize();
		DisplayHelper.INSTANCE.drawFluid(matrixStack, x, y, fluidStack, size.x, size.y, fluidStack.getAmount());
	}
}
