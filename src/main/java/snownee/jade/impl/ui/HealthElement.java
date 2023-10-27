package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.Identifiers;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.overlay.DisplayHelper;

public class HealthElement extends Element {

	public static final ResourceLocation HEART = new ResourceLocation("hud/heart/full");
	public static final ResourceLocation HALF_HEART = new ResourceLocation("hud/heart/half");
	public static final ResourceLocation EMPTY_HEART = new ResourceLocation("hud/heart/container");

	private final float maxHealth;
	private final float health;
	private final String text;

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
			return new Vec2(9 + font.width(text), 10);
		} else {
			float maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);

			float maxHealth = this.maxHealth * 0.5F;
			int heartsPerLine = (int) (Math.min(maxHearts, Math.ceil(maxHealth)));
			int lineCount = (int) (Math.ceil(maxHealth / maxHearts));

			return new Vec2(9 * heartsPerLine, 10 * lineCount);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		float maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
		int maxHeartsForRender = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_MAX_FOR_RENDER);
		boolean showNumbers = maxHealth > maxHeartsForRender;

		int heartCount = showNumbers ? 1 : Mth.ceil(maxHealth * 0.5F);
		float health = showNumbers ? 1 : (this.health * 0.5F);
		int heartsPerLine = (int) (Math.min(maxHearts, Math.ceil(maxHealth)));

		IDisplayHelper helper = IDisplayHelper.get();
		int xOffset = 0;
		for (int i = 1; i <= heartCount; i++) {
			helper.blitSprite(guiGraphics, EMPTY_HEART, (int) (x + xOffset), (int) y, 9, 9);

			if (i <= Mth.floor(health)) {
				helper.blitSprite(guiGraphics, HEART, (int) (x + xOffset), (int) y, 9, 9);
			}

			if ((i > health) && (i < health + 1)) {
				helper.blitSprite(guiGraphics, HALF_HEART, (int) (x + xOffset), (int) y, 9, 9);
			}

			xOffset += 9;
			if (!showNumbers && i % heartsPerLine == 0) {
				y += 10;
				xOffset = 0;
			}
		}

		if (showNumbers) {
			helper.drawText(guiGraphics, text, x + 8, y, IThemeHelper.get().getNormalColor());
		}
	}

	@Override
	public @Nullable String getMessage() {
		return I18n.get("narration.jade.health", DisplayHelper.dfCommas.format(health));
	}
}
