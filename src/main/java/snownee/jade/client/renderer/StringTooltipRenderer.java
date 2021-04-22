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
public class StringTooltipRenderer implements ITooltipRenderer {
	@Override
	public Dimension getSize(CompoundNBT tag, ICommonAccessor accessor) {
		int ox = tag.getInt("x");
		int oy = tag.getInt("y");
		String s = tag.getString("text");
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		return new Dimension(ox + fontRenderer.getStringWidth(s), oy + (s.isEmpty() ? 0 : 8));
	}

	@Override
	public void draw(CompoundNBT tag, ICommonAccessor accessor, int x, int y) {
		int ox = tag.getInt("x");
		int oy = tag.getInt("y");
		String s = tag.getString("text");
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
		fontRenderer.drawStringWithShadow(new MatrixStack(), s, x + ox, y + oy, color.getFontColor());
	}

}
