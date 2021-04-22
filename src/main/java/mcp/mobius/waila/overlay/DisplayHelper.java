package mcp.mobius.waila.overlay;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import mcp.mobius.waila.api.ui.IBorderStyle;
import mcp.mobius.waila.api.ui.IDisplayHelper;
import mcp.mobius.waila.impl.ui.BorderStyle;
import mcp.mobius.waila.utils.WailaExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

@SuppressWarnings("deprecation")
public class DisplayHelper implements IDisplayHelper {

	public static final DisplayHelper INSTANCE = new DisplayHelper();
	private static final String[] NUM_SUFFIXES = new String[] { "", "k", "m", "b", "t" };
	private static final int MAX_LENGTH = 4;
	private static final Minecraft CLIENT = Minecraft.getInstance();

	@Override
	public void drawItem(MatrixStack matrixStack, int x, int y, ItemStack stack, float scale) {
		matrixStack.push();
		enable3DRender();
		try {
			renderItemIntoGUI(matrixStack, stack, x, y, scale);
			renderItemOverlayIntoGUI(matrixStack, CLIENT.fontRenderer, stack, x, y, null);
			renderStackSize(matrixStack, CLIENT.fontRenderer, stack, x, y);
		} catch (Exception e) {
			String stackStr = stack != null ? stack.toString() : "NullStack";
			WailaExceptionHandler.handleErr(e, "drawItem | " + stackStr, null);
		}
		enable2DRender();
		matrixStack.pop();
	}

	private static void renderItemOverlayIntoGUI(MatrixStack matrixStack, FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
		if (stack.isEmpty()) {
			return;
		}
		matrixStack.push();
		ItemRenderer renderer = CLIENT.getItemRenderer();
		if (stack.getCount() != 1 || text != null) {
			String s = text == null ? String.valueOf(stack.getCount()) : text;
			matrixStack.translate(0.0D, 0.0D, renderer.zLevel + 200.0F);
			IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			fr.renderString(s, xPosition + 19 - 2 - fr.getStringWidth(s), yPosition + 6 + 3, 16777215, true, matrixStack.getLast().getMatrix(), irendertypebuffer$impl, false, 0, 15728880);
			irendertypebuffer$impl.finish();
		}

		if (stack.getItem().showDurabilityBar(stack)) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.disableAlphaTest();
			RenderSystem.disableBlend();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			double health = stack.getItem().getDurabilityForDisplay(stack);
			int i = Math.round(13.0F - (float) health * 13.0F);
			int j = stack.getItem().getRGBDurabilityForDisplay(stack);
			draw(matrixStack, bufferbuilder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
			draw(matrixStack, bufferbuilder, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
			RenderSystem.enableBlend();
			RenderSystem.enableAlphaTest();
			RenderSystem.enableTexture();
			RenderSystem.enableDepthTest();
		}

		ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;
		float f3 = clientplayerentity == null ? 0.0F : clientplayerentity.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getInstance().getRenderPartialTicks());
		if (f3 > 0.0F) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			Tessellator tessellator1 = Tessellator.getInstance();
			BufferBuilder bufferbuilder1 = tessellator1.getBuffer();
			draw(matrixStack, bufferbuilder1, xPosition, yPosition + MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
			RenderSystem.enableTexture();
			RenderSystem.enableDepthTest();
		}
		matrixStack.pop();
	}

	private static void draw(MatrixStack ms, BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
		Matrix4f matrix = ms.getLast().getMatrix();
		renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		renderer.pos(matrix, x, y, 0).color(red, green, blue, alpha).endVertex();
		renderer.pos(matrix, x, y + height, 0).color(red, green, blue, alpha).endVertex();
		renderer.pos(matrix, x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
		renderer.pos(matrix, x + width, y, 0).color(red, green, blue, alpha).endVertex();
		Tessellator.getInstance().draw();
	}

	public static void renderItemIntoGUI(MatrixStack matrixStack, ItemStack stack, int x, int y, float scale) {
		ItemRenderer renderer = CLIENT.getItemRenderer();
		renderItemModelIntoGUI(matrixStack, stack, x, y, renderer.getItemModelWithOverrides(stack, (World) null, (LivingEntity) null), scale);
	}

	private static void renderItemModelIntoGUI(MatrixStack matrixStack, ItemStack stack, int x, int y, IBakedModel bakedmodel, float scale) {
		ItemRenderer renderer = CLIENT.getItemRenderer();
		TextureManager textureManager = CLIENT.textureManager;
		matrixStack.push();
		textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmapDirect(false, false);
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		matrixStack.translate(x, y, 100.0F + renderer.zLevel);
		matrixStack.translate(8.0F * scale, 8.0F * scale, 0.0F);
		matrixStack.scale(scale, -scale, scale);
		matrixStack.scale(16.0F, 16.0F, 16.0F);
		IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		boolean flag = !bakedmodel.isSideLit();
		if (flag) {
			RenderHelper.setupGuiFlatDiffuseLighting();
		}

		renderer.renderItem(stack, ItemCameraTransforms.TransformType.GUI, false, matrixStack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
		irendertypebuffer$impl.finish();
		RenderSystem.enableDepthTest();
		if (flag) {
			RenderHelper.setupGui3DDiffuseLighting();
		}

		RenderSystem.disableAlphaTest();
		RenderSystem.disableRescaleNormal();
		matrixStack.pop();
	}

	private static void renderStackSize(MatrixStack matrixStack, FontRenderer fr, ItemStack stack, int xPosition, int yPosition) {
		if (!stack.isEmpty() && stack.getCount() != 1) {
			String s = shortHandNumber(stack.getCount());

			if (stack.getCount() < 1)
				s = TextFormatting.RED + String.valueOf(stack.getCount());

			RenderSystem.disableLighting();
			RenderSystem.disableDepthTest();
			RenderSystem.disableBlend();
			RenderSystem.translated(0, 0, Minecraft.getInstance().getItemRenderer().zLevel + 200F);
			fr.drawStringWithShadow(matrixStack, s, xPosition + 19 - 2 - fr.getStringWidth(s), yPosition + 6 + 3, 16777215);
			RenderSystem.enableLighting();
			RenderSystem.enableDepthTest();
			RenderSystem.enableBlend();
		}
	}

	private static String shortHandNumber(Number number) {
		String shorthand = new DecimalFormat("##0E0").format(number);
		shorthand = shorthand.replaceAll("E[0-9]", NUM_SUFFIXES[Character.getNumericValue(shorthand.charAt(shorthand.length() - 1)) / 3]);
		while (shorthand.length() > MAX_LENGTH || shorthand.matches("[0-9]+\\.[a-z]"))
			shorthand = shorthand.substring(0, shorthand.length() - 2) + shorthand.substring(shorthand.length() - 1);

		return shorthand;
	}

	public static void enable3DRender() {
		RenderSystem.enableLighting();
		RenderSystem.enableDepthTest();
	}

	public static void enable2DRender() {
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();
	}

	@Override
	public void drawGradientRect(MatrixStack matrixStack, float left, float top, float right, float bottom, int startColor, int endColor) {
		float zLevel = 0.0F;
		Matrix4f matrix = matrixStack.getLast().getMatrix();

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
		RenderSystem.disableAlphaTest();
		RenderSystem.blendFuncSeparate(770, 771, 1, 0);
		RenderSystem.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(matrix, left + right, top, zLevel).color(f1, f2, f3, f).endVertex();
		buffer.pos(matrix, left, top, zLevel).color(f1, f2, f3, f).endVertex();
		buffer.pos(matrix, left, top + bottom, zLevel).color(f5, f6, f7, f4).endVertex();
		buffer.pos(matrix, left + right, top + bottom, zLevel).color(f5, f6, f7, f4).endVertex();
		tessellator.draw();
		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture();
	}

	@Override
	public void drawBorder(MatrixStack matrixStack, int minX, int minY, int maxX, int maxY, IBorderStyle border0) {
		BorderStyle border = (BorderStyle) border0;
		AbstractGui.fill(matrixStack, minX + border.width, minY, maxX - border.width, minY + border.width, border.color);
		AbstractGui.fill(matrixStack, minX + border.width, maxY - border.width, maxX - border.width, maxY, border.color);
		AbstractGui.fill(matrixStack, minX, minY, minX + border.width, maxY, border.color);
		AbstractGui.fill(matrixStack, maxX - border.width, minY, maxX, maxY, border.color);
	}

	public static void drawTexturedModalRect(MatrixStack matrixStack, int x, int y, int textureX, int textureY, int width, int height, int tw, int th) {
		Matrix4f matrix = matrixStack.getLast().getMatrix();
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		float zLevel = 0.0F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(matrix, x, y + height, zLevel).tex(((textureX) * f), ((textureY + th) * f1)).endVertex();
		buffer.pos(matrix, x + width, y + height, zLevel).tex(((textureX + tw) * f), ((textureY + th) * f1)).endVertex();
		buffer.pos(matrix, x + width, y, zLevel).tex(((textureX + tw) * f), ((textureY) * f1)).endVertex();
		buffer.pos(matrix, x, y, zLevel).tex(((textureX) * f), ((textureY) * f1)).endVertex();
		tessellator.draw();
	}

	public static List<ITextComponent> itemDisplayNameMultiline(ItemStack itemstack) {
		List<ITextComponent> namelist = null;
		try {
			namelist = itemstack.getTooltip(CLIENT.player, ITooltipFlag.TooltipFlags.NORMAL);
		} catch (Throwable ignored) {
		}

		if (namelist == null)
			namelist = new ArrayList<>();

		if (namelist.isEmpty())
			namelist.add(new StringTextComponent("Unnamed"));

		namelist.set(0, new StringTextComponent(itemstack.getRarity().color.toString() + namelist.get(0)));
		for (int i = 1; i < namelist.size(); i++)
			namelist.set(i, namelist.get(i));

		return namelist;
	}

	public static void renderIcon(MatrixStack matrixStack, int x, int y, int sx, int sy, IconUI icon) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		CLIENT.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);

		if (icon == null)
			return;

		RenderSystem.enableAlphaTest();
		if (icon.bu != -1)
			DisplayHelper.drawTexturedModalRect(matrixStack, x, y, icon.bu, icon.bv, sx, sy, icon.bsu, icon.bsv);
		DisplayHelper.drawTexturedModalRect(matrixStack, x, y, icon.u, icon.v, sx, sy, icon.su, icon.sv);
		RenderSystem.disableAlphaTest();
	}

	//https://github.com/mezz/JustEnoughItems/blob/1.16/src/main/java/mezz/jei/plugins/vanilla/ingredients/fluid/FluidStackRenderer.java
	private static final NumberFormat nf = NumberFormat.getIntegerInstance();
	private static final int TEX_WIDTH = 16;
	private static final int TEX_HEIGHT = 16;
	private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible

	public void drawFluid(MatrixStack matrixStack, final int xPosition, final int yPosition, @Nullable FluidStack fluidStack, int width, int height, int capacityMb) {
		if (fluidStack == null) {
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
		int scaledAmount = (amount * height) / capacityMb;
		if (amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
			scaledAmount = MIN_FLUID_HEIGHT;
		}
		if (scaledAmount > height) {
			scaledAmount = height;
		}

		drawTiledSprite(matrixStack, xPosition, yPosition, width, height, fluidColor, scaledAmount, fluidStillSprite);
	}

	private void drawTiledSprite(MatrixStack matrixStack, final int xPosition, final int yPosition, final int tiledWidth, final int tiledHeight, int color, int scaledAmount, TextureAtlasSprite sprite) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
		Matrix4f matrix = matrixStack.getLast().getMatrix();
		setGLColorFromInt(color);

		final int xTileCount = tiledWidth / TEX_WIDTH;
		final int xRemainder = tiledWidth - (xTileCount * TEX_WIDTH);
		final int yTileCount = scaledAmount / TEX_HEIGHT;
		final int yRemainder = scaledAmount - (yTileCount * TEX_HEIGHT);

		final int yStart = yPosition + tiledHeight;

		for (int xTile = 0; xTile <= xTileCount; xTile++) {
			for (int yTile = 0; yTile <= yTileCount; yTile++) {
				int width = (xTile == xTileCount) ? xRemainder : TEX_WIDTH;
				int height = (yTile == yTileCount) ? yRemainder : TEX_HEIGHT;
				int x = xPosition + (xTile * TEX_WIDTH);
				int y = yStart - ((yTile + 1) * TEX_HEIGHT);
				if (width > 0 && height > 0) {
					int maskTop = TEX_HEIGHT - height;
					int maskRight = TEX_WIDTH - width;

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
		return minecraft.getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fluidStill);
	}

	@SuppressWarnings("deprecation")
	private static void setGLColorFromInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;
		float alpha = ((color >> 24) & 0xFF) / 255F;

		RenderSystem.color4f(red, green, blue, alpha);
	}

	private static void drawTextureWithMasking(Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, float zLevel) {
		float uMin = textureSprite.getMinU();
		float uMax = textureSprite.getMaxU();
		float vMin = textureSprite.getMinV();
		float vMax = textureSprite.getMaxV();
		uMax = uMax - (maskRight / 16F * (uMax - uMin));
		vMax = vMax - (maskTop / 16F * (vMax - vMin));

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.pos(matrix, xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
		bufferBuilder.pos(matrix, xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
		bufferBuilder.pos(matrix, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
		bufferBuilder.pos(matrix, xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
		tessellator.draw();
	}
}
