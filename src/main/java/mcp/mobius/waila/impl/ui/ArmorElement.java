package mcp.mobius.waila.impl.ui;

import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.overlay.IconUI;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class ArmorElement extends Element {

	private final float armor;

	public ArmorElement(float armor) {
		this.armor = armor;
	}

	@Override
	public Vec2 getSize() {
		int maxHearts = Waila.CONFIG.get().getGeneral().getMaxHeartsPerLine();
		float armor = this.armor;
		if (armor == -1)
			armor = maxHearts = 1;
		int lineCount = (int) (Math.ceil(armor / maxHearts));
		return new Vec2(8 * maxHearts, 10 * lineCount);
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		int maxHearts = Waila.CONFIG.get().getGeneral().getMaxHeartsPerLine();
		float armor = this.armor;
		if (armor == -1)
			armor = maxHearts = 1;
		int lineCount = (int) (Math.ceil(armor / maxHearts));
		int armorCount = lineCount * maxHearts;

		int xOffset = 0;
		for (int i = 1; i <= armorCount; i++) {
			if (i <= Mth.floor(armor)) {
				DisplayHelper.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.ARMOR);
				xOffset += 8;
			}

			if ((i > armor) && (i < armor + 1)) {
				DisplayHelper.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.HALF_ARMOR);
				xOffset += 8;
			}

			if (i >= armor + 1) {
				DisplayHelper.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.EMPTY_ARMOR);
				xOffset += 8;
			}

			if (i % maxHearts == 0) {
				y += 10;
				xOffset = 0;
			}

		}
	}
}
