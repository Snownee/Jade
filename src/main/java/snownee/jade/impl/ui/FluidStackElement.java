package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

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

	@Override
	public @Nullable Component getMessage() {
		return fluidStack.getDisplayName();
	}

}
