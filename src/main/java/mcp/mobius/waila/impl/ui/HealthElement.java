package mcp.mobius.waila.impl.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.overlay.IconUI;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;

public class HealthElement extends Element {

	private final float maxHealth;
	private final float health;

	public HealthElement(float maxHealth, float health) {
		this.maxHealth = maxHealth;
		this.health = health;
	}

	@Override
	public Vector2f getSize() {
		float maxHearts = Waila.CONFIG.get().getGeneral().getMaxHeartsPerLine();

		int heartsPerLine = (int) (Math.min(maxHearts, Math.ceil(maxHealth)));
		int lineCount = (int) (Math.ceil(maxHealth / maxHearts));

		return new Vector2f(8 * heartsPerLine, 10 * lineCount);
	}

	@Override
	public void render(MatrixStack matrixStack, float x, float y, float maxX, float maxY) {
		float maxHearts = Waila.CONFIG.get().getGeneral().getMaxHeartsPerLine();

		int heartCount = MathHelper.ceil(maxHealth);
		int heartsPerLine = (int) (Math.min(maxHearts, Math.ceil(maxHealth)));

		int xOffset = 0;
		for (int i = 1; i <= heartCount; i++) {
			if (i <= MathHelper.floor(health)) {
				DisplayHelper.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.HEART);
				xOffset += 8;
			}

			if ((i > health) && (i < health + 1)) {
				DisplayHelper.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.HALF_HEART);
				xOffset += 8;
			}

			if (i >= health + 1) {
				DisplayHelper.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.EMPTY_HEART);
				xOffset += 8;
			}

			if (i % heartsPerLine == 0) {
				y += 10;
				xOffset = 0;
			}

		}
	}
}
