package snownee.jade.gui.config;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class NotUglyEditBox extends EditBox {
	public Integer fixedTextX, fixedTextY, fixedInnerWidth;
	public WidgetSprites background = EditBox.SPRITES;
	public BackgroundMode backgroundMode = BackgroundMode.VISIBLE;
	public boolean alwaysRenderCross;
	private boolean isMouseOverCross;

	public NotUglyEditBox(Font font, int i, int j, Component component) {
		super(font, i, j, component);
		setBordered(false);
	}

	public NotUglyEditBox(Font font, int i, int j, int k, int l, Component component) {
		super(font, i, j, k, l, component);
		setBordered(false);
	}

	public NotUglyEditBox(Font font, int i, int j, int k, int l, @Nullable EditBox editBox, Component component) {
		super(font, i, j, k, l, editBox, component);
		setBordered(false);
	}

	@Override
	public void updateTextPosition() {
		super.updateTextPosition();
		if (fixedTextX != null) {
			textX = getX() + fixedTextX;
		}
		if (fixedTextY != null) {
			textY = getY() + fixedTextY;
		}
	}

	@Override
	public int getInnerWidth() {
		return fixedInnerWidth != null ? fixedInnerWidth : super.getInnerWidth();
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		if (isVisible()) {
			float bgAlpha;
			if (this.backgroundMode == BackgroundMode.HOVERING) {
				if (isFocused()) {
					bgAlpha = 1F;
				} else if (isActive() && isHovered()) {
					bgAlpha = 0.25F;
				} else {
					bgAlpha = 0F;
				}
			} else {
				bgAlpha = backgroundMode == BackgroundMode.VISIBLE ? 1.0F : 0.0F;
			}
			if (bgAlpha > 0F) {
				Identifier Identifier = background.get(this.isActive(), this.isFocused());
				guiGraphics.blitSprite(
						RenderPipelines.GUI_TEXTURED,
						Identifier,
						this.getX(),
						this.getY(),
						this.getWidth(),
						this.getHeight(),
						ARGB.white(bgAlpha));

				if (isEditable() && !getValue().isEmpty()) {
					if (alwaysRenderCross || isHovered) {
						isMouseOverCross = isHovered && i > getRight() - 12;
						int c = isMouseOverCross ? textColor : textColorUneditable;
						guiGraphics.drawString(font, "×", getX() + width - 10, textY + 1, c);
					}
				}
			}
		}
		super.renderWidget(guiGraphics, i, j, f);
		if (isMouseOverCross) {
			guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
		}
	}

	@Override
	public void onClick(MouseButtonEvent mouseButtonEvent, boolean bl) {
		if (isMouseOverCross) {
			setValue("");
			return;
		}
		super.onClick(mouseButtonEvent, bl);
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
		if (isMouseOverCross) {
			narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.jade.clear_content.usage"));
		}
	}

	public enum BackgroundMode {
		VISIBLE, INVISIBLE, HOVERING
	}
}
