package snownee.jade.impl.ui;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import snownee.jade.JadeClient;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.track.HealthTrackInfo;

public class HealthElement extends Element {

	private Gui.HeartType heartType;
	private final float maxHealth;
	private final float health;
	private final float absorption;
	private @Nullable String text;
	private int iconsPerLine = 1;
	private int lineCount = 1;
	private int iconCount = 1;
	private @Nullable HealthTrackInfo track;

	public HealthElement(Gui.HeartType heartType, float maxHealth, float health, float absorption) {
		this.heartType = heartType;
		this.maxHealth = maxHealth;
		this.health = health;
		this.absorption = absorption;
		IPluginConfig config = IWailaConfig.get().plugin();
		int iconCount = Mth.ceil(maxHealth) + Mth.ceil(absorption);
		if (iconCount > config.getInt(JadeIds.MC_ENTITY_HEALTH_MAX_FOR_RENDER)) {
			health += absorption;
			if (!config.get(JadeIds.MC_ENTITY_HEALTH_SHOW_FRACTIONS)) {
				maxHealth = Mth.ceil(maxHealth);
				health = Mth.ceil(health);
			}
			if (absorption > 0) {
				this.heartType = Gui.HeartType.ABSORBING;
			}
			text = String.format("%s/%s", DisplayHelper.dfCommas.format(health), DisplayHelper.dfCommas.format(maxHealth));
		} else {
			int maxHeartsPerLine = config.getInt(JadeIds.MC_ENTITY_HEALTH_ICONS_PER_LINE);
			iconCount = Mth.ceil(iconCount * 0.5F);
			this.iconCount = iconCount;
			iconsPerLine = Math.min(maxHeartsPerLine, iconCount);
			lineCount = Mth.ceil((float) iconCount / maxHeartsPerLine);
		}
		if (showText()) {
			width = DisplayHelper.font().width(text) + 10;
			height = 9;
		} else {
			width = 8 * iconsPerLine + 1;
			height = 5 + 4 * lineCount;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		float health = Math.round(this.health);
		float hearts = health * 0.5F;
		float lastHearts = hearts;
		float lastAbsorption = absorption;
		boolean blink = false;
		if (track == null && getTag() != null) {
			track = JadeClient.tickHandler().progressTracker.getOrCreate(
					getTag(),
					HealthTrackInfo.class,
					() -> new HealthTrackInfo(health, absorption));
		}
		if (track != null) {
			track.setHealth(health, absorption);
			track.update(Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
			lastHearts = track.getLastHealth() * 0.5F;
			lastAbsorption = track.getLastAbsorption();
			blink = track.isBlinking();
		}

		IDisplayHelper helper = IDisplayHelper.get();
		int xOffset = (iconCount - 1) % iconsPerLine * 8;
		int yOffset = lineCount * 4 - 4;
		Identifier containerSprite = Gui.HeartType.CONTAINER.getSprite(false, false, blink);
		//MC-265342 - Blinking absorption heart textures do not appear to actually be used in-game
		for (int i = iconCount; i > 0; --i) {
			int xPos = getX() + xOffset;
			int yPos = getY() + yOffset;
			helper.blitSprite(graphics, RenderPipelines.GUI_TEXTURED, containerSprite, xPos, yPos, 9, 9);

			boolean renderAbsorb = i > Mth.ceil(maxHealth * 0.5F);
			Gui.HeartType curHeartType = heartType;
			float curHearts = hearts;
			float curLastHearts = lastHearts;
			if (renderAbsorb) {
				curHeartType = Gui.HeartType.ABSORBING;
				curHearts = (Mth.ceil(maxHealth) + absorption) * 0.5F;
				curLastHearts = (Mth.ceil(maxHealth) + lastAbsorption) * 0.5F;
			}
			if (i <= Mth.floor(curHearts)) { // Full heart
				helper.blitSprite(graphics, RenderPipelines.GUI_TEXTURED, curHeartType.getSprite(false, false, false), xPos, yPos, 9, 9);
			}

			if (i > curHearts) {
				if (i <= Mth.floor(curLastHearts)) { // Full heart (last + blink)
					helper.blitSprite(graphics, RenderPipelines.GUI_TEXTURED, curHeartType.getSprite(false, false, true), xPos, yPos, 9, 9);
				} else if ((i > curLastHearts) && (i < curLastHearts + 1)) { // Half heart (blink)
					helper.blitSprite(graphics, RenderPipelines.GUI_TEXTURED, curHeartType.getSprite(false, true, true), xPos, yPos, 9, 9);
				}
				if (i < curHearts + 1) { // Half heart
					helper.blitSprite(graphics, RenderPipelines.GUI_TEXTURED, curHeartType.getSprite(false, true, false), xPos, yPos, 9, 9);
				}
			}

			xOffset -= 8;
			if (xOffset < 0) {
				xOffset = iconsPerLine * 8 - 8;
				yOffset -= 4;
			}
		}

		if (showText()) {
			helper.drawText(graphics, Objects.requireNonNull(text), getX() + 10, getY() + 1, IThemeHelper.get().getNormalColor());
		}
	}

	@Override
	public Component getNarration() {
		return Component.translatable("narration.jade.health", Mth.ceil(health));
	}

	public boolean showText() {
		return text != null;
	}
}
