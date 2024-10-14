package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.WailaTickHandler;
import snownee.jade.track.HealthTrackInfo;

public class HealthElement extends Element {

	public static final ResourceLocation HEART = ResourceLocation.withDefaultNamespace("hud/heart/full");
	public static final ResourceLocation HEART_BLINKING = ResourceLocation.withDefaultNamespace("hud/heart/full_blinking");
	public static final ResourceLocation HALF_HEART = ResourceLocation.withDefaultNamespace("hud/heart/half");
	public static final ResourceLocation HALF_HEART_BLINKING = ResourceLocation.withDefaultNamespace("hud/heart/half_blinking");
	public static final ResourceLocation EMPTY_HEART = ResourceLocation.withDefaultNamespace("hud/heart/container");
	public static final ResourceLocation EMPTY_HEART_BLINKING = ResourceLocation.withDefaultNamespace("hud/heart/container_blinking");

	private final float health;
	private String text;
	private int iconsPerLine = 1;
	private int lineCount = 1;
	private int iconCount = 1;
	private HealthTrackInfo track;

	public HealthElement(float maxHealth, float health) {
		this.health = health;
		IPluginConfig config = IWailaConfig.get().plugin();
		if (maxHealth > config.getInt(JadeIds.MC_ENTITY_HEALTH_MAX_FOR_RENDER)) {
			if (!config.get(JadeIds.MC_ENTITY_HEALTH_SHOW_FRACTIONS)) {
				maxHealth = Mth.ceil(maxHealth);
				health = Mth.ceil(health);
			}
			text = String.format("%s/%s", DisplayHelper.dfCommas.format(health), DisplayHelper.dfCommas.format(maxHealth));
		} else {
			maxHealth *= 0.5f;
			int maxHeartsPerLine = config.getInt(JadeIds.MC_ENTITY_HEALTH_ICONS_PER_LINE);
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
		float health = this.health * 0.5F;
		float lastHealth = health;
		boolean blink = false;
		if (track == null && getTag() != null) {
			track = WailaTickHandler.instance().progressTracker.getOrCreate(getTag(), HealthTrackInfo.class, () -> {
				return new HealthTrackInfo(this.health);
			});
		}
		if (track != null) {
			track.setHealth(this.health);
			track.update(Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
			lastHealth = track.getLastHealth() * 0.5F;
			blink = track.isBlinking();
		}

		IDisplayHelper helper = IDisplayHelper.get();
		int xOffset = (iconCount - 1) % iconsPerLine * 8;
		int yOffset = lineCount * 4 - 4;
		for (int i = iconCount; i > 0; --i) {
			int xPos = (int) (x + xOffset);
			int yPos = (int) (y + yOffset);
			helper.blitSprite(guiGraphics, RenderType::guiTextured, blink ? EMPTY_HEART_BLINKING : EMPTY_HEART, xPos, yPos, 9, 9);

			if (i <= Mth.floor(health)) {
				helper.blitSprite(guiGraphics, RenderType::guiTextured, HEART, xPos, yPos, 9, 9);
			}

			if (i > health) {
				if (i <= Mth.floor(lastHealth)) {
					helper.blitSprite(guiGraphics, RenderType::guiTextured, HEART_BLINKING, xPos, yPos, 9, 9);
				} else if ((i > lastHealth) && (i < lastHealth + 1)) {
					helper.blitSprite(guiGraphics, RenderType::guiTextured, HALF_HEART_BLINKING, xPos, yPos, 9, 9);
				}
				if (i < health + 1) {
					helper.blitSprite(guiGraphics, RenderType::guiTextured, HALF_HEART, xPos, yPos, 9, 9);
				}
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
