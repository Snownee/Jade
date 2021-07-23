package mcp.mobius.waila.impl.ui;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackElement extends Element {

	private static final Vec2 DEFAULT_SIZE = new Vec2(16, 16);
	private final FluidStack fluidStack;

	public FluidStackElement(FluidStack fluidStack) {
		this.fluidStack = fluidStack;
		Preconditions.checkNotNull(fluidStack);
	}

	@Override
	public Vec2 getSize() {
		return DEFAULT_SIZE;
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		Vec2 size = getCachedSize();
		DisplayHelper.INSTANCE.drawFluid(matrixStack, x, y, fluidStack, size.x, size.y, fluidStack.getAmount());
	}
}
