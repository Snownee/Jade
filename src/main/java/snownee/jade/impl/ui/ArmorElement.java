package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.Identifiers;
import snownee.jade.api.ui.Element;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.IconUI;
import snownee.jade.overlay.OverlayRenderer;

public class ArmorElement extends Element {

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
			return new Vec2(8 + font.width(text), 10);
		} else {
			int maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
			int lineCount = (int) (Math.ceil(armor / maxHearts * 0.5F));
			return new Vec2(8 * maxHearts, 10 * lineCount);
		}
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		if (armor > PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_ARMOR_MAX_FOR_RENDER)) {
			DisplayHelper.renderIcon(matrixStack, x, y, 8, 8, IconUI.ARMOR);
			String text = "  " + DisplayHelper.dfCommas.format(armor);
			DisplayHelper.INSTANCE.drawText(matrixStack, text, x + 8, y, OverlayRenderer.normalTextColorRaw);
		} else {
			float armor = this.armor * 0.5F;
			int maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
			int lineCount = (int) (Math.ceil(armor / maxHearts));
			int armorCount = lineCount * maxHearts;
			int xOffset = 0;
			for (int i = 1; i <= armorCount; i++) {
				if (i <= Mth.floor(armor)) {
					DisplayHelper.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.ARMOR);
					xOffset += 8;
				}

				if ((i > armor) && (i < armor + 1)) {
					DisplayHelper.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.HALF_ARMOR);
					xOffset += 8;
				}

				if (i >= armor + 1) {
					DisplayHelper.renderIcon(matrixStack, x + xOffset, y, 8, 8, IconUI.EMPTY_ARMOR);
					xOffset += 8;
				}

				if (i % maxHearts == 0) {
					y += 10;
					xOffset = 0;
				}
			}
		}
	}

	@Override
	public @Nullable Component getMessage() {
		return Component.translatable("narration.jade.armor", DisplayHelper.dfCommas.format(armor));
	}
}
