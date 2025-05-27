package snownee.jade.impl.ui;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

public class FluidStackElement extends Element {

	private final JadeFluidObject fluid;

	public FluidStackElement(JadeFluidObject fluid) {
		this.fluid = Objects.requireNonNull(fluid);
		width = height = 16;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		DisplayHelper.INSTANCE.drawFluid(graphics, getX(), getY(), fluid, width, height, JadeFluidObject.bucketVolume());
	}

	@Override
	public @Nullable Component getNarration() {
		return null;
	}
}
