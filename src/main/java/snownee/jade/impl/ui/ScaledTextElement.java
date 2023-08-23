package snownee.jade.impl.ui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;

public class ScaledTextElement extends TextElement {

	public final float scale;

	public ScaledTextElement(Component component, float scale) {
		super(component);
		this.scale = scale;
	}

	@Override
	public Vec2 getSize() {
		Font font = Minecraft.getInstance().font;
		return new Vec2(font.width(text) * scale, font.lineHeight * scale + 1);
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		matrixStack.pushPose();
		matrixStack.translate(x, y + scale, 0);
		matrixStack.scale(scale, scale, 1);
		DisplayHelper.INSTANCE.drawText(matrixStack, text, 0, 0, OverlayRenderer.normalTextColorRaw);
		matrixStack.popPose();
	}

}
