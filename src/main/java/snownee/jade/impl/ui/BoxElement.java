package snownee.jade.impl.ui;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.Jade;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.MessageType;
import snownee.jade.api.ui.TooltipRect;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.impl.Tooltip;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.track.ProgressTrackInfo;
import snownee.jade.overlay.WailaTickHandler;
import snownee.jade.util.ClientProxy;

public class BoxElement extends Element implements IBoxElement {
	private final Tooltip tooltip;
	private final BoxStyle style;
	private int[] padding;
	private IElement icon;
	private float boxProgress;
	private MessageType boxProgressType;
	private ProgressTrackInfo track;
	private Vec2 contentSize = Vec2.ZERO;

	public BoxElement(Tooltip tooltip, BoxStyle style) {
		this.tooltip = Objects.requireNonNull(tooltip);
		this.style = Objects.requireNonNull(style);
	}

	private static void chase(TooltipRect rect, ToIntFunction<Rect2i> getter, IntConsumer setter) {
		if (Jade.CONFIG.get().getOverlay().getAnimation()) {
			int source = getter.applyAsInt(rect.rect);
			int target = getter.applyAsInt(rect.expectedRect);
			float diff = target - source;
			if (diff == 0) {
				return;
			}
			float delta = Minecraft.getInstance().getDeltaFrameTime() * 2;
			if (delta < 1) {
				diff *= delta;
			}
			if (Mth.abs(diff) < 1) {
				diff = diff > 0 ? 1 : -1;
			}
			setter.accept((int) (source + diff));
		} else {
			setter.accept(getter.applyAsInt(rect.expectedRect));
		}
	}

	private static int calculateMargin(int margin1, int margin2) {
		if (margin1 >= 0 && margin2 >= 0) {
			return Math.max(margin1, margin2);
		} else if (margin1 < 0 && margin2 < 0) {
			return Math.min(margin1, margin2);
		} else {
			return margin1 + margin2;
		}
	}

	@Override
	public Vec2 getSize() {
		if (tooltip.isEmpty()) {
			return Vec2.ZERO;
		}
		float width = 0, height = 0;
		int lineCount = tooltip.lines.size();
		Tooltip.Line line = tooltip.lines.get(0);
		for (int i = 0; i < lineCount; i++) {
			Vec2 size = line.size();
			width = Math.max(width, size.x);
			height += size.y;
			if (i < lineCount - 1) {
				int marginBottom = line.marginBottom;
				line = tooltip.lines.get(i + 1);
				height += calculateMargin(marginBottom, line.marginTop);
			}
		}
		contentSize = new Vec2(width, height);
		if (icon != null) {
			Vec2 size = icon.getCachedSize();
			width += size.x + 3;
			height = Math.max(height, size.y);
		}
		width += padding(Direction2D.LEFT) + padding(Direction2D.RIGHT);
		height += padding(Direction2D.UP) + padding(Direction2D.DOWN);
		// our limited negative-padding support:
		width = Math.max(width, 0);
		height = Math.max(height, 0);

		if (icon != null && icon.getCachedSize().y > contentSize.y) {
			setPadding(Direction2D.UP, padding(Direction2D.UP) + (int) (icon.getCachedSize().y - contentSize.y) / 2);
		}

		return new Vec2(width, height);
	}

	@Override
	public void render(GuiGraphics guiGraphics, final float x, final float y, final float maxX, final float maxY) {
		if (tooltip.isEmpty()) {
			return;
		}
		RenderSystem.enableBlend();
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(x, y, 0);

		// render background
		float alpha = IDisplayHelper.get().opacity();
		if (Identifiers.ROOT.equals(getTag())) {
			alpha *= IWailaConfig.get().getOverlay().getAlpha();
		}
		style.render(guiGraphics, this, 0, 0, maxX - x, maxY - y, alpha);

		// render box progress
		if (boxProgressType != null) {
			float left = style.boxProgressOffset(Direction2D.LEFT);
			float width = maxX - x - left;
			float top = maxY - y - 1 + style.boxProgressOffset(Direction2D.UP) + style.borderWidth();
			float height = 1 + style.boxProgressOffset(Direction2D.DOWN);
			float progress = boxProgress;
			if (track == null && tag != null) {
				track = WailaTickHandler.instance().progressTracker.getOrCreate(tag, ProgressTrackInfo.class, () -> {
					return new ProgressTrackInfo(false, boxProgress, 0);
				});
			}
			if (track != null) {
				track.setProgress(progress);
				track.update(Minecraft.getInstance().getDeltaFrameTime());
				progress = track.getSmoothProgress();
			}
			((DisplayHelper) IDisplayHelper.get()).drawGradientProgress(
					guiGraphics,
					left,
					top,
					width,
					height,
					progress,
					style.boxProgressColors.get(boxProgressType));
		}

		float contentLeft = padding(Direction2D.LEFT);
		float contentTop = padding(Direction2D.UP);

		// render icon
		if (icon != null) {
			Vec2 iconSize = icon.getCachedSize();
			Vec2 offset = icon.getTranslation();
			float offsetY = offset.y;
			float min = contentTop + padding(Direction2D.DOWN) + iconSize.y;
			if (IWailaConfig.get().getOverlay().getIconMode() == IWailaConfig.IconMode.TOP && min < getCachedSize().y) {
				offsetY += contentTop;
			} else {
				offsetY += (size.y - iconSize.y) / 2;
			}
			float offsetX = contentLeft + offset.x;
			Tooltip.drawDebugBorder(guiGraphics, offsetX, offsetY, icon);
			icon.render(guiGraphics, offsetX, offsetY, offsetX + iconSize.x, offsetY + iconSize.y);
			contentLeft += iconSize.x + 3;
		}

		// render elements
		{
			float lineTop = contentTop;
			int lineCount = tooltip.lines.size();
			Tooltip.Line line = tooltip.lines.get(0);
			for (int i = 0; i < lineCount; i++) {
				Vec2 lineSize = line.size();
				line.render(guiGraphics, contentLeft, lineTop, maxX - x - padding(Direction2D.RIGHT), lineTop + lineSize.y);
				if (i < lineCount - 1) {
					int marginBottom = line.marginBottom;
					line = tooltip.lines.get(i + 1);
					lineTop += lineSize.y + calculateMargin(marginBottom, line.marginTop);
				}
			}
		}

		// render down arrow
		if (tooltip.sneakyDetails) {
			float arrowTop = (OverlayRenderer.ticks / 5) % 8 - 2;
			if (arrowTop <= 4) {
				alpha = 1 - Math.abs(arrowTop) / 2;
				if (alpha > 0.016) {
					guiGraphics.pose().pushPose();
					arrowTop += size.y - 6;
					Minecraft mc = Minecraft.getInstance();
					float arrowLeft = contentLeft + (contentSize.x - mc.font.width("▾") + 1) / 2f;
					guiGraphics.pose().translate(arrowLeft, arrowTop, 0);
					int color = IConfigOverlay.applyAlpha(IThemeHelper.get().theme().text.colors().info(), alpha);
					DisplayHelper.INSTANCE.drawText(guiGraphics, "▾", 0, 0, color);
					guiGraphics.pose().popPose();
				}
			}
		}

		Tooltip.drawDebugBorder(guiGraphics, 0, 0, this);
		guiGraphics.pose().popPose();
	}

	@Override
	public @Nullable String getMessage() {
		return tooltip.isEmpty() ? null : tooltip.getMessage();
	}

	@Override
	public Tooltip getTooltip() {
		return tooltip;
	}

	@Override
	public void setBoxProgress(MessageType type, float progress) {
		boxProgress = progress;
		boxProgressType = type;
	}

	@Override
	public float getBoxProgress() {
		return boxProgressType == null ? Float.NaN : boxProgress;
	}

	@Override
	public void clearBoxProgress() {
		boxProgress = 0;
		boxProgressType = null;
	}

	@Override
	@Nullable
	public IElement getIcon() {
		return icon;
	}

	@Override
	public void setIcon(@Nullable IElement icon) {
		this.icon = icon;
	}

	public void setThemeIcon(@Nullable IElement icon, Theme theme) {
		if (!IWailaConfig.get().getOverlay().shouldShowIcon()) {
			return;
		}
		if (icon != null && theme.iconSlotSprite != null) {
			if (theme.iconSlotSpriteCache == null) {
				GuiSpriteManager guiSprites = Minecraft.getInstance().getGuiSprites();
				TextureAtlasSprite textureAtlasSprite = guiSprites.getSprite(theme.iconSlotSprite);
				GuiSpriteScaling scaling = guiSprites.getSpriteScaling(textureAtlasSprite);
				int[] padding = new int[4];
				Arrays.fill(padding, theme.iconSlotInflation);
				if (scaling instanceof GuiSpriteScaling.NineSlice nineSlice) {
					GuiSpriteScaling.NineSlice.Border border = nineSlice.border();
					padding[0] += border.top();
					padding[1] += border.right();
					padding[2] += border.bottom();
					padding[3] += border.left();
				}
				theme.iconSlotSpriteCache = new BoxElement(new Tooltip(), BoxStyle.getSprite(theme.iconSlotSprite, padding));
			}
			ITooltip tooltip1 = theme.iconSlotSpriteCache.getTooltip();
			tooltip1.clear();
			tooltip1.add(icon);
			icon = theme.iconSlotSpriteCache.size(null);
		}
		setIcon(icon);
	}

	public void updateExpectedRect(TooltipRect rect) {
		Window window = Minecraft.getInstance().getWindow();
		IWailaConfig.IConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		Vec2 size = getCachedSize();
		float x = window.getGuiScaledWidth() * overlay.tryFlip(overlay.getOverlayPosX());
		float y = window.getGuiScaledHeight() * (1.0F - overlay.getOverlayPosY());
		float width = size.x;
		float height = size.y;

		if (style.hasRoundCorner()) {
			x++;
			y++;
			width += 2;
			height += 2;
		}

		rect.scale = overlay.getOverlayScale();
		float thresholdHeight = window.getGuiScaledHeight() * overlay.getAutoScaleThreshold();
		if (size.y * rect.scale > thresholdHeight) {
			rect.scale = Math.max(rect.scale * 0.5f, thresholdHeight / size.y);
		}

		Rect2i expectedRect = rect.expectedRect;
		expectedRect.setWidth((int) (width * rect.scale));
		expectedRect.setHeight((int) (height * rect.scale));
		expectedRect.setX((int) (x - expectedRect.getWidth() * overlay.tryFlip(overlay.getAnchorX())));
		expectedRect.setY((int) (y - expectedRect.getHeight() * overlay.getAnchorY()));

		if (PreviewOptionsScreen.isAdjustingPosition()) {
			return;
		}

		IWailaConfig.BossBarOverlapMode mode = Jade.CONFIG.get().getGeneral().getBossBarOverlapMode();
		if (mode == IWailaConfig.BossBarOverlapMode.PUSH_DOWN) {
			Rect2i bossBarRect = ClientProxy.getBossBarRect();
			if (bossBarRect != null) {
				width = expectedRect.getWidth();
				height = expectedRect.getHeight();
				int rw = bossBarRect.getWidth();
				int rh = bossBarRect.getHeight();
				x = expectedRect.getX();
				y = expectedRect.getY();
				int rx = bossBarRect.getX();
				int ry = bossBarRect.getY();
				rw += rx;
				rh += ry;
				width += x;
				height += y;
				// check if tooltip intersects with boss bar
				if (rw > x && rh > y && width > rx && height > ry) {
					expectedRect.setY(bossBarRect.getHeight());
				}
			}
		}
	}

	public void updateRect(TooltipRect rect) {
		Rect2i src = rect.rect;
		if (src.getWidth() == 0) {
			src.setX(rect.expectedRect.getX());
			src.setY(rect.expectedRect.getY());
			src.setWidth(rect.expectedRect.getWidth());
			src.setHeight(rect.expectedRect.getHeight());
		} else {
			chase(rect, Rect2i::getX, src::setX);
			chase(rect, Rect2i::getY, src::setY);
			chase(rect, Rect2i::getWidth, src::setWidth);
			chase(rect, Rect2i::getHeight, src::setHeight);
		}
	}

	@Override
	public int padding(Direction2D direction) {
		if (padding != null) {
			return padding[direction.ordinal()];
		}
		return style.padding(direction);
	}

	@Override
	public void setPadding(Direction2D direction, int value) {
		if (padding == null) {
			padding = style.padding.clone();
		}
		padding[direction.ordinal()] = value;
	}

	@Override
	public BoxStyle getStyle() {
		return style;
	}
}
