package mcp.mobius.waila.overlay.element;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.Element;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.overlay.DisplayUtil;
import mcp.mobius.waila.overlay.IconUI;
import net.minecraft.util.math.MathHelper;

public class ArmorElement extends Element {

    private final float armor;

    public ArmorElement(float armor) {
        this.armor = armor;
    }

    @Override
    public Size getSize() {
        float maxHearts = Waila.CONFIG.get().getGeneral().getMaxHeartsPerLine();
        float maxHealth = maxHearts;

        //FIXME magic number -1?
        int heartsPerLine = (int) (Math.min(maxHearts, Math.ceil(maxHealth)));
        int lineCount = (int) (Math.ceil(maxHealth / maxHearts));

        return new Size(8 * heartsPerLine, 10 * lineCount);
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y) {
        float maxHearts = Waila.CONFIG.get().getGeneral().getMaxHeartsPerLine();
        float armor = this.armor;
        if (armor == -1)
            maxHearts = armor = 1;
        float maxHealth = maxHearts;

        int heartCount = MathHelper.ceil(maxHealth);
        int heartsPerLine = (int) (Math.min(maxHearts, Math.ceil(maxHealth)));

        int xOffset = 0;
        for (int i = 1; i <= heartCount; i++) {
            if (i <= MathHelper.floor(armor)) {
                DisplayUtil.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.ARMOR);
                xOffset += 8;
            }

            if ((i > armor) && (i < armor + 1)) {
                DisplayUtil.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.HALF_ARMOR);
                xOffset += 8;
            }

            if (i >= armor + 1) {
                DisplayUtil.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.EMPTY_ARMOR);
                xOffset += 8;
            }

            if (i % heartsPerLine == 0) {
                y += 10;
                xOffset = 0;
            }

        }
    }
}
