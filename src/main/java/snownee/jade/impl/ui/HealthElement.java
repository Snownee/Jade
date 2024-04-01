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

	private final float health;
	private String text;
	private int iconsPerLine = 1;
	private int lineCount = 1;
	private int iconCount = 1;

	public HealthElement(float maxHealth, float health) {
		this.health = health;
		if (maxHealth > PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_MAX_FOR_RENDER)) {
			if (!PluginConfig.INSTANCE.get(Identifiers.MC_ENTITY_HEALTH_SHOW_FRACTIONS)) {
				maxHealth = Mth.ceil(maxHealth);
				health = Mth.ceil(health);
			}
			text = String.format("%s/%s", DisplayHelper.dfCommas.format(health), DisplayHelper.dfCommas.format(maxHealth));
		} else {
			maxHealth *= 0.5f;
			int maxHeartsPerLine = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
			iconCount = Mth.ceil(maxHealth);
			iconsPerLine = Math.min(maxHeartsPerLine, iconCount);
			lineCount = Mth.ceil(maxHealth / maxHeartsPerLine);
		}
	}

	@Override
	public Vec2 getSize() {
		if (showText()) {
			Font font = Minecraft.getInstance().font;
			return new Vec2(font.width(text) + 10, 9);
		} else {
			return new Vec2(8 * iconsPerLine + 1, 5 + 4 * lineCount);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		IDisplayHelper helper = IDisplayHelper.get();
		int xOffset = (iconCount - 1) % iconsPerLine * 8;
		int yOffset = lineCount * 4 - 4;
		for (int i = iconCount; i > 0; --i) {
			helper.blitSprite(guiGraphics, EMPTY_HEART, (int) (x + xOffset), (int) (y + yOffset), 9, 9);

			if (i <= Mth.floor(health)) {
				helper.blitSprite(guiGraphics, HEART, (int) (x + xOffset), (int) (y + yOffset), 9, 9);
			}

			if ((i > health) && (i < health + 1)) {
				helper.blitSprite(guiGraphics, HALF_HEART, (int) (x + xOffset), (int) (y + yOffset), 9, 9);
			}

			xOffset -= 8;
			if (xOffset < 0) {
				xOffset = iconsPerLine * 8 - 8;
				yOffset -= 4;
			}
		}

		if (showText()) {
			helper.drawText(guiGraphics, text, x + 10, y + 1, IThemeHelper.get().getNormalColor());
		}
	}

	@Override
	public @Nullable String getMessage() {
		return I18n.get("narration.jade.health", Mth.ceil(health));
	}

	public boolean showText() {
		return text != null;
	}
}
