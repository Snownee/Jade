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

public class ArmorElement extends Element {

	public static final ResourceLocation ARMOR = new ResourceLocation("hud/armor_full");
	public static final ResourceLocation HALF_ARMOR = new ResourceLocation("hud/armor_half");
	public static final ResourceLocation EMPTY_ARMOR = new ResourceLocation("hud/armor_empty");

	private final float armor;

	public ArmorElement(float armor) {
		if (!PluginConfig.INSTANCE.get(Identifiers.MC_ENTITY_HEALTH_SHOW_FRACTIONS)) {
			armor = Mth.ceil(armor);
		}
		this.armor = armor;
	}

	@Override
	public Vec2 getSize() {
		if (armor > PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_ARMOR_MAX_FOR_RENDER)) {
			String text = "  " + DisplayHelper.dfCommas.format(armor);
			Font font = Minecraft.getInstance().font;
			return new Vec2(9 + font.width(text), 10);
		} else {
			int maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
			int lineCount = (int) (Math.ceil(armor / maxHearts * 0.5F));
			return new Vec2(9 * maxHearts, 10 * lineCount);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
		IDisplayHelper helper = IDisplayHelper.get();
		if (armor > PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_ARMOR_MAX_FOR_RENDER)) {
			helper.blitSprite(guiGraphics, ARMOR, (int) x, (int) y, 9, 9);
			String text = "  " + DisplayHelper.dfCommas.format(armor);
			helper.drawText(guiGraphics, text, x + 9, y, IThemeHelper.get().getNormalColor());
		} else {
			float armor = this.armor * 0.5F;
			int maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
			int lineCount = (int) (Math.ceil(armor / maxHearts));
			int armorCount = lineCount * maxHearts;
			int xOffset = 0;
			for (int i = 1; i <= armorCount; i++) {
				if (i <= Mth.floor(armor)) {
					helper.blitSprite(guiGraphics, ARMOR, (int) x + xOffset, (int) y, 9, 9);
				}

				if ((i > armor) && (i < armor + 1)) {
					helper.blitSprite(guiGraphics, HALF_ARMOR, (int) x + xOffset, (int) y, 9, 9);
				}

				if (i >= armor + 1) {
					helper.blitSprite(guiGraphics, EMPTY_ARMOR, (int) x + xOffset, (int) y, 9, 9);
				}

				xOffset += 9;
				if (i % maxHearts == 0) {
					y += 10;
					xOffset = 0;
				}
			}
		}
	}

	@Override
	public @Nullable String getMessage() {
		return I18n.get("narration.jade.armor", DisplayHelper.dfCommas.format(armor));
	}
}
