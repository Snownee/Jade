package snownee.jade.client.renderer;

import java.awt.Dimension;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.api.impl.config.WailaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SubStringTooltipRenderer implements ITooltipRenderer {
    @Override
    public Dimension getSize(CompoundNBT tag, ICommonAccessor accessor) {
        return new Dimension(0, 0);
    }

    @Override
    public void draw(CompoundNBT tag, ICommonAccessor accessor, int x, int y) {
        String s = tag.getString("text");
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.translate(x - 4, y + 6, 800);
        matrixStack.scale(0.75f, 0.75f, 0);
        fontRenderer.drawStringWithShadow(matrixStack, s, 0, 0, color.getFontColor());
    }

}
