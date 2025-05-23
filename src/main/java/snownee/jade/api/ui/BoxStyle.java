package snownee.jade.api.ui;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import snownee.jade.api.JadeIds;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.impl.ui.StyledElement;
import snownee.jade.util.JadeCodecs;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BoxStyle implements Cloneable {
	private static final int[] DEFAULT_PADDING = new int[]{3, 3, 3, 3};
	public static final Codec<BoxStyle> CODEC = RecordCodecBuilder.create(i -> i.group(
					JadeCodecs.floatArrayCodec(4, Codec.FLOAT)
							.optionalFieldOf("boxProgressOffset")
							.forGetter($ -> Optional.ofNullable($.boxProgressOffset)),
					ColorPalette.CODEC.optionalFieldOf("boxProgressColors", ColorPalette.DEFAULT).forGetter($ -> $.boxProgressColors),
					JadeCodecs.intArrayCodec(4, Codec.INT).optionalFieldOf("padding").forGetter($ -> Optional.ofNullable($.padding)),
					Codec.INT.optionalFieldOf("borderWidth", 1).forGetter($ -> $.borderWidth),
					ResourceLocation.CODEC.optionalFieldOf("sprite").forGetter($ -> Optional.ofNullable($.sprite)),
					ResourceLocation.CODEC.optionalFieldOf("withIconSprite").forGetter($ -> Optional.ofNullable($.withIconSprite)))
			.apply(i, BoxStyle::new));
	private static final BoxStyle TRANSPARENT = new BoxStyle(
			Optional.empty(),
			ColorPalette.DEFAULT,
			Optional.empty(),
			0,
			Optional.empty(),
			Optional.empty());
	public static final BoxStyle DEFAULT_NESTED_BOX = new BoxStyle(
			Optional.empty(),
			ColorPalette.DEFAULT,
			Optional.empty(),
			1,
			Optional.of(JadeIds.JADE("nested_box")),
			Optional.empty());
	public static final BoxStyle DEFAULT_VIEW_GROUP = new BoxStyle(
			Optional.empty(),
			ColorPalette.DEFAULT,
			Optional.of(new int[]{2, 2, 2, 2}),
			0,
			Optional.of(JadeIds.JADE("view_group")),
			Optional.empty());
	public final float[] boxProgressOffset;
	public final int[] padding;
	public int borderWidth;
	public ColorPalette boxProgressColors;
	@Nullable
	public ResourceLocation sprite;
	@Nullable
	public ResourceLocation withIconSprite;

	public BoxStyle(
			Optional<float[]> boxProgressOffset,
			ColorPalette boxProgressColors,
			Optional<int[]> padding,
			int borderWidth,
			Optional<ResourceLocation> sprite,
			Optional<ResourceLocation> withIconSprite) {
		this.boxProgressOffset = boxProgressOffset.orElse(null);
		this.boxProgressColors = boxProgressColors;
		this.padding = padding.orElseGet(DEFAULT_PADDING::clone);
		this.borderWidth = borderWidth;
		this.sprite = sprite.orElse(null);
		this.withIconSprite = withIconSprite.orElse(null);
	}

	public static BoxStyle getNestedBox() {
		return IThemeHelper.get().theme().nestedBoxStyle;
	}

	public static BoxStyle getViewGroup() {
		return IThemeHelper.get().theme().viewGroupStyle;
	}

	public static BoxStyle getTransparent() {
		return BoxStyle.TRANSPARENT;
	}

	public static BoxStyle getSprite(ResourceLocation sprite, @Nullable int[] padding) {
		return getSprite(sprite, padding, 1);
	}

	public static BoxStyle getSprite(ResourceLocation sprite, @Nullable int[] padding, int borderWidth) {
		return new BoxStyle(
				Optional.empty(),
				ColorPalette.DEFAULT,
				Optional.ofNullable(padding),
				borderWidth,
				Optional.ofNullable(sprite),
				Optional.empty());
	}

	public float boxProgressOffset(ScreenDirection dir) {
		return boxProgressOffset == null ? 0 : boxProgressOffset[dir.ordinal()];
	}

	public int padding(ScreenDirection dir) {
		return MoreObjects.firstNonNull(padding, DEFAULT_PADDING)[dir.ordinal()];
	}

	public void render(GuiGraphics guiGraphics, StyledElement element, float x, float y, float w, float h, float alpha) {
		ResourceLocation texture = sprite;
		if (withIconSprite != null && element.getIcon() != null) {
			texture = withIconSprite;
		}
		if (texture == null) {
			return;
		}
		int roundedX = Math.round(x);
		int roundedY = Math.round(y);
		int roundedW = Math.round(w);
		int roundedH = Math.round(h);
		int col = ARGB.white(alpha);
		roundedX = roundedX - 9;
		roundedY = roundedY - 9;
		roundedW = roundedW + 9 + 9;
		roundedH = roundedH + 9 + 9;
		guiGraphics.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				TooltipRenderUtil.getBackgroundSprite(texture),
				roundedX,
				roundedY,
				roundedW,
				roundedH,
				col);
		guiGraphics.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				TooltipRenderUtil.getFrameSprite(texture),
				roundedX,
				roundedY,
				roundedW,
				roundedH,
				col);
	}

	public int borderWidth() {
		return borderWidth;
	}

	@Override
	public BoxStyle clone() {
		return new BoxStyle(
				JadeCodecs.nullableClone(boxProgressOffset),
				boxProgressColors,
				JadeCodecs.nullableClone(padding),
				borderWidth,
				Optional.ofNullable(sprite),
				Optional.ofNullable(withIconSprite));
	}

//	public static class GradientBorder extends BoxStyle {
//		public static final GradientBorder TRANSPARENT = new GradientBorder(
//				Optional.empty(),
//				ColorPalette.DEFAULT,
//				Optional.empty(),
//				-1,
//				new int[]{-1, -1, -1, -1},
//				0,
//				Optional.of(false));
//		public static final GradientBorder DEFAULT_NESTED_BOX = new GradientBorder(
//				Optional.empty(),
//				ColorPalette.DEFAULT,
//				Optional.empty(),
//				-1,
//				new int[]{0xFF808080, 0xFF808080, 0xFF808080, 0xFF808080},
//				1,
//				Optional.empty());
//		public static final GradientBorder DEFAULT_VIEW_GROUP = new GradientBorder(
//				Optional.empty(),
//				ColorPalette.DEFAULT,
//				Optional.of(new int[]{2, 2, 2, 2}),
//				0x44444444,
//				new int[]{0x44444444, 0x44444444, 0x44444444, 0x44444444},
//				0.75F,
//				Optional.empty());
//		public int bgColor;
//		public int[] borderColor;
//		public float borderWidth;
//		@Nullable
//		public Boolean roundCorner;
//
//		private GradientBorder(
//				Optional<float[]> boxProgressOffset,
//				ColorPalette boxProgressColors,
//				Optional<int[]> padding,
//				int bgColor,
//				int[] borderColor,
//				float borderWidth,
//				Optional<Boolean> roundCorner) {
//			super(boxProgressOffset, boxProgressColors, padding);
//			this.bgColor = bgColor;
//			this.borderColor = borderColor;
//			this.borderWidth = borderWidth;
//			this.roundCorner = roundCorner.orElse(null);
//		}
//
//		@Override
//		public float borderWidth() {
//			return borderWidth;
//		}
//
//		@Override
//		public void render(GuiGraphics guiGraphics, StyledElement element, float x, float y, float w, float h, float alpha) {
//			boolean roundCorner = hasRoundCorner();
//			if (bgColor != -1) {
//				int bg = IWailaConfig.Overlay.applyAlpha(bgColor, alpha);
//				DisplayHelper.INSTANCE.drawGradientRect(
//						guiGraphics,
//						x + borderWidth,
//						y + borderWidth,
//						w - borderWidth - borderWidth,
//						h - borderWidth - borderWidth,
//						bg,
//						bg);//center
//				if (roundCorner) {
//					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x, y - 1, w, 1, bg, bg);
//					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x, y + h, w, 1, bg, bg);
//					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x - 1, y, 1, h, bg, bg);
//					DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x + w, y, 1, h, bg, bg);
//				}
//			}
//			if (borderWidth > 0) {
//				int[] borderColors = new int[4];
//				for (int i = 0; i < 4; i++) {
//					if (borderColor[i] != -1) {
//						borderColors[i] = IWailaConfig.Overlay.applyAlpha(borderColor[i], alpha);
//					}
//				}
//				DisplayHelper.INSTANCE.drawGradientRect(
//						guiGraphics,
//						x,
//						y + borderWidth,
//						borderWidth,
//						h - borderWidth - borderWidth,
//						borderColors[0],
//						borderColors[3]);
//				DisplayHelper.INSTANCE.drawGradientRect(
//						guiGraphics,
//						x + w - borderWidth,
//						y + borderWidth,
//						borderWidth,
//						h - borderWidth - borderWidth,
//						borderColors[1],
//						borderColors[2]);
//				DisplayHelper.INSTANCE.drawGradientRect(guiGraphics, x, y, w, borderWidth, borderColors[0], borderColors[1]);
//				DisplayHelper.INSTANCE.drawGradientRect(
//						guiGraphics,
//						x,
//						y + h - borderWidth,
//						w,
//						borderWidth,
//						borderColors[3],
//						borderColors[2]);
//			}
//		}
//	}
}
