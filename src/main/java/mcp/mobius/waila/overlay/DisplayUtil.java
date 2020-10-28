package mcp.mobius.waila.overlay;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mcp.mobius.waila.utils.WailaExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DisplayUtil {

    private static final String[] NUM_SUFFIXES = new String[]{"", "k", "m", "b", "t"};
    private static final int MAX_LENGTH = 4;
    private static final Minecraft CLIENT = Minecraft.getInstance();

    public static void renderStack(int x, int y, ItemStack stack) {
        enable3DRender();
        try {
            CLIENT.getItemRenderer().renderItemIntoGUI(stack, x, y);
            ItemStack overlayRender = stack.copy();
            overlayRender.setCount(1);
            CLIENT.getItemRenderer().renderItemOverlayIntoGUI(CLIENT.fontRenderer, overlayRender, x, y, null);
            renderStackSize(new MatrixStack(), CLIENT.fontRenderer, stack, x, y);
        } catch (Exception e) {
            String stackStr = stack != null ? stack.toString() : "NullStack";
            WailaExceptionHandler.handleErr(e, "renderStack | " + stackStr, null);
        }
        enable2DRender();
    }

    public static void renderStackSize(MatrixStack matrixStack, FontRenderer fr, ItemStack stack, int xPosition, int yPosition) {
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

    public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float zLevel = 0.0F;

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
        RenderSystem.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(left + right, top, zLevel).color(f1, f2, f3, f).endVertex();
        buffer.pos(left, top, zLevel).color(f1, f2, f3, f).endVertex();
        buffer.pos(left, top + bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        buffer.pos(left + right, top + bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height, int tw, int th) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        float zLevel = 0.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, zLevel).tex( ((textureX) * f), ((textureY + th) * f1)).endVertex();
        buffer.pos(x + width, y + height, zLevel).tex( ((textureX + tw) * f), ((textureY + th) * f1)).endVertex();
        buffer.pos(x + width, y, zLevel).tex( ((textureX + tw) * f), ((textureY) * f1)).endVertex();
        buffer.pos(x, y, zLevel).tex( ((textureX) * f), ((textureY) * f1)).endVertex();
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

    public static void renderIcon(int x, int y, int sx, int sy, IconUI icon) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        CLIENT.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);

        if (icon == null)
            return;

        RenderSystem.enableAlphaTest();
        if (icon.bu != -1)
            DisplayUtil.drawTexturedModalRect(x, y, icon.bu, icon.bv, sx, sy, icon.bsu, icon.bsv);
        DisplayUtil.drawTexturedModalRect(x, y, icon.u, icon.v, sx, sy, icon.su, icon.sv);
        RenderSystem.disableAlphaTest();
    }
}