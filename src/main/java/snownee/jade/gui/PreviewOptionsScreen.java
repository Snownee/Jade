package snownee.jade.gui;

import java.util.Objects;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.overlay.OverlayRenderer;

public abstract class PreviewOptionsScreen extends BaseOptionsScreen {

	private boolean adjustingPosition;
	private boolean adjustDragging;
	private double dragOffsetX;
	private double dragOffsetY;

	public PreviewOptionsScreen(Screen parent, Component title) {
		super(parent, title);
	}

	public static boolean isAdjustingPosition() {
		return Minecraft.getInstance().screen instanceof PreviewOptionsScreen screen && screen.adjustingPosition;
	}

	private static float calculateAnchor(float center, float size, int rectSize) {
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
		if (!Screen.hasControlDown() && value > 0.475f && value < 0.525f) {
			return 0.5f;
		}
		return value;
	}

	@Override
	protected void init() {
		Objects.requireNonNull(minecraft);
		super.init();
		if (minecraft.level != null) {
			CycleButton<Boolean> previewButton = CycleButton.booleanBuilder(OptionsList.OPTION_ON, OptionsList.OPTION_OFF).create(
					10,
					saveButton.getY(),
					85,
					20,
					Component.translatable("gui.jade.preview"),
					(button, value) -> {
						Jade.CONFIG.get().getGeneral().previewOverlay = value;
						saver.run();
					});
			previewButton.setValue(Jade.CONFIG.get().getGeneral().previewOverlay);
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
	public boolean mouseClicked(double mouseX, double mouseY, int p_94697_) {
		if (adjustingPosition) {
			Objects.requireNonNull(minecraft);
			Rect2i rect = OverlayRenderer.rect.expectedRect;
			if (rect.contains((int) mouseX, (int) mouseY)) {
				setDragging(true);
				adjustDragging = true;
				float centerX = rect.getX() + rect.getWidth() / 2F;
				float centerY = rect.getY() + rect.getHeight() / 2F;
				dragOffsetX = mouseX - centerX;
				dragOffsetY = mouseY - centerY;
			} else {
				adjustingPosition = false;
				adjustDragging = false;
				minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			}
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, p_94697_);
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		if (adjustingPosition) {
			setDragging(false);
			adjustDragging = false;
			return true;
		}
		return super.mouseReleased(d, e, i);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
		if (adjustingPosition) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (adjustingPosition) {
			return true;
		}
		return super.keyPressed(i, j, k);
	}

	@Override
	public boolean keyReleased(int i, int j, int k) {
		Objects.requireNonNull(minecraft);
		if (adjustingPosition) {
			if (i == InputConstants.KEY_ESCAPE) {
				adjustingPosition = false;
				adjustDragging = false;
				minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			}
			return true;
		}
		return super.keyReleased(i, j, k);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		if (adjustingPosition && adjustDragging) {
			float centerX = (float) d - (float) dragOffsetX;
			float centerY = (float) e - (float) dragOffsetY;
			Rect2i rect = OverlayRenderer.rect.expectedRect;
			int rectWidth = rect.getWidth();
			int rectHeight = rect.getHeight();
			float anchorX = calculateAnchor(centerX, width, rectWidth);
			float anchorY = calculateAnchor(centerY, height, rectHeight);
			float posX = (centerX + rectWidth * (anchorX - 0.5F)) / width;
			float posY = 1 - (centerY + rectHeight * (anchorY - 0.5F)) / height;
			IWailaConfig.IConfigOverlay config = IWailaConfig.get().getOverlay();
			config.setOverlayPosX(config.tryFlip(maybeSnap(posX)));
			config.setOverlayPosY(maybeSnap(posY));
			config.setAnchorX(config.tryFlip(anchorX));
			config.setAnchorY(anchorY);
			return true;
		}
		return super.mouseDragged(d, e, i, f, g);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (adjustingPosition) {
			super.render(guiGraphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
			guiGraphics.fill(0, 0, width, height, 50, 0x80AAAAAA);
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 50);
			guiGraphics.drawCenteredString(
					font,
					Component.translatable("config.jade.overlay_pos.exit"),
					width / 2,
					height / 2 - 7,
					0xFFFFFF);
			guiGraphics.pose().popPose();
			IWailaConfig.IConfigOverlay config = IWailaConfig.get().getOverlay();
			Rect2i rect = OverlayRenderer.rect.expectedRect;
			if (IWailaConfig.get().getGeneral().isDebug()) {
				int anchorX = (int) (rect.getX() + rect.getWidth() * config.getAnchorX());
				int anchorY = (int) (rect.getY() + rect.getHeight() * config.getAnchorY());
				guiGraphics.fill(anchorX - 2, anchorY - 2, anchorX + 1, anchorY + 1, 1000, 0xFFFF0000);
			}
			if (config.getOverlayPosX() == 0.5f) {
				guiGraphics.fill(width / 2, rect.getY() - 5, width / 2 + 1, rect.getY() + rect.getHeight() + 4, 1000, 0xFF0000FF);
			}
			if (config.getOverlayPosY() == 0.5f) {
				guiGraphics.fill(rect.getX() - 5, height / 2, rect.getX() + rect.getWidth() + 4, height / 2 + 1, 1000, 0xFF0000FF);
			}
			deferredTooltipRendering = null;
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
