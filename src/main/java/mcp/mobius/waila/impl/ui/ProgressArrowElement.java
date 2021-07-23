package mcp.mobius.waila.impl.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public class ProgressArrowElement extends Element {

	private static final ResourceLocation SHEET = new ResourceLocation(Waila.MODID, "textures/sprites.png");

	private final float progress;

	public ProgressArrowElement(float progress) {
		this.progress = progress;
	}

	@Override
	public Vec2 getSize() {
		return new Vec2(26, 16);
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		RenderSystem.setShaderTexture(0, SHEET);
		RenderSystem.enableBlend();

		// Draws the "empty" background arrow
		DisplayHelper.drawTexturedModalRect(matrixStack, x + 2, y, 0, 16, 22, 16, 22, 16);

		if (progress > 0) {
			int progress = (int) (this.progress * 22);
			// Draws the "full" foreground arrow based on the progress
			DisplayHelper.drawTexturedModalRect(matrixStack, x + 2, y, 0, 0, progress + 1, 16, progress + 1, 16);
		}
	}
}
