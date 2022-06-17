package snownee.jade.impl.ui;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

public class FluidStackElement extends Element {

	private static final Vec2 DEFAULT_SIZE = new Vec2(16, 16);
	private final FluidState fluidState;

	public FluidStackElement(FluidState fluidState) {
		this.fluidState = fluidState;
		Objects.nonNull(fluidState);
	}

	@Override
	public Vec2 getSize() {
		return DEFAULT_SIZE;
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		Vec2 size = getCachedSize();
		DisplayHelper.INSTANCE.drawFluid(matrixStack, x, y, fluidState, size.x, size.y, FluidConstants.BUCKET);
	}

	@Override
	public @Nullable Component getMessage() {
		return null; //TODO
	}

}
