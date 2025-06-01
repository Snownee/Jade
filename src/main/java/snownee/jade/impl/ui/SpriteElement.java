package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.overlay.DisplayHelper;

public class SpriteElement extends ProgressOverlayElement {

	private final RenderPipeline renderPipeline;
	private final ResourceLocation sprite;

	public SpriteElement(ResourceLocation sprite, int width, int height) {
		this(RenderPipelines.GUI_TEXTURED, sprite, width, height);
	}

	public SpriteElement(RenderPipeline renderPipeline, ResourceLocation sprite, int width, int height) {
		this.renderPipeline = renderPipeline;
		this.sprite = sprite;
		this.width = width;
		this.height = height;
	}

	@Override
	public @Nullable Component getNarration() {
		return null;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (floatingRect == null) {
			IDisplayHelper.get().blitSprite(
					graphics,
					renderPipeline,
					sprite,
					width,
					height,
					0,
					0,
					getX(),
					getY(),
					width,
					height);
		} else {
			DisplayHelper.INSTANCE.blitSprite(
					graphics,
					renderPipeline,
					sprite,
					width,
					height,
					0,
					0,
					floatingRect.getX(),
					floatingRect.getY(),
					floatingRect.getWidth(),
					floatingRect.getHeight());
		}
	}

	@Override
	public void setFreeSpace(int width, int height) {

	}
}
