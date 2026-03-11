package snownee.jade.impl.ui;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import snownee.jade.JadeClient;
import snownee.jade.api.JadeIds;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.NarratableComponent;
import snownee.jade.api.ui.Orientation;
import snownee.jade.api.ui.ResizeableElement;
import snownee.jade.api.ui.ScreenDirection;
import snownee.jade.api.view.ProgressView;
import snownee.jade.gui.ResizeableLayout;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.track.ProgressTrackInfo;

public class ProgressElement extends ResizeableElement implements StyledElement {
	private static final SpriteElement DEFAULT_OVERLAY = new SpriteElement(JadeIds.JADE("progressbar"), 16, 16);
	private final ProgressView view;
	private @Nullable ProgressTrackInfo track;

	public ProgressElement(ProgressView view) {
		this.view = view;
		width = 100;
		height = 8;
		if (view.text != null) {
			width = Math.max(width, DisplayHelper.font().width(view.text) + 10);
			height = 14;
		}
		if (view.style.direction().isHorizontal()) {
			if (view.style.fitContentX()) {
				flexGrow(1);
			}
			if (view.style.fitContentY()) {
				alignSelfStretch();
			}
		} else {
			if (view.style.fitContentY()) {
				flexGrow(1);
			}
			if (view.style.fitContentX()) {
				alignSelfStretch();
			}
		}
	}

	public ProgressElement(ProgressView view, int width, int height) {
		this.view = view;
		this.width = width;
		this.height = height;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		view.boxStyle.render(graphics, this, getX(), getY(), width, height, IDisplayHelper.get().opacity());

		if (track != null) {
			track.setProgress(view.parts);
			track.update(Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
		}

		int borderWidth = view.boxStyle.borderWidth();
		int freeX = getX() + borderWidth;
		int freeY = getY() + borderWidth;
		int freeWidth = width - borderWidth * 2;
		int freeHeight = height - borderWidth * 2;
		float progress = 0;
		float start = 0;
		for (int i = 0; i < view.parts.size(); i++) {
			ProgressView.Part part = view.parts.get(i);
			float partProgress = part.progress();
			if (partProgress <= 0F) {
				continue;
			}
			if (track != null) {
				partProgress = track.getSmoothProgress(part);
			}
			progress = Math.min(progress + partProgress, 1F);
			start = renderPart(
					graphics,
					partialTicks,
					part,
					partProgress,
					start,
					freeX,
					freeY,
					freeWidth,
					freeHeight,
					i == view.parts.size() - 1);
			if (progress == 1F) {
				break;
			}
		}
		if (progress > 0 && view.style.foreground() != null) {
			DisplayHelper.INSTANCE.blitSprite(
					graphics,
					RenderPipelines.GUI_TEXTURED,
					Objects.requireNonNull(view.style.foreground()),
					freeX,
					freeY,
					(int) (freeWidth - start),
					freeHeight);
		}

		if (view.text != null) {
			IDisplayHelper.get().drawText(graphics, view.text, getX() + 4, getY() + 3, IThemeHelper.get().getNormalColor());
		}
	}

	private float renderPart(
			GuiGraphicsExtractor graphics,
			float partialTicks,
			ProgressView.Part part,
			float partProgress,
			float start,
			int x,
			int y,
			int width,
			int height,
			boolean isLast) {
		float partWidth = Math.min(partProgress * width, width - start);
		int roundedPartWidth = Mth.ceil(partWidth);
		Element overlay = part.overlay();
		if (overlay == null) {
			DEFAULT_OVERLAY.setColor(part.themeColor());
			DEFAULT_OVERLAY.tiledOrientation =
					view.style.direction() == ScreenDirection.RIGHT ? Orientation.HORIZONTAL : Orientation.VERTICAL;
			overlay = DEFAULT_OVERLAY;
		}
//		graphics.enableScissor(x + (int) start, y, x + (int) start + roundedPartWidth, y + height);
		// we can only draw a sprite from its top-left corner, so only makes the last part more detailed
		if (isLast && view.style.foreground() == null && overlay instanceof ProgressOverlayElement element &&
				element.canUseFloatingRect(graphics)) {
			element.setFloatingRect(x + start, y, partWidth, height);
			element.extractRenderState(graphics, -1, -1, partialTicks);
			element.setFloatingRect(null);
//			graphics.disableScissor();
			return start + partWidth;
		} else {
			resizeElement(overlay, x + (int) start, y, roundedPartWidth, height);
			overlay.extractRenderState(graphics, -1, -1, partialTicks);
//			graphics.disableScissor();
			return start + roundedPartWidth;
		}
	}

	private void resizeElement(Element element, int x, int y, int width, int height) {
		element.setX(x);
		element.setY(y);
		if (element instanceof ResizeableLayout resizeableLayout) {
			resizeableLayout.setFreeSpace(width, height);
		}
	}

	@Override
	public @Nullable Component getNarration() {
		return view.text == null ? null : NarratableComponent.getNarration(view.text);
	}

	@Override
	public @Nullable Element getIcon() {
		return null;
	}

	@Override
	public BoxStyle getStyle() {
		return view.boxStyle;
	}

	@Override
	public void setFreeSpace(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void updateSize() {
		if (getTag() != null) {
			track = JadeClient.tickHandler().progressTracker.getOrCreate(
					getTag(),
					ProgressTrackInfo.class,
					() -> new ProgressTrackInfo(view.parts, view.style.canDecrease(), width));
			track.setExpectedWidth(width);
			width = track.getWidth();
		} else {
			track = null;
		}
	}
}
