package snownee.jade.impl.ui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.ITextElement;
import snownee.jade.overlay.DisplayHelper;

public class SpecialTextElement extends TextElement {

	private float scale = 1;
	private int zOffset;

	public SpecialTextElement(FormattedText text) {
		super(text);
	}

	@Override
	public Vec2 getSize() {
		return new Vec2(DisplayHelper.font().width(text) * scale, DisplayHelper.font().lineHeight * scale + 1);
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		PoseStack matrixStack = guiGraphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(x, y + scale, zOffset);
		matrixStack.scale(scale, scale, 1);
		DisplayHelper.INSTANCE.drawText(guiGraphics, text, 0, 0, IThemeHelper.get().getNormalColor());
		matrixStack.popPose();
	}

	@Override
	public SpecialTextElement toSpecial() {
		return this;
	}

	@Override
	public ITextElement scale(float scale) {
		this.scale = scale;
		return this;
	}

	@Override
	public ITextElement zOffset(int zOffset) {
		this.zOffset = zOffset;
		return this;
	}
}
