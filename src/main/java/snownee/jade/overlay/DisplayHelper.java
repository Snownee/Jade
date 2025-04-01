package snownee.jade.overlay;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IBorderStyle;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.impl.ui.BorderStyle;
import snownee.jade.util.Color;

public class DisplayHelper implements IDisplayHelper {

	public static final DisplayHelper INSTANCE = new DisplayHelper();
	private static final Minecraft CLIENT = Minecraft.getInstance();

	@Override
	public void drawItem(PoseStack matrixStack, float x, float y, ItemStack stack, float scale, @Nullable String text) {
		if (OverlayRenderer.alpha < 0.5F) {
			return;
		}
		RenderSystem.enableDepthTest();

		PoseStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushPose();
		modelViewStack.mulPoseMatrix(matrixStack.last().pose());
		float o = 8 * scale;
		modelViewStack.translate(x + o, y + o, 0);
		scale *= Math.min(1, OverlayRenderer.alpha + 0.2F);
		modelViewStack.scale(scale, scale, scale);
		modelViewStack.translate(-8, -8, 0);
		CLIENT.getItemRenderer().renderGuiItem(stack, 0, 0);
		renderGuiItemDecorations(matrixStack, CLIENT.font, stack, 0, 0, text);
		modelViewStack.popPose();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.disableDepthTest();
	}

	private static void renderGuiItemDecorations(PoseStack posestack, Font font, ItemStack stack, int i, int j, @Nullable String p_115179_) {
		if (stack.isEmpty()) {
			return;
		}
		posestack = new PoseStack();
		if (stack.getCount() != 1 || p_115179_ != null) {
			String s = p_115179_ == null ? INSTANCE.humanReadableNumber(stack.getCount(), "", false) : p_115179_;
			posestack.pushPose();
			posestack.translate(0.0f, 0.0f, 200.0f);
			posestack.scale(.75f, .75f, .75f);
			MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			font.drawInBatch(s, i + 22 - font.width(s), j + 13, 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 0xF000F0);
			multibuffersource$buffersource.endBatch();
			posestack.popPose();
		}

		if (stack.isBarVisible()) {
			posestack.pushPose();
			RenderSystem.disableDepthTest();
			int k = stack.getBarWidth();
			int l = stack.getBarColor();
			int m = i + 2;
			int n = j + 13;
			GuiComponent.fill(posestack, m, n, m + 13, n + 2, -16777216);
			GuiComponent.fill(posestack, m, n, m + k, n + 1, l | 0xFF000000);
			RenderSystem.enableDepthTest();
			posestack.popPose();
		}
	}

	@Override
	public void drawGradientRect(PoseStack matrixStack, float left, float top, float width, float height, int startColor, int endColor) {
		drawGradientRect(matrixStack, left, top, width, height, startColor, endColor, false);
	}

	public void drawGradientRect(PoseStack matrixStack, float left, float top, float width, float height, int startColor, int endColor, boolean horizontal) {
		float zLevel = 0.0F;
		Matrix4f matrix = matrixStack.last().pose();

		float f = (startColor >> 24 & 255) / 255.0F * OverlayRenderer.alpha;
		float f1 = (startColor >> 16 & 255) / 255.0F;
		float f2 = (startColor >> 8 & 255) / 255.0F;
		float f3 = (startColor & 255) / 255.0F;
		float f4 = (endColor >> 24 & 255) / 255.0F * OverlayRenderer.alpha;
		float f5 = (endColor >> 16 & 255) / 255.0F;
		float f6 = (endColor >> 8 & 255) / 255.0F;
		float f7 = (endColor & 255) / 255.0F;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		if (horizontal) {
			buffer.vertex(matrix, left + width, top, zLevel).color(f5, f6, f7, f4).endVertex();
			buffer.vertex(matrix, left, top, zLevel).color(f1, f2, f3, f).endVertex();
			buffer.vertex(matrix, left, top + height, zLevel).color(f1, f2, f3, f).endVertex();
			buffer.vertex(matrix, left + width, top + height, zLevel).color(f5, f6, f7, f4).endVertex();
		} else {
			buffer.vertex(matrix, left + width, top, zLevel).color(f1, f2, f3, f).endVertex();
			buffer.vertex(matrix, left, top, zLevel).color(f1, f2, f3, f).endVertex();
			buffer.vertex(matrix, left, top + height, zLevel).color(f5, f6, f7, f4).endVertex();
			buffer.vertex(matrix, left + width, top + height, zLevel).color(f5, f6, f7, f4).endVertex();
		}
		tessellator.end();
		RenderSystem.disableBlend();
	}

	@Override
	public void drawBorder(PoseStack matrixStack, float minX, float minY, float maxX, float maxY, IBorderStyle border0) {
		BorderStyle border = (BorderStyle) border0;
		drawBorder(matrixStack, minX, minY, maxX, maxY, border.width, border.color, true);
	}

	@Override
	public void drawBorder(PoseStack matrixStack, float minX, float minY, float maxX, float maxY, float width, int color, boolean corner) {
		fill(matrixStack, minX + width, minY, maxX - width, minY + width, color);
		fill(matrixStack, minX + width, maxY - width, maxX - width, maxY, color);
		if (corner) {
			fill(matrixStack, minX, minY, minX + width, maxY, color);
			fill(matrixStack, maxX - width, minY, maxX, maxY, color);
		} else {
			fill(matrixStack, minX, minY + width, minX + width, maxY - width, color);
			fill(matrixStack, maxX - width, minY + width, maxX, maxY - width, color);
		}
	}

	public static void drawTexturedModalRect(PoseStack matrixStack, float x, float y, int textureX, int textureY, int width, int height, int tw, int th) {
		Matrix4f matrix = matrixStack.last().pose();
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		float zLevel = 0.0F;
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.vertex(matrix, x, y + height, zLevel).uv(((textureX) * f), ((textureY + th) * f1)).endVertex();
		buffer.vertex(matrix, x + width, y + height, zLevel).uv(((textureX + tw) * f), ((textureY + th) * f1)).endVertex();
		buffer.vertex(matrix, x + width, y, zLevel).uv(((textureX + tw) * f), ((textureY) * f1)).endVertex();
		buffer.vertex(matrix, x, y, zLevel).uv(((textureX) * f), ((textureY) * f1)).endVertex();
		tessellator.end();
	}

	public static void renderIcon(PoseStack matrixStack, float x, float y, int sx, int sy, IconUI icon) {
		if (icon == null)
			return;

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, OverlayRenderer.alpha);
		RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		if (icon.bu != -1)
			DisplayHelper.drawTexturedModalRect(matrixStack, x, y, icon.bu, icon.bv, sx, sy, icon.bsu, icon.bsv);
		DisplayHelper.drawTexturedModalRect(matrixStack, x, y, icon.u, icon.v, sx, sy, icon.su, icon.sv);
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	//https://github.com/mezz/JustEnoughItems/blob/1.16/src/main/java/mezz/jei/plugins/vanilla/ingredients/fluid/FluidStackRenderer.java
	private static final int TEX_WIDTH = 16;
	private static final int TEX_HEIGHT = 16;
	private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible

	public void drawFluid(PoseStack matrixStack, final float xPosition, final float yPosition, JadeFluidObject fluid, float width, float height, long capacityMb) {
		if (OverlayRenderer.alpha < 0.5F) {
			return;
		}
		if (fluid.isEmpty()) {
			return;
		}
		Fluid type = fluid.getType();
		FluidVariant variant = FluidVariant.of(type, fluid.getTag());
		FluidVariantRenderHandler handler = FluidVariantRendering.getHandlerOrDefault(type);
		TextureAtlasSprite fluidStillSprite = handler.getSprites(variant)[0];
		int fluidColor = handler.getColor(variant, Minecraft.getInstance().level, null);

		long amount = JadeFluidObject.bucketVolume();
		float scaledAmount = (amount * height) / capacityMb;
		if (amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
			scaledAmount = MIN_FLUID_HEIGHT;
		}
		if (scaledAmount > height) {
			scaledAmount = height;
		}

		drawTiledSprite(matrixStack, xPosition, yPosition, width, height, fluidColor, scaledAmount, fluidStillSprite);
	}

	private void drawTiledSprite(PoseStack matrixStack, final float xPosition, final float yPosition, final float tiledWidth, final float tiledHeight, int color, float scaledAmount, TextureAtlasSprite sprite) {
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		Matrix4f matrix = matrixStack.last().pose();
		setGLColorFromInt(color);

		final int xTileCount = (int) (tiledWidth / TEX_WIDTH);
		final float xRemainder = tiledWidth - (xTileCount * TEX_WIDTH);
		final int yTileCount = (int) (scaledAmount / TEX_HEIGHT);
		final float yRemainder = scaledAmount - (yTileCount * TEX_HEIGHT);

		final float yStart = yPosition + tiledHeight;

		for (int xTile = 0; xTile <= xTileCount; xTile++) {
			for (int yTile = 0; yTile <= yTileCount; yTile++) {
				float width = (xTile == xTileCount) ? xRemainder : TEX_WIDTH;
				float height = (yTile == yTileCount) ? yRemainder : TEX_HEIGHT;
				float x = xPosition + (xTile * TEX_WIDTH);
				float y = yStart - ((yTile + 1) * TEX_HEIGHT);
				if (width > 0 && height > 0) {
					float maskTop = TEX_HEIGHT - height;
					float maskRight = TEX_WIDTH - width;

					drawTextureWithMasking(matrix, x, y, sprite, maskTop, maskRight, 100);
				}
			}
		}
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	private static void setGLColorFromInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;
		float alpha = ((color >> 24) & 0xFF) / 255F;

		RenderSystem.setShaderColor(red, green, blue, alpha);
	}

	private static void drawTextureWithMasking(Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, float maskTop, float maskRight, float zLevel) {
		float uMin = textureSprite.getU0();
		float uMax = textureSprite.getU1();
		float vMin = textureSprite.getV0();
		float vMax = textureSprite.getV1();
		uMax = uMax - (maskRight / 16F * (uMax - uMin));
		vMax = vMax - (maskTop / 16F * (vMax - vMin));

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(matrix, xCoord, yCoord + 16, zLevel).uv(uMin, vMax).endVertex();
		bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + 16, zLevel).uv(uMax, vMax).endVertex();
		bufferBuilder.vertex(matrix, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).uv(uMax, vMin).endVertex();
		bufferBuilder.vertex(matrix, xCoord, yCoord + maskTop, zLevel).uv(uMin, vMin).endVertex();
		tessellator.end();
	}

	public static void fill(PoseStack matrixStack, float minX, float minY, float maxX, float maxY, int color) {
		fill(matrixStack.last().pose(), minX, minY, maxX, maxY, color);
	}

	private static void fill(Matrix4f matrix, float minX, float minY, float maxX, float maxY, int color) {
		if (minX < maxX) {
			float i = minX;
			minX = maxX;
			maxX = i;
		}

		if (minY < maxY) {
			float j = minY;
			minY = maxY;
			maxY = j;
		}

		float f3 = (color >> 24 & 255) / 255.0F * OverlayRenderer.alpha;
		float f = (color >> 16 & 255) / 255.0F;
		float f1 = (color >> 8 & 255) / 255.0F;
		float f2 = (color & 255) / 255.0F;
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferbuilder.vertex(matrix, minX, maxY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(matrix, maxX, maxY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(matrix, maxX, minY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(matrix, minX, minY, 0.0F).color(f, f1, f2, f3).endVertex();
		Tesselator.getInstance().end();
		RenderSystem.disableBlend();
	}

	public static DecimalFormat dfCommas = new DecimalFormat("##.##");

	static {
		dfCommas.setRoundingMode(RoundingMode.DOWN);
	}

	// https://programming.guide/worlds-most-copied-so-snippet.html
	@Override
	public String humanReadableNumber(double number, String unit, boolean milli) {
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
		if (number < 1000) {
			sb.append(dfCommas.format(number));
			if (milli && number != 0) {
				sb.append('m');
			}
		} else {
			int exp = (int) (Math.log10(number) / 3);
			if (exp > 7)
				exp = 7;
			char pre = "kMGTPEZ".charAt(exp - 1);
			sb.append(dfCommas.format(number / Math.pow(1000, exp)));
			sb.append(pre);
		}
		sb.append(unit);
		return sb.toString();
	}

	@Override
	public void drawText(PoseStack poseStack, String text, float x, float y, int color) {
		drawText(poseStack, new TextComponent(text), x, y, color);
	}

	@Override
	public void drawText(PoseStack poseStack, FormattedText text, float x, float y, int color) {
		FormattedCharSequence sequence;
		if (text instanceof Component component) {
			sequence = component.getVisualOrderText();
		} else {
			sequence = Language.getInstance().getVisualOrder(text);
		}
		drawText(poseStack, sequence, x, y, color);
	}

	@Override
	public void drawText(PoseStack poseStack, FormattedCharSequence text, float x, float y, int color) {
		boolean shadow = Jade.CONFIG.get().getOverlay().getTheme().textShadow;
		if (OverlayRenderer.alpha != 1) {
			color = IConfigOverlay.applyAlpha(color, OverlayRenderer.alpha);
		}
		if (shadow) {
			CLIENT.font.drawShadow(poseStack, text, x, y, color);
		} else {
			CLIENT.font.draw(poseStack, text, x, y, color);
		}
	}

	public void drawGradientProgress(PoseStack matrixStack, float left, float top, float width, float height, float progress, int progressColor) {
		Color color = Color.rgb(progressColor);
		Color highlight = Color.hsl(color.getHue(), color.getSaturation(), Math.min(color.getLightness() + 0.2, 1), color.getOpacity());
		if (progress < 0.1F) {
			drawGradientRect(matrixStack, left, top, width * progress, height, progressColor, highlight.toInt(), true);
		} else {
			float hlWidth = width * 0.1F;
			float normalWidth = width * progress - hlWidth;
			fill(matrixStack, left, top, left + normalWidth, top + height, progressColor);
			drawGradientRect(matrixStack, left + normalWidth, top, hlWidth, height, progressColor, highlight.toInt(), true);
		}
	}

	private static final Pattern STRIP_COLOR = Pattern.compile("(?i)\u00a7[0-9A-F]");

	@Override
	public MutableComponent stripColor(Component component) {
		MutableComponent mutableComponent = TextComponent.EMPTY.copy();
		component.visit((style, string) -> {
			if (!string.isEmpty()) {
				MutableComponent literal = new TextComponent(STRIP_COLOR.matcher(string).replaceAll(""));
				literal.withStyle(style.withColor((TextColor) null));
				mutableComponent.append(literal);
			}
			return Optional.empty();
		}, Style.EMPTY);
		return mutableComponent;
	}
}
