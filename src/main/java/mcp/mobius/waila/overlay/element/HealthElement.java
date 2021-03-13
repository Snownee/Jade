package mcp.mobius.waila.overlay.element;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.api.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.overlay.IconUI;
import net.minecraft.util.math.MathHelper;

public class HealthElement extends Element {

    private final float maxHealth;
    private final float health;

    public HealthElement(float maxHealth, float health) {
        this.maxHealth = maxHealth;
        this.health = health;
    }

    @Override
    public Size getSize() {
        float maxHearts = Waila.CONFIG.get().getGeneral().getMaxHeartsPerLine();

        int heartsPerLine = (int) (Math.min(maxHearts, Math.ceil(maxHealth)));
        int lineCount = (int) (Math.ceil(maxHealth / maxHearts));

        return new Size(8 * heartsPerLine, 10 * lineCount);
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, int maxX, int maxY) {
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
