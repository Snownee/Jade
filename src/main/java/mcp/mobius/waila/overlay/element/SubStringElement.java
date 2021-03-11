package mcp.mobius.waila.overlay.element;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.Element;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import mcp.mobius.waila.api.impl.config.WailaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.JadePlugin;

@OnlyIn(Dist.CLIENT)
public class SubStringElement extends Element {

    private final String text;

    public SubStringElement(String text) {
        this.text = text;
    }

    @Override
    public Size getSize() {
        return Size.ZERO;
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y) {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
        if (PluginConfig.INSTANCE.get(JadePlugin.HARVEST_TOOL_NEW_LINE)) {
            matrixStack.translate(x - 4, y + 7, 800);
        } else {
            matrixStack.translate(x - 4, y + 4, 800);
        }
        matrixStack.scale(0.75f, 0.75f, 0);
        fontRenderer.drawStringWithShadow(matrixStack, text, 0, 0, color.getFontColor());
    }

}
