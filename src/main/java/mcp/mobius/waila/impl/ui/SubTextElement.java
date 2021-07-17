package mcp.mobius.waila.impl.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.ui.Element;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SubTextElement extends Element {

	private final String text;

	public SubTextElement(String text) {
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

		IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		fontRenderer.renderString(text, 0, 0, color.getTheme().textColor, color.getTheme().textShadow, matrixStack.getLast().getMatrix(), irendertypebuffer$impl, false, 0, 15728880);
		irendertypebuffer$impl.finish();
		matrixStack.pop();
	}

}
