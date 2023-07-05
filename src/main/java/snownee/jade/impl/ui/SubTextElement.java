package snownee.jade.impl.ui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;

public class SubTextElement extends Element {

	private final Component text;

	public SubTextElement(Component text) {
		this.text = text;
	}

	@Override
	public Vec2 getSize() {
		return Vec2.ZERO;
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		matrixStack.pushPose();
		matrixStack.translate(x, y, 800);
		matrixStack.scale(0.75f, 0.75f, 0);
		DisplayHelper.INSTANCE.drawText(matrixStack, text, 0, 0, OverlayRenderer.normalTextColorRaw);
		matrixStack.popPose();
	}

}
