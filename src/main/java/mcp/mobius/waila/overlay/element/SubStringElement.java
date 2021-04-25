package mcp.mobius.waila.overlay.element;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.ui.Element;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SubStringElement extends Element {

	private final String text;

	public SubStringElement(String text) {
		this.text = text;
	}

	@Override
	public Vector2f getSize() {
		return Vector2f.ZERO;
	}

	@Override
	public void render(MatrixStack matrixStack, float x, float y, float maxX, float maxY) {
		matrixStack.push();
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
		matrixStack.translate(x, y, 800);
		matrixStack.scale(0.75f, 0.75f, 0);

		fontRenderer.drawStringWithShadow(matrixStack, text, 0, 0, color.getFontColor());
		matrixStack.pop();
	}

}
