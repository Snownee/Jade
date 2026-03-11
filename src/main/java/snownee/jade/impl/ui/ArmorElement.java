package snownee.jade.impl.ui;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.overlay.DisplayHelper;

public class ArmorElement extends Element {

	public static final Identifier ARMOR = Identifier.withDefaultNamespace("hud/armor_full");
	public static final Identifier HALF_ARMOR = Identifier.withDefaultNamespace("hud/armor_half");
	public static final Identifier EMPTY_ARMOR = Identifier.withDefaultNamespace("hud/armor_empty");

	private final float armor;
	private @Nullable String text;
	private int iconsPerLine = 1;
	private int lineCount = 1;
	private int iconCount = 1;

	public ArmorElement(float armor) {
		this.armor = armor;
		IPluginConfig config = IWailaConfig.get().plugin();
		if (armor > config.getInt(JadeIds.MC_ENTITY_ARMOR_MAX_FOR_RENDER)) {
			if (!config.get(JadeIds.MC_ENTITY_HEALTH_SHOW_FRACTIONS)) {
				armor = Mth.ceil(armor);
			}
			text = DisplayHelper.dfCommas.format(armor);
		} else {
			armor *= 0.5F;
			int maxHeartsPerLine = config.getInt(JadeIds.MC_ENTITY_HEALTH_ICONS_PER_LINE);
			iconCount = Mth.ceil(armor);
			iconsPerLine = Math.min(maxHeartsPerLine, iconCount);
			lineCount = Mth.ceil(armor / maxHeartsPerLine);
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
		IDisplayHelper helper = IDisplayHelper.get();
		int x = getX();
		int y = getY();
		int xOffset = (iconCount - 1) % iconsPerLine * 8;
		int yOffset = lineCount * 4 - 4;
		for (int i = iconCount; i > 0; --i) {
			helper.blitSprite(graphics, RenderPipelines.GUI_TEXTURED, EMPTY_ARMOR, x + xOffset, y + yOffset, 9, 9);

			if (i <= Mth.floor(armor)) {
				helper.blitSprite(graphics, RenderPipelines.GUI_TEXTURED, ARMOR, x + xOffset, y + yOffset, 9, 9);
			}

			if ((i > armor) && (i < armor + 1)) {
				helper.blitSprite(graphics, RenderPipelines.GUI_TEXTURED, HALF_ARMOR, x + xOffset, y + yOffset, 9, 9);
			}

			xOffset -= 8;
			if (xOffset < 0) {
				xOffset = iconsPerLine * 8 - 8;
				yOffset -= 4;
			}
		}

		if (showText()) {
			helper.drawText(graphics, Objects.requireNonNull(text), x + 10, y + 1, IThemeHelper.get().getNormalColor());
		}
	}

	@Override
	public Component getNarration() {
		return Component.translatable("narration.jade.armor", Mth.ceil(armor));
	}

	public boolean showText() {
		return text != null;
	}
}
