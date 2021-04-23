package mcp.mobius.waila.api.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public interface IProgressStyle {

	default IProgressStyle color(int color) {
		return color(color, color);
	}

	IProgressStyle color(int color, int color2);

	IProgressStyle textColor(int color);

	IProgressStyle vertical(boolean vertical);

	IProgressStyle fluid(FluidStack fluidStack);

	void render(MatrixStack matrixStack, float x, float y, float w, float h, ITextComponent text);
}
