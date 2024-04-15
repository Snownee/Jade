package snownee.jade.gui;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import snownee.jade.util.SmoothChasingValue;

public class CreditButton extends Button {
	private final Component hoveredTitle;
	private final OnPress onHover;
	private final SmoothChasingValue progress = new SmoothChasingValue();
	private boolean oldHovered;
	private boolean showTranslators;
	private List<String> translators = List.of();
	private int translatorIndex;
	private float translatorTime;

	protected CreditButton(
			int x,
			int y,
			int width,
			int height,
			Component title,
			Component hoveredTitle,
			OnPress onPress,
			OnPress onHover,
			CreateNarration createNarration) {
		super(x, y, width, height, title, onPress, createNarration);
		this.hoveredTitle = hoveredTitle;
		this.onHover = onHover;
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float partialTicks) {
		boolean hovered = isHoveredOrFocused();
		if (!oldHovered && hovered) {
			progress.target(1);
		} else if (!hovered) {
			progress.target(0);
		} else if (progress.value > 0.5F) {
			progress.target(0);
			onHover.onPress(this);
		}
		progress.tick(partialTicks);
		progress.value = Math.min(0.6F, progress.value);
		float alpha = hovered ? 170 : 85;
		if (showTranslators && !translators.isEmpty()) {
			int cycleTime = 60;
			translatorTime += partialTicks;
			if (translatorTime > cycleTime) {
				nextTranslator();
			}
			if (!hovered && translators.size() > 1) {
				if (translatorTime < 5) {
					alpha *= translatorTime / 5;
				} else if (cycleTime - translatorTime < 5) {
					alpha *= (cycleTime - translatorTime) / 5;
				}
				alpha = Math.max(alpha, 17);
			}
		}
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(getX() + width * 0.5F, getY(), 0);
		float scale = 1 + progress.value * 0.2F;
		guiGraphics.pose().scale(scale, scale, scale);
		Component credit = hovered ? hoveredTitle : getMessage();
		Font font = Minecraft.getInstance().font;
		guiGraphics.pose().translate(font.width(credit) * -0.5F, 0, 0);
		guiGraphics.drawString(font, credit, 0, 0, 0xFFFFFF | (int) alpha << 24);
		guiGraphics.pose().popPose();
		oldHovered = hovered;
	}

	public void showTranslators() {
		if (showTranslators) {
			return;
		}
		showTranslators = true;
		if (!I18n.exists("gui.jade.translators") || "placeholder ".equals(I18n.get("gui.jade.translated_by", ""))) {
			return;
		}
		String s = I18n.get("gui.jade.translators");
		if ("Bob, Alice, Charlie".equals(s)) {
			return;
		}
		translators = Stream.of(StringUtils.split(s, ',')).map(String::trim).filter(StringUtils::isNotEmpty).toList();
		if (translators.size() > 1) {
			translatorIndex = RandomSource.create().nextInt(translators.size());
		}
		nextTranslator();
	}

	private void nextTranslator() {
		setMessage(Component.translatable("gui.jade.translated_by", translators.get(translatorIndex)));
		if (translators.size() <= 1) {
			return;
		}
		translatorIndex++;
		if (translatorIndex >= translators.size()) {
			translatorIndex = 0;
		}
		translatorTime = 0;
	}
}
