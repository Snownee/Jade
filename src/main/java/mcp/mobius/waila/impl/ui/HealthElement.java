package mcp.mobius.waila.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.config.WailaConfig.ConfigGeneral;
import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import mcp.mobius.waila.overlay.IconUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class HealthElement extends Element {

	private final float maxHealth;
	private final float health;
	private String text;

	public HealthElement(float maxHealth, float health) {
		this.maxHealth = maxHealth;
		this.health = health;
		text = String.format("  %s/%s", DisplayHelper.dfCommas.format(health), DisplayHelper.dfCommas.format(maxHealth));
	}

	@Override
	public Vec2 getSize() {
		ConfigGeneral config = Waila.CONFIG.get().getGeneral();
		if (maxHealth > config.getMaxHealthForRender()) {
			Font font = Minecraft.getInstance().font;
			return new Vec2(8 + font.width(text), 10);
		} else {
			float maxHearts = config.getMaxHeartsPerLine();

			float maxHealth = this.maxHealth * 0.5F;
			int heartsPerLine = (int) (Math.min(maxHearts, Math.ceil(maxHealth)));
			int lineCount = (int) (Math.ceil(maxHealth / maxHearts));

			return new Vec2(8 * heartsPerLine, 10 * lineCount);
		}
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		ConfigGeneral config = Waila.CONFIG.get().getGeneral();
		float maxHearts = config.getMaxHeartsPerLine();

		int heartCount = maxHealth > config.getMaxHealthForRender() ? 1 : Mth.ceil(maxHealth * 0.5F);
		float health = maxHealth > config.getMaxHealthForRender() ? 1 : (this.health * 0.5F);
		int heartsPerLine = (int) (Math.min(maxHearts, Math.ceil(maxHealth)));

		int xOffset = 0;
		for (int i = 1; i <= heartCount; i++) {
			if (i <= Mth.floor(health)) {
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

		if (maxHealth > config.getMaxHealthForRender()) {
			WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
			DisplayHelper.INSTANCE.drawText(matrixStack, text, x + 8, y, color.getTheme().textColor);
		}
	}

	@Override
	public @Nullable Component getMessage() {
		return new TranslatableComponent("narration.waila.health", DisplayHelper.dfCommas.format(health));
	}
}
