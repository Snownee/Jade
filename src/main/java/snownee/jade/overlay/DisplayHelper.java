package snownee.jade.overlay;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import snownee.jade.WailaClient;
import snownee.jade.api.ui.IBorderStyle;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.impl.ui.BorderStyle;
import snownee.jade.util.WailaExceptionHandler;

@SuppressWarnings("deprecation")
public class DisplayHelper implements IDisplayHelper {

	public static final DisplayHelper INSTANCE = new DisplayHelper();

	//WTF is it???
	private static final Vector3f DIFFUSE_LIGHT_0 = new Vector3f(-0.5f, -0.1f, -0.1f);
	private static final Vector3f DIFFUSE_LIGHT_1 = new Vector3f(0, -1, 0);

	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static float blitOffset;

	@Override
	public void drawItem(PoseStack matrixStack, float x, float y, ItemStack stack, float scale, @Nullable String text) {
		matrixStack.pushPose();
		RenderSystem.enableDepthTest();
		try {
			//CLIENT.getItemRenderer().renderGuiItem(stack, (int) x, (int) y + 20);
			tryRenderGuiItem(matrixStack, stack, x, y, scale);
			renderGuiItemDecorations(matrixStack, CLIENT.font, stack, x, y, text);
			//renderStackSize(matrixStack, CLIENT.font, stack, x, y);
		} catch (Exception e) {
			String stackStr = stack != null ? stack.toString() : "NullStack";
			WailaExceptionHandler.handleErr(e, "drawItem | " + stackStr, null);
		}
		RenderSystem.disableDepthTest();
		matrixStack.popPose();
	}

	private static void renderGuiItemDecorations(PoseStack posestack, Font font, ItemStack stack, float p_115177_, float p_115178_, @Nullable String p_115179_) {
		if (stack.isEmpty()) {
			return;
		}
		ItemRenderer renderer = CLIENT.getItemRenderer();
		//PoseStack posestack = new PoseStack();
		if (stack.getCount() != 1 || p_115179_ != null) {
			String s = p_115179_ == null ? String.valueOf(stack.getCount()) : p_115179_;
			posestack.translate(0.0D, 0.0D, renderer.blitOffset + 200.0F);
			MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			font.drawInBatch(s, p_115177_ + 19 - 2 - font.width(s), p_115178_ + 6 + 3, 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
			multibuffersource$buffersource.endBatch();
		}

		if (stack.isBarVisible()) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.disableBlend();
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferbuilder = tesselator.getBuilder();
			double health = stack.getBarWidth();
			int i = Math.round(13.0F - (float) health * 13.0F);
			int j = stack.getBarColor();
			draw(posestack, bufferbuilder, p_115177_ + 2, p_115178_ + 13, 13, 2, 0, 0, 0, 255);
			draw(posestack, bufferbuilder, p_115177_ + 2, p_115178_ + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
			RenderSystem.enableBlend();
			RenderSystem.enableTexture();
			RenderSystem.enableDepthTest();
		}

		LocalPlayer localplayer = Minecraft.getInstance().player;
		float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
		if (f > 0.0F) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			Tesselator tesselator1 = Tesselator.getInstance();
			BufferBuilder bufferbuilder1 = tesselator1.getBuilder();
			draw(posestack, bufferbuilder1, p_115177_, p_115178_ + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
			RenderSystem.enableTexture();
			RenderSystem.enableDepthTest();
		}
	}

	private static void draw(PoseStack ms, BufferBuilder renderer, float x, float y, int width, int height, int red, int green, int blue, int alpha) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Matrix4f matrix = ms.last().pose();
		renderer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		renderer.vertex(matrix, x, y, 0).color(red, green, blue, alpha).endVertex();
		renderer.vertex(matrix, x, y + height, 0).color(red, green, blue, alpha).endVertex();
		renderer.vertex(matrix, x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
		renderer.vertex(matrix, x + width, y, 0).color(red, green, blue, alpha).endVertex();
		renderer.end();
		BufferUploader.end(renderer);
	}

	public static void tryRenderGuiItem(PoseStack matrixStack, ItemStack stack, float x, float y, float scale) {
		ItemRenderer renderer = CLIENT.getItemRenderer();
		renderGuiItem(matrixStack, stack, x, y, renderer.getModel(stack, null, null, 0), scale);
	}

	private static void renderGuiItem(PoseStack posestack, ItemStack p_115128_, float p_115129_, float p_115130_, BakedModel p_115131_, float scale) {
		ItemRenderer renderer = CLIENT.getItemRenderer();
		TextureManager textureManager = CLIENT.textureManager;
		textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
		RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		posestack.pushPose();

		RenderSystem.setShaderLights(DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);

		posestack.translate(p_115129_, p_115130_, 150.0F + blitOffset);
		posestack.translate(8.0D * scale, 8.0D * scale, 0.0D);
		posestack.scale(scale, -scale, scale);
		posestack.scale(16.0F, 16.0F, 16.0F);

		MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
		boolean flag = !p_115131_.usesBlockLight();
		if (flag) {
			Lighting.setupForFlatItems();
		}

		renderer.render(p_115128_, ItemTransforms.TransformType.GUI, false, posestack, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, p_115131_);
		multibuffersource$buffersource.endBatch();
		RenderSystem.enableDepthTest();
		Lighting.setupFor3DItems();

		posestack.popPose();
	}

	//	private static void renderStackSize(PoseStack matrixStack, Font fr, ItemStack stack, float xPosition, float yPosition) {
	//		if (!stack.isEmpty() && stack.getCount() != 1) {
	//			String s = shortHandNumber(stack.getCount());
	//
	//			if (stack.getCount() < 1)
	//				s = ChatFormatting.RED + String.valueOf(stack.getCount());
	//
	//			RenderSystem.disableLighting();
	//			RenderSystem.disableDepthTest();
	//			RenderSystem.disableBlend();
	//			matrixStack.pushPose();
	//			matrixStack.translate(0, 0, Minecraft.getInstance().getItemRenderer().blitOffset + 200F);
	//			fr.drawStringWithShadow(matrixStack, s, xPosition + 19 - 2 - fr.width(s), yPosition + 6 + 3, 16777215);
	//			matrixStack.popPose();
	//			RenderSystem.enableLighting();
	//			RenderSystem.enableDepthTest();
	//			RenderSystem.enableBlend();
	//		}
	//	}

	@Override
	public void drawGradientRect(PoseStack matrixStack, float left, float top, float right, float bottom, int startColor, int endColor) {
		drawGradientRect(matrixStack, left, top, right, bottom, startColor, endColor, false);
	}

	public void drawGradientRect(PoseStack matrixStack, float left, float top, float right, float bottom, int startColor, int endColor, boolean vertical) {
		float zLevel = 0.0F;
		Matrix4f matrix = matrixStack.last().pose();

		float f = (startColor >> 24 & 255) / 255.0F;
		float f1 = (startColor >> 16 & 255) / 255.0F;
		float f2 = (startColor >> 8 & 255) / 255.0F;
		float f3 = (startColor & 255) / 255.0F;
		float f4 = (endColor >> 24 & 255) / 255.0F;
		float f5 = (endColor >> 16 & 255) / 255.0F;
		float f6 = (endColor >> 8 & 255) / 255.0F;
		float f7 = (endColor & 255) / 255.0F;
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		if (vertical) {
			buffer.vertex(matrix, left + right, top, zLevel).color(f5, f6, f7, f4).endVertex();
			buffer.vertex(matrix, left, top, zLevel).color(f1, f2, f3, f).endVertex();
			buffer.vertex(matrix, left, top + bottom, zLevel).color(f1, f2, f3, f).endVertex();
			buffer.vertex(matrix, left + right, top + bottom, zLevel).color(f5, f6, f7, f4).endVertex();
		} else {
			buffer.vertex(matrix, left + right, top, zLevel).color(f1, f2, f3, f).endVertex();
			buffer.vertex(matrix, left, top, zLevel).color(f1, f2, f3, f).endVertex();
			buffer.vertex(matrix, left, top + bottom, zLevel).color(f5, f6, f7, f4).endVertex();
			buffer.vertex(matrix, left + right, top + bottom, zLevel).color(f5, f6, f7, f4).endVertex();
		}
		tessellator.end();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	@Override
	public void drawBorder(PoseStack matrixStack, float minX, float minY, float maxX, float maxY, IBorderStyle border0) {
		BorderStyle border = (BorderStyle) border0;
		fill(matrixStack, minX + border.width, minY, maxX - border.width, minY + border.width, border.color);
		fill(matrixStack, minX + border.width, maxY - border.width, maxX - border.width, maxY, border.color);
		fill(matrixStack, minX, minY, minX + border.width, maxY, border.color);
		fill(matrixStack, maxX - border.width, minY, maxX, maxY, border.color);
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

	public static List<Component> itemDisplayNameMultiline(ItemStack itemstack) {
		List<Component> namelist = null;
		try {
			namelist = itemstack.getTooltipLines(CLIENT.player, TooltipFlag.Default.NORMAL);
		} catch (Throwable ignored) {
		}

		if (namelist == null)
			namelist = new ArrayList<>();

		if (namelist.isEmpty())
			namelist.add(new TextComponent("Unnamed"));

		namelist.set(0, new TextComponent(itemstack.getRarity().color.toString() + namelist.get(0)));
		for (int i = 1; i < namelist.size(); i++)
			namelist.set(i, namelist.get(i));

		return namelist;
	}

	public static void renderIcon(PoseStack matrixStack, float x, float y, int sx, int sy, IconUI icon) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

		if (icon == null)
			return;

		//RenderSystem.enableAlphaTest();
		if (icon.bu != -1)
			DisplayHelper.drawTexturedModalRect(matrixStack, x, y, icon.bu, icon.bv, sx, sy, icon.bsu, icon.bsv);
		DisplayHelper.drawTexturedModalRect(matrixStack, x, y, icon.u, icon.v, sx, sy, icon.su, icon.sv);
		//RenderSystem.disableAlphaTest();
	}

	//https://github.com/mezz/JustEnoughItems/blob/1.16/src/main/java/mezz/jei/plugins/vanilla/ingredients/fluid/FluidStackRenderer.java
	private static final int TEX_WIDTH = 16;
	private static final int TEX_HEIGHT = 16;
	private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible

	public void drawFluid(PoseStack matrixStack, final float xPosition, final float yPosition, @Nullable FluidStack fluidStack, float width, float height, int capacityMb) {
		if (fluidStack == null || fluidStack.isEmpty()) {
			return;
		}
		Fluid fluid = fluidStack.getFluid();
		if (fluid == null) {
			return;
		}

		TextureAtlasSprite fluidStillSprite = getStillFluidSprite(fluidStack);

		FluidAttributes attributes = fluid.getAttributes();
		int fluidColor = attributes.getColor(fluidStack);

		int amount = fluidStack.getAmount();
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
	}

	private static TextureAtlasSprite getStillFluidSprite(FluidStack fluidStack) {
		Minecraft minecraft = Minecraft.getInstance();
		Fluid fluid = fluidStack.getFluid();
		FluidAttributes attributes = fluid.getAttributes();
		ResourceLocation fluidStill = attributes.getStillTexture(fluidStack);
		return minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
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

		float f3 = (color >> 24 & 255) / 255.0F;
		float f = (color >> 16 & 255) / 255.0F;
		float f1 = (color >> 8 & 255) / 255.0F;
		float f2 = (color & 255) / 255.0F;
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferbuilder.vertex(matrix, minX, maxY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(matrix, maxX, maxY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(matrix, maxX, minY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(matrix, minX, minY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
		RenderSystem.enableTexture();
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
			if (milli) {
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
		boolean shadow = WailaClient.CONFIG.get().getOverlay().getTheme().textShadow;
		if (shadow) {
			CLIENT.font.drawShadow(poseStack, text, x, y, color);
		} else {
			CLIENT.font.draw(poseStack, text, x, y, color);
		}
	}

	@Override
	public void drawText(PoseStack poseStack, Component text, float x, float y, int color) {
		boolean shadow = WailaClient.CONFIG.get().getOverlay().getTheme().textShadow;
		if (shadow) {
			CLIENT.font.drawShadow(poseStack, text, x, y, color);
		} else {
			CLIENT.font.draw(poseStack, text, x, y, color);
		}
	}
}
