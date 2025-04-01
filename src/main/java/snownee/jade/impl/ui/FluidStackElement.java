package snownee.jade.impl.ui;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.phys.Vec2;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

public class FluidStackElement extends Element {

	private static final Vec2 DEFAULT_SIZE = new Vec2(16, 16);
	private final JadeFluidObject fluid;

	public FluidStackElement(JadeFluidObject fluid) {
		this.fluid = fluid;
		Objects.requireNonNull(fluid);
	}

	@Override
	public Vec2 getSize() {
		return DEFAULT_SIZE;
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		Vec2 size = getCachedSize();
		DisplayHelper.INSTANCE.drawFluid(matrixStack, x, y, fluid, size.x, size.y, JadeFluidObject.bucketVolume());
	}

	@Override
	public @Nullable String getMessage() {
		return null; //TODO
	}

}
