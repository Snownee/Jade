package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.overlay.DisplayHelper;

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
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		PoseStack matrixStack = guiGraphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(x, y + scale, 0);
		matrixStack.scale(scale, scale, 1);
		DisplayHelper.INSTANCE.drawText(guiGraphics, text, 0, 0, IThemeHelper.get().getNormalColor());
		matrixStack.popPose();
	}

	@Override
	public @Nullable String getMessage() {
		return text.getString();
	}

}
