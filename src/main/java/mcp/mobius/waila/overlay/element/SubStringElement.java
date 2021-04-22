package mcp.mobius.waila.overlay.element;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.api.ui.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SubStringElement extends Element {

	private final String text;

	public SubStringElement(String text) {
		this.text = text;
	}

	@Override
	public Size getSize() {
		return Size.ZERO;
	}

	@Override
	public void render(MatrixStack matrixStack, int x, int y, int maxX, int maxY) {
		matrixStack.push();
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
		matrixStack.translate(x, y, 800);
		matrixStack.scale(0.75f, 0.75f, 0);

		fontRenderer.drawStringWithShadow(matrixStack, text, 0, 0, color.getFontColor());
		matrixStack.pop();
	}

}
