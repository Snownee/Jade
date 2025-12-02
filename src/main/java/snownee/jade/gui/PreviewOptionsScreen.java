package snownee.jade.gui;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.Rect2f;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;

public abstract class PreviewOptionsScreen extends BaseOptionsScreen {

	private boolean adjustingPosition;
	private boolean adjustDragging;
	private double dragOffsetX;
	private double dragOffsetY;

	public PreviewOptionsScreen(@Nullable Screen parent, Component title) {
		super(parent, title);
	}

	public static boolean isAdjustingPosition() {
		return Minecraft.getInstance().screen instanceof PreviewOptionsScreen screen && screen.adjustingPosition;
	}

	private static float calculateAnchor(float center, float size, float rectSize) {
		float anchor = center / size;
		if (anchor < 0.25F) {
			return 0;
		}
		if (anchor > 0.75F) {
			return 1;
		}
		float halfRectSize = rectSize / 2F;
		float tolerance = Math.min(15, halfRectSize / 2F - 3);
		if (Math.abs(center + halfRectSize - size / 2F) < tolerance) {
			return 1;
		}
		if (Math.abs(center - halfRectSize - size / 2F) < tolerance) {
			return 0;
		}
		return 0.5F;
	}

	private static float maybeSnap(float value) {
		if (!JadeUI.hasControlDown() && value > 0.475f && value < 0.525f) {
			return 0.5f;
		}
		return value;
	}

	@Override
	protected void init() {
		Objects.requireNonNull(minecraft);
		super.init();
		if (minecraft.level != null) {
			CycleButton<Boolean> previewButton = CycleButton.booleanBuilder(
					OptionsList.OPTION_ON,
					OptionsList.OPTION_OFF,
					Jade.history().previewOverlay).create(
					10,
					Objects.requireNonNull(saveButton).getY(),
					85,
					20,
					Component.translatable("gui.jade.preview"),
					(button, value) -> {
						Jade.history().previewOverlay = value;
						Objects.requireNonNull(saver).run();
					});
			addRenderableWidget(previewButton);
		}
	}

	public boolean forcePreviewOverlay() {
		Objects.requireNonNull(minecraft);
		if (adjustingPosition) {
			return true;
		}
		if (!isDragging() || options == null) {
			return false;
		}
		OptionsList.Entry entry = options.getSelected();
		if (entry == null || entry.getFirstWidget() == null) {
			return false;
		}
		return options.forcePreview.contains(entry);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!adjustingPosition) {
			return super.mouseClicked(event, doubleClick);
		}

		Objects.requireNonNull(minecraft);
		Rect2f rect = OverlayRenderer.animation.expectedRect;
		if (rect.contains((int) event.x(), (int) event.y())) {
			setDragging(true);
			adjustDragging = true;
			float centerX = rect.getX() + rect.getWidth() / 2F;
			float centerY = rect.getY() + rect.getHeight() / 2F;
			dragOffsetX = event.x() - centerX;
			dragOffsetY = event.y() - centerY;
		} else {
			minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			int xIndex = Mth.clamp((int) (event.x() / (width / 3D)), 0, 2);
			int yIndex = Mth.clamp((int) (event.y() / (height / 3D)), 0, 2);
			if (xIndex == 1 && yIndex == 1) {
				adjustingPosition = false;
				adjustDragging = false;
			} else {
				IWailaConfig.Overlay overlay = IWailaConfig.get().overlay();
				overlay.setOverlayPosX(IWailaConfig.get().accessibility().tryFlip(xIndex / 2F));
				overlay.setOverlayPosY(1 - yIndex / 2F);
				overlay.setAnchorX(IWailaConfig.get().accessibility().tryFlip(xIndex / 2F));
				overlay.setAnchorY(yIndex / 2F);
			}
		}
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (adjustingPosition) {
			setDragging(false);
			adjustDragging = false;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
		if (adjustingPosition) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if (adjustingPosition) {
			return true;
		}
		return super.keyPressed(keyEvent);
	}

	@Override
	public boolean keyReleased(KeyEvent keyEvent) {
		Objects.requireNonNull(minecraft);
		if (adjustingPosition) {
			if (keyEvent.isEscape()) {
				adjustingPosition = false;
				adjustDragging = false;
				minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			}
			return true;
		}
		return super.keyReleased(keyEvent);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double d, double e) {
		if (adjustingPosition && adjustDragging) {
			float centerX = (float) event.x() - (float) dragOffsetX;
			float centerY = (float) event.y() - (float) dragOffsetY;
			Rect2f rect = OverlayRenderer.animation.expectedRect;
			float rectWidth = rect.getWidth();
			float rectHeight = rect.getHeight();
			float anchorX = calculateAnchor(centerX, width, rectWidth);
			float anchorY = calculateAnchor(centerY, height, rectHeight);
			float posX = (centerX + rectWidth * (anchorX - 0.5F)) / width;
			float posY = 1 - (centerY + rectHeight * (anchorY - 0.5F)) / height;
			IWailaConfig.Overlay overlay = IWailaConfig.get().overlay();
			IWailaConfig.Accessibility accessibility = IWailaConfig.get().accessibility();
			overlay.setOverlayPosX(accessibility.tryFlip(maybeSnap(posX)));
			overlay.setOverlayPosY(maybeSnap(posY));
			overlay.setAnchorX(accessibility.tryFlip(anchorX));
			overlay.setAnchorY(anchorY);
			return true;
		}
		return super.mouseDragged(event, d, e);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (adjustingPosition) {
			super.render(guiGraphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
			guiGraphics.fill(0, 0, width, height, 0x80808080);

			MutableComponent text = Component.translatable("config.jade.overlay_pos.exit");
			Font font = DisplayHelper.font();
			int textWidth = font.width(text);
			int x = (width - textWidth) / 2;
			int y = height / 2 - 7;
			guiGraphics.fill(x - 4, y - 4, x + textWidth + 4, y + font.lineHeight + 4, 0x88000000);
			guiGraphics.drawString(font, text, x, y, 0xFFFFFFFF);

			IWailaConfig.Overlay config = IWailaConfig.get().overlay();
			Rect2f rect = OverlayRenderer.animation.expectedRect;
			if (rect.contains(mouseX, mouseY)) {
				guiGraphics.requestCursor(CursorTypes.RESIZE_ALL);
			}
			if (IWailaConfig.get().general().isDebug()) {
				int anchorX = (int) (rect.getX() + rect.getWidth() * config.getAnchorX());
				int anchorY = (int) (rect.getY() + rect.getHeight() * config.getAnchorY());
				guiGraphics.fill(anchorX - 2, anchorY - 2, anchorX + 1, anchorY + 1, 0xFFFF0000);
			}
			if (config.getOverlayPosX() == 0.5f) {
				guiGraphics.fill(width / 2, (int) (rect.getY() - 5), width / 2 + 1, (int) (rect.getY() + rect.getHeight() + 4), 0xFF0000FF);
			}
			if (config.getOverlayPosY() == 0.5f) {
				guiGraphics.fill(
						(int) (rect.getX() - 5),
						height / 2,
						(int) (rect.getX() + rect.getWidth() + 4),
						height / 2 + 1,
						0xFF0000FF);
			}
		} else {
			super.render(guiGraphics, mouseX, mouseY, partialTicks);
		}
	}

	public void startAdjustingPosition() {
		adjustingPosition = true;
	}

	@Override
	protected void updateNarratedWidget(NarrationElementOutput narrationElementOutput) {
		if (adjustingPosition) {
			narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.jade.adjusting_position"));
			return;
		}
		super.updateNarratedWidget(narrationElementOutput);
	}

	@Override
	protected boolean shouldNarrateNavigation() {
		return !adjustingPosition;
	}
}
