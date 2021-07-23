package mcp.mobius.waila.impl.ui;

import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SubTextElement extends Element {

	private final String text;

	public SubTextElement(String text) {
		this.text = text;
	}

	@Override
	public Vec2 getSize() {
		return Vec2.ZERO;
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		matrixStack.pushPose();
		WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
		matrixStack.translate(x, y, 800);
		matrixStack.scale(0.75f, 0.75f, 0);
		DisplayHelper.INSTANCE.drawText(matrixStack, text, 0, 0, color.getTheme().textColor);
		matrixStack.popPose();
	}

}
