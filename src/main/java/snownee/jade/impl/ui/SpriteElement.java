package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.Orientation;
import snownee.jade.overlay.DisplayHelper;

public class SpriteElement extends ProgressOverlayElement {

	private final RenderPipeline renderPipeline;
	private final ResourceLocation sprite;
	public @Nullable Orientation tiledOrientation;
	private final int oWidth;
	private final int oHeight;

	public SpriteElement(ResourceLocation sprite, int width, int height) {
		this(RenderPipelines.GUI_TEXTURED, sprite, width, height);
	}

	public SpriteElement(RenderPipeline renderPipeline, ResourceLocation sprite, int width, int height) {
		this.renderPipeline = renderPipeline;
		this.sprite = sprite;
		oWidth = this.width = width;
		oHeight = this.height = height;
	}

	@Override
	public @Nullable Component getNarration() {
		return null;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (tiledOrientation != null) {
			//TODO
		}
		if (floatingRect == null) {
			IDisplayHelper.get().blitSprite(
					graphics,
					renderPipeline,
					sprite,
					oWidth,
					oHeight,
					0,
					0,
					getX(),
					getY(),
					width,
					height);
		} else {
			DisplayHelper.INSTANCE.drawBorder(
					graphics, new ScreenRectangle(
							((int) floatingRect.getX()),
							((int) floatingRect.getY()),
							(int) floatingRect.getWidth(),
							(int) floatingRect.getHeight()),
					1, 0xFF00AAAA, true);
			DisplayHelper.INSTANCE.blitSprite(
					graphics,
					renderPipeline,
					sprite,
					oWidth,
					oHeight,
					0,
					0,
					floatingRect.getX(),
					floatingRect.getY(),
					floatingRect.getWidth(),
					floatingRect.getHeight());
		}
	}

	@Override
	public boolean canUseFloatingRect() {
		GuiSpriteManager guiSprites = Minecraft.getInstance().getGuiSprites();
		TextureAtlasSprite textureAtlasSprite = guiSprites.getSprite(sprite);
		GuiSpriteScaling scaling = guiSprites.getSpriteScaling(textureAtlasSprite);
		return scaling instanceof GuiSpriteScaling.Stretch;
	}
}
