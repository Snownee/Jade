package snownee.jade.api.ui;

import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.impl.ui.StyledElement;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.JadeCodecs;

public abstract class BoxStyle implements Cloneable {
	private static final int[] DEFAULT_PADDING = new int[]{4, 4, 4, 4};
	public static final Codec<GradientBorder> GRADIENT_BORDER_CODEC = RecordCodecBuilder.create(i -> i.group(
			JadeCodecs.floatArrayCodec(4, Codec.FLOAT)
					.optionalFieldOf("boxProgressOffset")
					.forGetter($ -> Optional.ofNullable($.boxProgressOffset)),
			ColorPalette.CODEC.optionalFieldOf("boxProgressColors", ColorPalette.DEFAULT).forGetter($ -> $.boxProgressColors),
			JadeCodecs.intArrayCodec(4, Codec.INT).optionalFieldOf("padding").forGetter($ -> Optional.ofNullable($.padding)),
			Color.CODEC.optionalFieldOf("backgroundColor", -1).forGetter($ -> $.bgColor),
			JadeCodecs.intArrayCodec(4, Color.CODEC).fieldOf("borderColor").forGetter($ -> $.borderColor),
			Codec.FLOAT.optionalFieldOf("borderWidth", 1F).forGetter($ -> $.borderWidth),
			Codec.BOOL.optionalFieldOf("roundCorner").forGetter($ -> Optional.ofNullable($.roundCorner))
	).apply(i, GradientBorder::new));
	public static final Codec<SpriteBase> SPRITE_BASE_CODEC = RecordCodecBuilder.create(i -> i.group(
			JadeCodecs.floatArrayCodec(4, Codec.FLOAT)
					.optionalFieldOf("boxProgressOffset")
					.forGetter($ -> Optional.ofNullable($.boxProgressOffset)),
			ColorPalette.CODEC.optionalFieldOf("boxProgressColors", ColorPalette.DEFAULT).forGetter($ -> $.boxProgressColors),
			JadeCodecs.intArrayCodec(4, Codec.INT).optionalFieldOf("padding").forGetter($ -> Optional.ofNullable($.padding)),
			ResourceLocation.CODEC.fieldOf("sprite").forGetter($ -> $.sprite),
			ResourceLocation.CODEC.optionalFieldOf("withIconSprite").forGetter($ -> Optional.ofNullable($.withIconSprite))
	).apply(i, SpriteBase::new));
	public static final Codec<BoxStyle> CODEC = Codec.either(GRADIENT_BORDER_CODEC, SPRITE_BASE_CODEC).xmap(
			$ -> $.map(Function.identity(), Function.identity()),
			$ -> $ instanceof GradientBorder ? Either.left((GradientBorder) $) : Either.right((SpriteBase) $));
	public final float[] boxProgressOffset;
	public final int[] padding;
	public ColorPalette boxProgressColors;

	public BoxStyle(Optional<float[]> boxProgressOffset, ColorPalette boxProgressColors, Optional<int[]> padding) {
		this.boxProgressOffset = boxProgressOffset.orElse(null);
		this.boxProgressColors = boxProgressColors;
		this.padding = padding.orElseGet(DEFAULT_PADDING::clone);
	}

	public static BoxStyle getNestedBox() {
		return IThemeHelper.get().theme().nestedBoxStyle;
	}

	public static BoxStyle getViewGroup() {
		return IThemeHelper.get().theme().viewGroupStyle;
	}

	public static GradientBorder getTransparent() {
		return GradientBorder.TRANSPARENT;
	}

	public static BoxStyle getSprite(ResourceLocation sprite, @Nullable int[] padding) {
		return new BoxStyle.SpriteBase(Optional.empty(), ColorPalette.DEFAULT, Optional.ofNullable(padding), sprite, Optional.empty());
	}

	public abstract void render(GuiGraphics guiGraphics, StyledElement element, float x, float y, float w, float h, float alpha);

	public abstract float borderWidth();

	public float boxProgressOffset(ScreenDirection dir) {
		return boxProgressOffset == null ? 0 : boxProgressOffset[dir.ordinal()];
	}

	public int padding(ScreenDirection dir) {
		return MoreObjects.firstNonNull(padding, DEFAULT_PADDING)[dir.ordinal()];
	}

	@Override
	public abstract BoxStyle clone();

	public boolean hasRoundCorner() {
		return false;
	}

	public static class GradientBorder extends BoxStyle {
		public static final GradientBorder TRANSPARENT = new GradientBorder(
				Optional.empty(),
				ColorPalette.DEFAULT,
				Optional.empty(),
				-1,
				new int[]{-1, -1, -1, -1},
				0,
				Optional.of(false));
		public static final GradientBorder DEFAULT_NESTED_BOX = new GradientBorder(
				Optional.empty(),
				ColorPalette.DEFAULT,
				Optional.empty(),
				-1,
				new int[]{0xFF808080, 0xFF808080, 0xFF808080, 0xFF808080},
				1,
				Optional.empty());
		public static final GradientBorder DEFAULT_VIEW_GROUP = new GradientBorder(
				Optional.empty(),
				ColorPalette.DEFAULT,
				Optional.of(new int[]{2, 2, 2, 2}),
				0x44444444,
				new int[]{0x44444444, 0x44444444, 0x44444444, 0x44444444},
				0.75F,
				Optional.empty());
		public int bgColor;
		public int[] borderColor;
		public float borderWidth;
		@Nullable
		public Boolean roundCorner;

		private GradientBorder(
				Optional<float[]> boxProgressOffset,
				ColorPalette boxProgressColors,
				Optional<int[]> padding,
				int bgColor,
				int[] borderColor,
				float borderWidth,
				Optional<Boolean> roundCorner) {
			super(boxProgressOffset, boxProgressColors, padding);
			this.bgColor = bgColor;
			this.borderColor = borderColor;
			this.borderWidth = borderWidth;
			this.roundCorner = roundCorner.orElse(null);
		}

		@Override
		public float borderWidth() {
			return borderWidth;
		}

		@Override
		public void render(GuiGraphics guiGraphics, StyledElement element, float x, float y, float w, float h, float alpha) {
			boolean roundCorner = hasRoundCorner();
			if (bgColor != -1) {
				int bg = IWailaConfig.Overlay.applyAlpha(bgColor, alpha);
				DisplayHelper.INSTANCE.drawGradientRect(
						guiGraphics,
						x + borderWidth,
						y + borderWidth,
						w - borderWidth - borderWidth,
						h - borderWidth - borderWidth,
						bg,
						bg);//center
				if (roundCorner) {
					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x, y - 1, w, 1, bg, bg);
					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x, y + h, w, 1, bg, bg);
					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x - 1, y, 1, h, bg, bg);
					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x + w, y, 1, h, bg, bg);
				}
			}
			if (borderWidth > 0) {
				int[] borderColors = new int[4];
				for (int i = 0; i < 4; i++) {
					if (borderColor[i] != -1) {
						borderColors[i] = IWailaConfig.Overlay.applyAlpha(borderColor[i], alpha);
					}
				}
				DisplayHelper.INSTANCE.drawGradientRect(
						guiGraphics,
						x,
						y + borderWidth,
						borderWidth,
						h - borderWidth - borderWidth,
						borderColors[0],
						borderColors[3]);
				DisplayHelper.INSTANCE.drawGradientRect(
						guiGraphics,
						x + w - borderWidth,
						y + borderWidth,
						borderWidth,
						h - borderWidth - borderWidth,
						borderColors[1],
						borderColors[2]);
				DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x, y, w, borderWidth, borderColors[0], borderColors[1]);
				DisplayHelper.INSTANCE.drawGradientRect(
						guiGraphics,
						x,
						y + h - borderWidth,
						w,
						borderWidth,
						borderColors[3],
						borderColors[2]);
			}
		}

		@Override
		public GradientBorder clone() {
			return new GradientBorder(
					JadeCodecs.nullableClone(boxProgressOffset),
					boxProgressColors,
					JadeCodecs.nullableClone(padding),
					bgColor,
					borderColor,
					borderWidth,
					Optional.ofNullable(roundCorner));
		}

		@Override
		public boolean hasRoundCorner() {
			return roundCorner == null ? !IWailaConfig.get().overlay().getSquare() : roundCorner;
		}
	}

	public static class SpriteBase extends BoxStyle {
		public ResourceLocation sprite;
		@Nullable
		public ResourceLocation withIconSprite;

		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		public SpriteBase(
				Optional<float[]> boxProgressOffset,
				ColorPalette boxProgressColors,
				Optional<int[]> padding,
				ResourceLocation sprite,
				Optional<ResourceLocation> withIconSprite) {
			super(boxProgressOffset, boxProgressColors, padding);
			this.sprite = sprite;
			this.withIconSprite = withIconSprite.orElse(null);
		}

		public static void blitSprite(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int i, int j, int k, int l, int m) {
//			guiGraphics.blitSprite(RenderType::guiTextured, resourceLocation, i, j, k, l, m);
			GuiSpriteManager sprites = Minecraft.getInstance().getGuiSprites();
			TextureAtlasSprite textureAtlasSprite = sprites.getSprite(resourceLocation);
			GuiSpriteScaling guiSpriteScaling = sprites.getSpriteScaling(textureAtlasSprite);
			switch (guiSpriteScaling) {
				case GuiSpriteScaling.Stretch ignored -> guiGraphics.blitSprite(
						RenderType::guiTextured,
						resourceLocation,
						i,
						j,
						k,
						l,
						m);
				case GuiSpriteScaling.Tile tile -> guiGraphics.blitTiledSprite(
						RenderType::guiTextured,
						textureAtlasSprite,
						i,
						j,
						k,
						l,
						0,
						0,
						tile.width(),
						tile.height(),
						tile.width(),
						tile.height(),
						m);
				case GuiSpriteScaling.NineSlice nineSlice -> blitNineSlicedSprite(
						RenderType::guiTextured,
						guiGraphics,
						textureAtlasSprite,
						nineSlice,
						i,
						j,
						k,
						l,
						m);
				default -> {
				}
			}
		}

		public static void blitNineSlicedSprite(
				Function<ResourceLocation, RenderType> function,
				GuiGraphics guiGraphics,
				TextureAtlasSprite textureAtlasSprite,
				GuiSpriteScaling.NineSlice nineSlice,
				int i,
				int j,
				int k,
				int l,
				int m) {
			GuiSpriteScaling.NineSlice.Border border = nineSlice.border();
			int n = Math.min(border.left(), k / 2);
			int o = Math.min(border.right(), k / 2);
			int p = Math.min(border.top(), l / 2);
			int q = Math.min(border.bottom(), l / 2);
			if (k == nineSlice.width() && l == nineSlice.height()) {
				guiGraphics.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, i, j, k, l, m);
			} else if (l == nineSlice.height()) {
				guiGraphics.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, i, j, n, l, m);
				guiGraphics.blitNineSliceInnerSegment(
						function,
						nineSlice,
						textureAtlasSprite,
						i + n,
						j,
						k - o - n,
						l,
						n,
						0,
						nineSlice.width() - o - n,
						nineSlice.height(),
						nineSlice.width(),
						nineSlice.height(),
						m);
				guiGraphics.blitSprite(
						function,
						textureAtlasSprite,
						nineSlice.width(),
						nineSlice.height(),
						nineSlice.width() - o,
						0,
						i + k - o,
						j,
						o,
						l,
						m);
			} else if (k == nineSlice.width()) {
				guiGraphics.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, i, j, k, p, m);
				guiGraphics.blitNineSliceInnerSegment(
						function,
						nineSlice,
						textureAtlasSprite,
						i,
						j + p,
						k,
						l - q - p,
						0,
						p,
						nineSlice.width(),
						nineSlice.height() - q - p,
						nineSlice.width(),
						nineSlice.height(),
						m);
				guiGraphics.blitSprite(
						function,
						textureAtlasSprite,
						nineSlice.width(),
						nineSlice.height(),
						0,
						nineSlice.height() - q,
						i,
						j + l - q,
						k,
						q,
						m);
			} else {
				guiGraphics.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, i, j, n, p, m);
				guiGraphics.blitNineSliceInnerSegment(
						function,
						nineSlice,
						textureAtlasSprite,
						i + n,
						j,
						k - o - n,
						p,
						n,
						0,
						nineSlice.width() - o - n,
						p,
						nineSlice.width(),
						nineSlice.height(),
						m);
				guiGraphics.blitSprite(
						function,
						textureAtlasSprite,
						nineSlice.width(),
						nineSlice.height(),
						nineSlice.width() - o,
						0,
						i + k - o,
						j,
						o,
						p,
						m);
				guiGraphics.blitSprite(
						function,
						textureAtlasSprite,
						nineSlice.width(),
						nineSlice.height(),
						0,
						nineSlice.height() - q,
						i,
						j + l - q,
						n,
						q,
						m);
				guiGraphics.blitNineSliceInnerSegment(
						function,
						nineSlice,
						textureAtlasSprite,
						i + n,
						j + l - q,
						k - o - n,
						q,
						n,
						nineSlice.height() - q,
						nineSlice.width() - o - n,
						q,
						nineSlice.width(),
						nineSlice.height(),
						m);
				guiGraphics.blitSprite(
						function,
						textureAtlasSprite,
						nineSlice.width(),
						nineSlice.height(),
						nineSlice.width() - o,
						nineSlice.height() - q,
						i + k - o,
						j + l - q,
						o,
						q,
						m);
				guiGraphics.blitNineSliceInnerSegment(
						function,
						nineSlice,
						textureAtlasSprite,
						i,
						j + p,
						n,
						l - q - p,
						0,
						p,
						n,
						nineSlice.height() - q - p,
						nineSlice.width(),
						nineSlice.height(),
						m);
				guiGraphics.blitNineSliceInnerSegment(
						function,
						nineSlice,
						textureAtlasSprite,
						i + n,
						j + p,
						k - o - n,
						l - q - p,
						n,
						p,
						nineSlice.width() - o - n,
						nineSlice.height() - q - p,
						nineSlice.width(),
						nineSlice.height(),
						m);
				guiGraphics.blitNineSliceInnerSegment(
						function,
						nineSlice,
						textureAtlasSprite,
						i + k - o,
						j + p,
						o,
						l - q - p,
						nineSlice.width() - o,
						p,
						o,
						nineSlice.height() - q - p,
						nineSlice.width(),
						nineSlice.height(),
						m);
			}
		}

		@Override
		public void render(GuiGraphics guiGraphics, StyledElement element, float x, float y, float w, float h, float alpha) {
			ResourceLocation texture = sprite;
			if (withIconSprite != null && element.getIcon() != null) {
				texture = withIconSprite;
			}
			blitSprite(guiGraphics, texture, Math.round(x), Math.round(y), Math.round(w), Math.round(h), ARGB.white(alpha));
		}

		@Override
		public float borderWidth() {
			return 0;
		}

		@Override
		public SpriteBase clone() {
			return new SpriteBase(
					JadeCodecs.nullableClone(boxProgressOffset),
					boxProgressColors,
					JadeCodecs.nullableClone(padding),
					sprite,
					Optional.ofNullable(withIconSprite));
		}
	}

}
