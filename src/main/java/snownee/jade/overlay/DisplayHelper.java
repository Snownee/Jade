package snownee.jade.overlay;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.Overlay;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.Rect2f;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.JadeFont;

public class DisplayHelper implements IDisplayHelper {

	public static final DisplayHelper INSTANCE = new DisplayHelper();
	//https://github.com/mezz/JustEnoughItems/blob/1.16/src/main/java/mezz/jei/plugins/vanilla/ingredients/fluid/FluidStackRenderer.java
	private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible
	private static final Pattern STRIP_COLOR = Pattern.compile("(?i)\u00a7[0-9A-F]");
	public static DecimalFormat dfCommas = new DecimalFormat("0.##");
	public static final DecimalFormat[] dfCommasArray = new DecimalFormat[]{dfCommas, new DecimalFormat("0.#"), new DecimalFormat("0")};
	private static final Supplier<JadeFont> FONT = Suppliers.memoize(() -> new JadeFont(Minecraft.getInstance().font));

	static {
		for (DecimalFormat format : dfCommasArray) {
			format.setRoundingMode(RoundingMode.DOWN);
		}
	}

	private static void renderGuiItemDecorations(GuiGraphics graphics, Font font, ItemStack stack, int i, int j, @Nullable String text) {
		if (stack.isEmpty()) {
			return;
		}
		graphics.pose().pushMatrix();
		graphics.renderItemBar(stack, i, j);
		if (stack.getCount() != 1 || text != null) {
			String s = text == null ? INSTANCE.humanReadableNumber(stack.getCount(), "", false, null) : text;
			boolean smaller = s.length() > 3;
			float scale = smaller ? 0.5F : 0.75F;
			int x = smaller ? 32 : 22;
			int y = smaller ? 23 : 13;
			graphics.pose().pushMatrix();
			//FIXME
//			graphics.pose().translate(0.0f, 0.0f, 200.0f);
			graphics.pose().scale(scale);
			int color = IThemeHelper.get().theme().text.itemAmountColor();
			graphics.drawString(font, s, i + x - font.width(s), j + y, color, true);
			graphics.pose().popMatrix();
		}
		graphics.pose().popMatrix();
		ClientProxy.renderItemDecorationsExtra(graphics, font, stack, i, j, text);
	}

	public static void fill(GuiGraphics graphics, float minX, float minY, float maxX, float maxY, int color) {
		fill(graphics, RenderPipelines.GUI, minX, minY, maxX, maxY, color);
	}

	public static void fill(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			float minX,
			float minY,
			float maxX,
			float maxY,
			int color) {
		graphics.guiRenderState.submitGuiElement(new FloatColoredRectangleRenderState(
				renderPipeline,
				TextureSetup.noTexture(),
				new Matrix3x2f(graphics.pose()),
				minX,
				minY,
				maxX,
				maxY,
				color,
				color,
				graphics.scissorStack.peek()
		));
	}

	@Override
	public void drawItem(GuiGraphics graphics, float x, float y, ItemStack stack, float scale, @Nullable String text) {
		if (opacity() < 0.5F) {
			return;
		}
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale);
		graphics.renderFakeItem(stack, 0, 0);
		renderGuiItemDecorations(graphics, font(), stack, 0, 0, text);
		graphics.pose().popMatrix();
	}

	@Override
	public void drawBorder(GuiGraphics graphics, Rect2f rectangle, int width, int color, boolean corner) {
		float minX = rectangle.getX();
		float minY = rectangle.getY();
		float maxX = rectangle.getRight();
		float maxY = rectangle.getBottom();
		fill(graphics, minX + width, minY, maxX - width, minY + width, color);
		fill(graphics, minX + width, maxY - width, maxX - width, maxY, color);
		if (corner) {
			fill(graphics, minX, minY, minX + width, maxY, color);
			fill(graphics, maxX - width, minY, maxX, maxY, color);
		} else {
			fill(graphics, minX, minY + width, minX + width, maxY - width, color);
			fill(graphics, maxX - width, minY + width, maxX, maxY - width, color);
		}
	}

	public void drawFluid(
			GuiGraphics graphics,
			final float xPosition,
			final float yPosition,
			JadeFluidObject fluid,
			float width,
			float height,
			long capacityMb) {
		if (fluid.isEmpty()) {
			return;
		}

		long amount = JadeFluidObject.bucketVolume();
		MutableFloat scaledAmount = new MutableFloat((amount * height) / capacityMb);
		if (amount > 0 && scaledAmount.floatValue() < MIN_FLUID_HEIGHT) {
			scaledAmount.setValue(MIN_FLUID_HEIGHT);
		}
		if (scaledAmount.floatValue() > height) {
			scaledAmount.setValue(height);
		}

		ClientProxy.getFluidSpriteAndColor(
				fluid, (sprite, color) -> {
					if (sprite == null) {
						float maxY = yPosition + height;
						if (color == -1) {
							color = 0xAAAAAAAA;
						}
						fill(graphics, xPosition, maxY - scaledAmount.floatValue(), xPosition + width, maxY, color);
					} else {
						if (opacity() != 1) {
							color = Overlay.applyAlpha(color, opacity());
						}
						blitTiledSprite(
								graphics,
								RenderPipelines.GUI_TEXTURED,
								sprite,
								xPosition,
								yPosition,
								width,
								height,
								0,
								0,
								16,
								16,
								16,
								16,
								color);
					}
				});
	}

	public void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			TextureAtlasSprite textureAtlasSprite,
			float spriteWidth,
			float spriteHeight,
			float uStart,
			float vStart,
			float uSize,
			float vSize,
			float x,
			float y,
			float width,
			float height,
			int color) {
		if (width == 0 || height == 0) {
			return;
		}
		this.innerBlit(
				graphics,
				renderPipeline,
				textureAtlasSprite.atlasLocation(),
				x,
				x + width,
				y,
				y + height,
				textureAtlasSprite.getU(uStart / spriteWidth),
				textureAtlasSprite.getU((uStart + uSize) / spriteWidth),
				textureAtlasSprite.getV(vStart / spriteHeight),
				textureAtlasSprite.getV((vStart + vSize) / spriteHeight),
				color
		);
	}

	@Override
	public String humanReadableNumber(double number, String unit, boolean milli) {
		return humanReadableNumber(number, unit, milli, dfCommas);
	}

	// https://programming.guide/worlds-most-copied-so-snippet.html
	@Override
	public String humanReadableNumber(double number, String unit, boolean milli, @Nullable Format formatter) {
		if (Mth.equal(number, 0)) {
			return "0" + unit;
		}
		StringBuilder sb = new StringBuilder();
		boolean n = number < 0;
		if (n) {
			number = -number;
			sb.append('-');
		}
		if (milli && number >= 1000) {
			number /= 1000;
			milli = false;
		}
		int exp = formatter == null && number < 10000 ? 0 : (int) Math.log10(number) / 3;
		if (exp > 7) {
			exp = 7;
		}
		if (exp > 0) {
			number /= Math.pow(1000, exp);
		}
		if (formatter == null) {
			if (number < 10) {
				formatter = dfCommasArray[0];
			} else if (number < 100) {
				formatter = dfCommasArray[1];
			} else {
				formatter = dfCommasArray[2];
			}
		}
		if (formatter instanceof NumberFormat numberFormat) {
			sb.append(numberFormat.format(number));
		} else {
			sb.append(formatter.format(new Object[]{number}));
		}
		if (exp == 0) {
			if (milli) {
				sb.append('m');
			}
		} else {
			char pre = "kMGTPEZ".charAt(exp - 1);
			sb.append(pre);
		}
		sb.append(unit);
		return sb.toString();
	}

	@Override
	public void drawText(GuiGraphics graphics, String text, float x, float y, int color) {
		drawText(graphics, Component.literal(text), x, y, color);
	}

	@Override
	public void drawText(GuiGraphics graphics, FormattedText text, float x, float y, int color) {
		FormattedCharSequence sequence;
		if (text instanceof Component component) {
			sequence = component.getVisualOrderText();
		} else {
			sequence = Language.getInstance().getVisualOrder(text);
		}
		drawText(graphics, sequence, x, y, color);
	}

	@Override
	public void drawText(GuiGraphics graphics, FormattedCharSequence text, float x, float y, int color) {
		boolean shadow = IWailaConfig.get().overlay().getTheme().text.shadow();
		if (opacity() != 1) {
			color = Overlay.applyAlpha(color, opacity());
		}
		graphics.drawString(font(), text, (int) x, (int) y, color, shadow);
	}

	@Override
	public MutableComponent stripColor(Component component) {
		MutableComponent mutableComponent = Component.empty();
		component.visit(
				(style, string) -> {
					if (!string.isEmpty()) {
						MutableComponent literal = Component.literal(STRIP_COLOR.matcher(string).replaceAll(""));
						literal.withStyle(style.withColor((TextColor) null));
						mutableComponent.append(literal);
					}
					return Optional.empty();
				}, Style.EMPTY);
		return mutableComponent;
	}

	@Override
	public void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation sprite,
			int i,
			int j,
			int k,
			int l) {
		sprite = IThemeHelper.get().theme().mapSprite(sprite);
		graphics.blitSprite(renderPipeline, sprite, i, j, k, l, ARGB.white(opacity()));
	}

	@Override
	public void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation sprite,
			int i,
			int j,
			int k,
			int l,
			int color) {
		if (opacity() != 1) {
			float alpha = ARGB.alpha(color) / 255F;
			alpha *= opacity();
			color = ARGB.color(ARGB.as8BitChannel(alpha), color);
		}
		sprite = IThemeHelper.get().theme().mapSprite(sprite);
		graphics.blitSprite(renderPipeline, sprite, i, j, k, l, color);
	}

	@Override
	public void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation sprite,
			int spriteWidth,
			int spriteHeight,
			int uStart,
			int vStart,
			int x,
			int y,
			int width,
			int height) {
		sprite = IThemeHelper.get().theme().mapSprite(sprite);
		graphics.blitSprite(renderPipeline, sprite, spriteWidth, spriteHeight, uStart, vStart, x, y, width, height);
	}

	@Override
	public void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation sprite,
			int spriteWidth,
			int spriteHeight,
			int uStart,
			int vStart,
			int x,
			int y,
			int width,
			int height,
			int color) {
		sprite = IThemeHelper.get().theme().mapSprite(sprite);
		if (opacity() != 1) {
			float alpha = ARGB.alpha(color) / 255F;
			alpha *= opacity();
			color = ARGB.color(ARGB.as8BitChannel(alpha), color);
		}
		graphics.blitSprite(renderPipeline, sprite, spriteWidth, spriteHeight, uStart, vStart, x, y, width, height, color);
	}

	public void blitTiledSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			TextureAtlasSprite textureAtlasSprite,
			float x,
			float y,
			float width,
			float height,
			float uStart,
			float vStart,
			int tileWidth,
			int tileHeight,
			int spriteWidth,
			int spriteHeight,
			int color
	) {
		if (width <= 0 || height <= 0) {
			return;
		}
		if (tileWidth <= 0 || tileHeight <= 0) {
			throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + tileWidth + "x" + tileHeight);
		}
		for (int i = 0; i < width; i += tileWidth) {
			float u = Math.min(tileWidth, width - i);

			for (int j = 0; j < height; j += tileHeight) {
				float w = Math.min(tileHeight, height - j);
				this.blitSprite(
						graphics,
						renderPipeline,
						textureAtlasSprite,
						spriteWidth,
						spriteHeight,
						uStart,
						vStart,
						u,
						w,
						x + i,
						y + j,
						u,
						w,
						color);
			}
		}
	}

	public void blit(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation resourceLocation,
			int i,
			int j,
			float f,
			float g,
			int k,
			int l,
			int m,
			int n,
			int o) {
		this.blit(graphics, renderPipeline, resourceLocation, i, j, f, g, k, l, k, l, m, n, o);
	}

	public void blit(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation resourceLocation,
			int i,
			int j,
			float f,
			float g,
			int k,
			int l,
			int m,
			int n) {
		this.blit(graphics, renderPipeline, resourceLocation, i, j, f, g, k, l, k, l, m, n);
	}

	public void blit(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation resourceLocation,
			int i,
			int j,
			float f,
			float g,
			int k,
			int l,
			int m,
			int n,
			int o,
			int p) {
		this.blit(graphics, renderPipeline, resourceLocation, i, j, f, g, k, l, m, n, o, p, -1);
	}

	public void blit(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation resourceLocation,
			int i,
			int j,
			float f,
			float g,
			int k,
			int l,
			int m,
			int n,
			int o,
			int p,
			int q
	) {
		this.innerBlit(
				graphics,
				renderPipeline,
				resourceLocation,
				i,
				i + k,
				j,
				j + l,
				(f + 0.0F) / o,
				(f + m) / o,
				(g + 0.0F) / p,
				(g + n) / p,
				q);
	}

	public void blit(
			GuiGraphics graphics,
			ResourceLocation resourceLocation,
			int i,
			int j,
			int k,
			int l,
			float f,
			float g,
			float h,
			float m) {
		this.innerBlit(graphics, RenderPipelines.GUI_TEXTURED, resourceLocation, i, k, j, l, f, g, h, m, -1);
	}

	private void innerBlit(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation sprite,
			float x0,
			float x1,
			float y0,
			float y1,
			float u0,
			float v0,
			float u1,
			float v1,
			int color) {
		GpuTextureView gpuTextureView = Minecraft.getInstance().getTextureManager().getTexture(sprite).getTextureView();
		this.submitBlit(graphics, renderPipeline, gpuTextureView, x0, y0, x1, y1, u0, v0, u1, v1, color);
	}

	private void submitBlit(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			GpuTextureView gpuTextureView,
			float x0,
			float y0,
			float x1,
			float y1,
			float u0,
			float v0,
			float u1,
			float v1,
			int color) {
		graphics.guiRenderState
				.submitGuiElement(
						new FloatBlitRenderState(
								renderPipeline,
								TextureSetup.singleTexture(gpuTextureView),
								new Matrix3x2f(graphics.pose()),
								x0,
								y0,
								x1,
								y1,
								u0,
								v0,
								u1,
								v1,
								color,
								graphics.scissorStack.peek()
						)
				);
	}

	@Override
	public float opacity() {
		return OverlayRenderer.animation.alpha;
	}

	@Override
	public float backgroundOpacity() {
		return OverlayRenderer.animation.showHideAlpha;
	}

	public static Font font() {
		return FONT.get();
	}

	public void blitSprite(
			GuiGraphics graphics,
			RenderPipeline renderPipeline,
			ResourceLocation sprite,
			int spriteWidth,
			int spriteHeight,
			int u0,
			int v0,
			float x,
			float y,
			float width,
			float height,
			int color) {
		if (width == 0 || height == 0) {
			return;
		}
		if (opacity() != 1) {
			float alpha = ARGB.alpha(color) / 255F;
			alpha *= opacity();
			color = ARGB.color(ARGB.as8BitChannel(alpha), color);
		}
		sprite = IThemeHelper.get().theme().mapSprite(sprite);
		TextureAtlasSprite textureAtlasSprite = Minecraft.getInstance().getGuiSprites().getSprite(sprite);
		this.blitSprite(
				graphics,
				renderPipeline,
				textureAtlasSprite,
				spriteWidth,
				spriteHeight,
				u0,
				v0,
				width,
				height,
				x,
				y,
				width,
				height,
				color);
	}
}
