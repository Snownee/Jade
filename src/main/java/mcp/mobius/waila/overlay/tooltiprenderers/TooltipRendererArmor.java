package mcp.mobius.waila.overlay.tooltiprenderers;

import java.awt.Dimension;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.api.RenderContext;
import mcp.mobius.waila.overlay.DisplayUtil;
import mcp.mobius.waila.overlay.IconUI;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

public class TooltipRendererArmor implements ITooltipRenderer {

	@Override
	public Dimension getSize(CompoundNBT tag, ICommonAccessor accessor) {
		int maxHearts = Waila.CONFIG.get().getGeneral().getMaxHeartsPerLine();
		float armor = tag.getFloat("armor");
		if (armor == -1)
			armor = maxHearts = 1;
		int lineCount = (int) (Math.ceil(armor / maxHearts));
		return new Dimension(8 * maxHearts, 10 * lineCount);
	}

	@Override
	public void draw(CompoundNBT tag, ICommonAccessor accessor, int x, int y) {
		int maxHearts = Waila.CONFIG.get().getGeneral().getMaxHeartsPerLine();
		float armor = tag.getFloat("armor");
		if (armor == -1)
			armor = maxHearts = 1;
		int lineCount = (int) (Math.ceil(armor / maxHearts));
		int armorCount = lineCount * maxHearts;

		int xOffset = 0;
		for (int i = 1; i <= armorCount; i++) {
			if (i <= MathHelper.floor(armor)) {
				DisplayUtil.renderIcon(RenderContext.matrixStack, x + xOffset, y, 8, 8, IconUI.ARMOR);
				xOffset += 8;
			}

			if ((i > armor) && (i < armor + 1)) {
				DisplayUtil.renderIcon(RenderContext.matrixStack, x + xOffset, y, 8, 8, IconUI.HALF_ARMOR);
				xOffset += 8;
			}

			if (i >= armor + 1) {
				DisplayUtil.renderIcon(RenderContext.matrixStack, x + xOffset, y, 8, 8, IconUI.EMPTY_ARMOR);
				xOffset += 8;
			}

			if (i % maxHearts == 0) {
				y += 10;
				xOffset = 0;
			}

		}
	}
}
