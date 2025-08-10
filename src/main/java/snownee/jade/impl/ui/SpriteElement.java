package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.Orientation;
import snownee.jade.api.ui.Rect2f;
import snownee.jade.overlay.DisplayHelper;

public class SpriteElement extends ProgressOverlayElement {

	private final RenderPipeline renderPipeline;
	private final ResourceLocation sprite;
	public @Nullable Orientation tiledOrientation;
	private final int oWidth;
	private final int oHeight;
	private int color = -1;
	private int generation;
	private @Nullable ResourceLocation mappedSprite;

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
			TextureAtlasSprite textureAtlasSprite = graphics.guiSprites.getSprite(mappedSprite());
			Rect2f rect;
			if (floatingRect == null) {
				rect = Rect2f.of(this);
			} else {
				rect = floatingRect.copy();
			}
			float axisLength = tiledOrientation.getAxisLength(rect);
			float axisPosition = tiledOrientation.getAxisPosition(rect);
			float crossAxisLength = tiledOrientation.getCrossAxisLength(rect);
			float crossAxisPosition = tiledOrientation.getCrossAxisPosition(rect);
			float tileAxisStart = 0F;
			float tileAxisStep = tiledOrientation == Orientation.HORIZONTAL ? oWidth : oHeight;
			while (tileAxisStart < axisLength) {
				float tileAxisEnd = Math.min(tileAxisStart + tileAxisStep, axisLength);
				float tileSize = tileAxisEnd - tileAxisStart;
				tiledOrientation.setPosition(rect, axisPosition + tileAxisStart, crossAxisPosition);
				tiledOrientation.setSize(rect, tileSize, crossAxisLength);
				float tileWidth = oWidth;
				float tileHeight = oHeight;
				if (tiledOrientation == Orientation.HORIZONTAL) {
					tileWidth = tileSize;
				} else {
					tileHeight = tileSize;
				}
				DisplayHelper.INSTANCE.blitSprite(
						graphics,
						renderPipeline,
						textureAtlasSprite,
						oWidth,
						oHeight,
						0,
						0,
						tileWidth,
						tileHeight,
						rect.getX(),
						rect.getY(),
						rect.getWidth(),
						rect.getHeight(),
						color);
				tileAxisStart += tileAxisStep;
			}
			return;
		}
		if (floatingRect == null) {
			IDisplayHelper.get().blitSprite(
					graphics,
					renderPipeline,
					mappedSprite(),
					oWidth,
					oHeight,
					0,
					0,
					getX(),
					getY(),
					width,
					height,
					color);
		} else {
			DisplayHelper.INSTANCE.blitSprite(
					graphics,
					renderPipeline,
					mappedSprite(),
					oWidth,
					oHeight,
					0,
					0,
					floatingRect.getX(),
					floatingRect.getY(),
					floatingRect.getWidth(),
					floatingRect.getHeight(),
					color);
		}
		if (IWailaConfig.get().general().isDebug() && floatingRect != null) {
			DisplayHelper.INSTANCE.drawBorder(
					graphics, new ScreenRectangle(
							(int) floatingRect.getX(),
							(int) floatingRect.getY(),
							(int) floatingRect.getWidth(),
							(int) floatingRect.getHeight()),
					1, 0xFF00AAAA, true);
		}
	}

	private ResourceLocation mappedSprite() {
		if (mappedSprite == null || generation != IThemeHelper.get().generation()) {
			generation = IThemeHelper.get().generation();
			mappedSprite = IThemeHelper.get().theme().mapSprite(sprite);
		}
		return mappedSprite;
	}

	@Override
	public boolean canUseFloatingRect(GuiGraphics graphics) {
		TextureAtlasSprite textureAtlasSprite = graphics.guiSprites.getSprite(mappedSprite());
		GuiSpriteScaling scaling = textureAtlasSprite.contents()
				.getAdditionalMetadata(GuiMetadataSection.TYPE)
				.orElse(GuiMetadataSection.DEFAULT)
				.scaling();
		return scaling instanceof GuiSpriteScaling.Stretch;
	}

	public void setColor(int color) {
		this.color = color;
	}
}
