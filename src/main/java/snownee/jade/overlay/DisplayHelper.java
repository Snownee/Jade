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
import snownee.jade.api.ui.Color;
import snownee.jade.api.ui.IDisplayHelper;
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

	private static void renderGuiItemDecorations(GuiGraphics guiGraphics, Font font, ItemStack stack, int i, int j, @Nullable String text) {
		if (stack.isEmpty()) {
			return;
		}
		guiGraphics.pose().pushMatrix();
		guiGraphics.renderItemBar(stack, i, j);
		if (stack.getCount() != 1 || text != null) {
			String s = text == null ? INSTANCE.humanReadableNumber(stack.getCount(), "", false, null) : text;
			boolean smaller = s.length() > 3;
			float scale = smaller ? 0.5F : 0.75F;
			int x = smaller ? 32 : 22;
			int y = smaller ? 23 : 13;
			guiGraphics.pose().pushMatrix();
			//FIXME
//			guiGraphics.pose().translate(0.0f, 0.0f, 200.0f);
			guiGraphics.pose().scale(scale);
			int color = IThemeHelper.get().theme().text.itemAmountColor();
			guiGraphics.drawString(font, s, i + x - font.width(s), j + y, color, true);
			guiGraphics.pose().popMatrix();
		}
		guiGraphics.pose().popMatrix();
		ClientProxy.renderItemDecorationsExtra(guiGraphics, font, stack, i, j, text);
	}

	public static void fill(GuiGraphics guiGraphics, float minX, float minY, float maxX, float maxY, int color) {
		fill(guiGraphics, RenderPipelines.GUI, minX, minY, maxX, maxY, color);
	}

	public static void fill(
			GuiGraphics guiGraphics,
			RenderPipeline renderPipeline,
			float minX,
			float minY,
			float maxX,
			float maxY,
			int color) {
		guiGraphics.guiRenderState.submitGuiElement(new FloatColoredRectangleRenderState(
				renderPipeline,
				TextureSetup.noTexture(),
				new Matrix3x2f(guiGraphics.pose()),
				minX,
				minY,
				maxX,
				maxY,
				color,
				color,
				guiGraphics.scissorStack.peek()
		));
	}

	@Override
	public void drawItem(GuiGraphics guiGraphics, float x, float y, ItemStack stack, float scale, @Nullable String text) {
		if (opacity() < 0.5F) {
			return;
		}
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(x, y);
		guiGraphics.pose().scale(scale);
		guiGraphics.renderFakeItem(stack, 0, 0);
		renderGuiItemDecorations(guiGraphics, font(), stack, 0, 0, text);
		guiGraphics.pose().popMatrix();
	}

	@Override
	public void drawGradientRect(GuiGraphics guiGraphics, float left, float top, float width, float height, int startColor, int endColor) {
		drawGradientRect(guiGraphics, left, top, width, height, startColor, endColor, false);
	}

	public void drawGradientRect(
			GuiGraphics guiGraphics,
			float left,
			float top,
			float width,
			float height,
			int startColor,
			int endColor,
			boolean horizontal) {
		if (startColor == -1 && endColor == -1) {
			return;
		}

//		float zLevel = 0.0F;
//		Matrix4f matrix = guiGraphics.pose().last().pose();
//
//		startColor = Overlay.applyAlpha(startColor, opacity());
//		endColor = Overlay.applyAlpha(endColor, opacity());
//		VertexConsumer buffer = guiGraphics.bufferSource.getBuffer(RenderType.gui());
//		if (horizontal) {
//			buffer.addVertex(matrix, left + width, top, zLevel).setColor(endColor);
//			buffer.addVertex(matrix, left, top, zLevel).setColor(startColor);
//			buffer.addVertex(matrix, left, top + height, zLevel).setColor(startColor);
//			buffer.addVertex(matrix, left + width, top + height, zLevel).setColor(endColor);
//		} else {
//			buffer.addVertex(matrix, left + width, top, zLevel).setColor(startColor);
//			buffer.addVertex(matrix, left, top, zLevel).setColor(startColor);
//			buffer.addVertex(matrix, left, top + height, zLevel).setColor(endColor);
//			buffer.addVertex(matrix, left + width, top + height, zLevel).setColor(endColor);
//		}
//		guiGraphics.flush();
	}

	@Override
	public void drawBorder(
			GuiGraphics guiGraphics,
			float minX,
			float minY,
			float maxX,
			float maxY,
			float width,
			int color,
			boolean corner) {
		fill(guiGraphics, minX + width, minY, maxX - width, minY + width, color);
		fill(guiGraphics, minX + width, maxY - width, maxX - width, maxY, color);
		if (corner) {
			fill(guiGraphics, minX, minY, minX + width, maxY, color);
			fill(guiGraphics, maxX - width, minY, maxX, maxY, color);
		} else {
			fill(guiGraphics, minX, minY + width, minX + width, maxY - width, color);
			fill(guiGraphics, maxX - width, minY + width, maxX, maxY - width, color);
		}
	}

	public void drawFluid(
			GuiGraphics guiGraphics,
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
						fill(guiGraphics, xPosition, maxY - scaledAmount.floatValue(), xPosition + width, maxY, color);
					} else {
						if (opacity() != 1) {
							color = Overlay.applyAlpha(color, opacity());
						}
						blitTiledSprite(
								guiGraphics,
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

	private void blitSprite(
			GuiGraphics guiGraphics,
			RenderPipeline renderPipeline,
			TextureAtlasSprite textureAtlasSprite,
			int i,
			int j,
			int k,
			int l,
			float x,
			float y,
			float w,
			float h,
			int color) {
		if (w == 0 || h == 0) {
			return;
		}
		this.innerBlit(
				guiGraphics,
				renderPipeline,
				textureAtlasSprite.atlasLocation(),
				x,
				x + w,
				y,
				y + h,
				textureAtlasSprite.getU(k / i),
				textureAtlasSprite.getU((k + w) / i),
				textureAtlasSprite.getV(l / j),
				textureAtlasSprite.getV((l + h) / j),
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
	public void drawText(GuiGraphics guiGraphics, String text, float x, float y, int color) {
		drawText(guiGraphics, Component.literal(text), x, y, color);
	}

	@Override
	public void drawText(GuiGraphics guiGraphics, FormattedText text, float x, float y, int color) {
		FormattedCharSequence sequence;
		if (text instanceof Component component) {
			sequence = component.getVisualOrderText();
		} else {
			sequence = Language.getInstance().getVisualOrder(text);
		}
		drawText(guiGraphics, sequence, x, y, color);
	}

	@Override
	public void drawText(GuiGraphics guiGraphics, FormattedCharSequence text, float x, float y, int color) {
		boolean shadow = IWailaConfig.get().overlay().getTheme().text.shadow();
		if (opacity() != 1) {
			color = Overlay.applyAlpha(color, opacity());
		}
		guiGraphics.drawString(font(), text, (int) x, (int) y, color, shadow);
	}

	public void drawGradientProgress(
			GuiGraphics guiGraphics,
			float left,
			float top,
			float width,
			float height,
			float progress,
			int progressColor) {
		Color color = Color.rgb(progressColor);
		Color highlight = Color.hsl(color.getHue(), color.getSaturation(), Math.min(color.getLightness() + 0.2, 1), color.getOpacity());
		if (progress < 0.1F) {
			drawGradientRect(guiGraphics, left, top, width * progress, height, progressColor, highlight.toInt(), true);
		} else {
			float hlWidth = width * 0.1F;
			float normalWidth = width * progress - hlWidth;
			fill(guiGraphics, left, top, left + normalWidth, top + height, progressColor);
			drawGradientRect(guiGraphics, left + normalWidth, top, hlWidth, height, progressColor, highlight.toInt(), true);
		}
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
			GuiGraphics guiGraphics,
			RenderPipeline renderPipeline,
			ResourceLocation resourceLocation,
			int i,
			int j,
			int k,
			int l) {
		guiGraphics.blitSprite(renderPipeline, resourceLocation, i, j, k, l, ARGB.white(opacity()));
	}

	@Override
	public void blitSprite(
			GuiGraphics guiGraphics,
			RenderPipeline renderPipeline,
			ResourceLocation resourceLocation,
			int i,
			int j,
			int k,
			int l,
			int m) {
		guiGraphics.blitSprite(renderPipeline, resourceLocation, i, j, k, l, ARGB.color(ARGB.as8BitChannel(opacity()), m));
	}

	@Override
	public void blitSprite(
			GuiGraphics guiGraphics,
			RenderPipeline renderPipeline,
			ResourceLocation resourceLocation,
			int i,
			int j,
			int k,
			int l,
			int m,
			int n,
			int o,
			int p) {
		guiGraphics.blitSprite(renderPipeline, resourceLocation, i, j, k, l, m, n, o, p);
	}

	private void blitTiledSprite(
			GuiGraphics guiGraphics,
			RenderPipeline renderPipeline,
			TextureAtlasSprite textureAtlasSprite,
			float i,
			float j,
			float k,
			float l,
			int m,
			int n,
			int o,
			int p,
			int q,
			int r,
			int color
	) {
		if (k > 0 && l > 0) {
			if (o > 0 && p > 0) {
				for (int t = 0; t < k; t += o) {
					float u = Math.min(o, k - t);

					for (int v = 0; v < l; v += p) {
						float w = Math.min(p, l - v);
						this.blitSprite(guiGraphics, renderPipeline, textureAtlasSprite, q, r, m, n, i + t, j + v, u, w, color);
					}
				}
			} else {
				throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + o + "x" + p);
			}
		}
	}

	public void blit(
			GuiGraphics guiGraphics,
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
		this.blit(guiGraphics, renderPipeline, resourceLocation, i, j, f, g, k, l, k, l, m, n, o);
	}

	public void blit(
			GuiGraphics guiGraphics,
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
		this.blit(guiGraphics, renderPipeline, resourceLocation, i, j, f, g, k, l, k, l, m, n);
	}

	public void blit(
			GuiGraphics guiGraphics,
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
		this.blit(guiGraphics, renderPipeline, resourceLocation, i, j, f, g, k, l, m, n, o, p, -1);
	}

	public void blit(
			GuiGraphics guiGraphics,
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
				guiGraphics,
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
			GuiGraphics guiGraphics,
			ResourceLocation resourceLocation,
			int i,
			int j,
			int k,
			int l,
			float f,
			float g,
			float h,
			float m) {
		this.innerBlit(guiGraphics, RenderPipelines.GUI_TEXTURED, resourceLocation, i, k, j, l, f, g, h, m, -1);
	}

	private void innerBlit(
			GuiGraphics guiGraphics,
			RenderPipeline renderPipeline,
			ResourceLocation resourceLocation,
			float x0,
			float x1,
			float y0,
			float y1,
			float u0,
			float v0,
			float u1,
			float v1,
			int color) {
		GpuTextureView gpuTextureView = Minecraft.getInstance().getTextureManager().getTexture(resourceLocation).getTextureView();
		this.submitBlit(guiGraphics, renderPipeline, gpuTextureView, x0, y0, x1, y1, u0, v0, u1, v1, color);
	}

	private void submitBlit(
			GuiGraphics guiGraphics,
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
		guiGraphics.guiRenderState
				.submitGuiElement(
						new FloatBlitRenderState(
								renderPipeline,
								TextureSetup.singleTexture(gpuTextureView),
								new Matrix3x2f(guiGraphics.pose()),
								x0,
								y0,
								x1,
								y1,
								u0,
								v0,
								u1,
								v1,
								color,
								guiGraphics.scissorStack.peek()
						)
				);
	}

	@Override
	public float opacity() {
		return OverlayRenderer.alpha;
	}

	public static Font font() {
		return FONT.get();
	}
}
