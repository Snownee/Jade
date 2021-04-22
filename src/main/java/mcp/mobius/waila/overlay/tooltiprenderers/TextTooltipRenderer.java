package mcp.mobius.waila.overlay.tooltiprenderers;

import java.awt.Dimension;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.api.impl.config.WailaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextTooltipRenderer implements ITooltipRenderer {
	@Override
	public Dimension getSize(CompoundNBT tag, ICommonAccessor accessor) {
		ITextComponent component = ITextComponent.Serializer.getComponentFromJson(tag.getString("text"));
		if (component == null) {
			return new Dimension();
		}
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		return new Dimension(fontRenderer.getStringWidth(component.getString()), fontRenderer.FONT_HEIGHT + 1);
	}

	@Override
	public void draw(CompoundNBT tag, ICommonAccessor accessor, int x, int y) {
		ITextComponent component = ITextComponent.Serializer.getComponentFromJson(tag.getString("text"));
		if (component == null) {
			return;
		}
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
		IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		fontRenderer.drawEntityText(component.func_241878_f(), x, y, color.getFontColor(), true, new MatrixStack().getLast().getMatrix(), irendertypebuffer$impl, false, 0, 15728880);
		irendertypebuffer$impl.finish();
	}

}
