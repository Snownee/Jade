package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.Identifiers;
import snownee.jade.api.ui.Element;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.IconUI;
import snownee.jade.overlay.OverlayRenderer;

public class HealthElement extends Element {

	private final float maxHealth;
	private final float health;
	private String text;

	public HealthElement(float maxHealth, float health) {
		if (!PluginConfig.INSTANCE.get(Identifiers.MC_ENTITY_HEALTH_SHOW_FRACTIONS)) {
			maxHealth = Mth.ceil(maxHealth);
			health = Mth.ceil(health);
		}
		this.maxHealth = maxHealth;
		this.health = health;
		text = String.format("  %s/%s", DisplayHelper.dfCommas.format(health), DisplayHelper.dfCommas.format(maxHealth));
	}

	@Override
	public Vec2 getSize() {
		if (maxHealth > PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_MAX_FOR_RENDER)) {
			Font font = Minecraft.getInstance().font;
			return new Vec2(8 + font.width(text), 10);
		} else {
			float maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);

			float maxHealth = this.maxHealth * 0.5F;
			int heartsPerLine = (int) (Math.min(maxHearts, Math.ceil(maxHealth)));
			int lineCount = (int) (Math.ceil(maxHealth / maxHearts));

			return new Vec2(8 * heartsPerLine, 10 * lineCount);
		}
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		float maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
		int maxHeartsForRender = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_MAX_FOR_RENDER);

		int heartCount = maxHealth > maxHeartsForRender ? 1 : Mth.ceil(maxHealth * 0.5F);
		float health = maxHealth > maxHeartsForRender ? 1 : (this.health * 0.5F);
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

		if (maxHealth > maxHeartsForRender) {
			DisplayHelper.INSTANCE.drawText(matrixStack, text, x + 8, y, OverlayRenderer.normalTextColorRaw);
		}
	}

	@Override
	public @Nullable String getMessage() {
		return I18n.get("narration.jade.health", DisplayHelper.dfCommas.format(health));
	}
}
