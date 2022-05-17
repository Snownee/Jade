package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.WailaClient;
import snownee.jade.api.config.IWailaConfig.IConfigGeneral;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.IconUI;
import snownee.jade.overlay.OverlayRenderer;

public class ArmorElement extends Element {

	private final float armor;

	public ArmorElement(float armor) {
		this.armor = armor;
	}

	@Override
	public Vec2 getSize() {
		IConfigGeneral config = WailaClient.CONFIG.get().getGeneral();
		if (armor > config.getMaxHealthForRender()) {
			String text = "  " + DisplayHelper.dfCommas.format(armor);
			Font font = Minecraft.getInstance().font;
			return new Vec2(8 + font.width(text), 10);
		} else {
			int maxHearts = config.getMaxHeartsPerLine();
			int lineCount = (int) (Math.ceil(armor / maxHearts * 0.5F));
			return new Vec2(8 * maxHearts, 10 * lineCount);
		}
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		IConfigGeneral config = WailaClient.CONFIG.get().getGeneral();
		if (armor > config.getMaxHealthForRender()) {
			DisplayHelper.renderIcon(matrixStack, x, y, 8, 8, IconUI.ARMOR);
			String text = "  " + DisplayHelper.dfCommas.format(armor);
			DisplayHelper.INSTANCE.drawText(matrixStack, text, x + 8, y, OverlayRenderer.normalTextColorRaw);
		} else {
			float armor = this.armor * 0.5F;
			int maxHearts = config.getMaxHeartsPerLine();
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
		return new TranslatableComponent("narration.waila.armor", DisplayHelper.dfCommas.format(armor));
	}
}
