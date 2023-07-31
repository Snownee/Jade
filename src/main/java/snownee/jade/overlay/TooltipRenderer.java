package snownee.jade.overlay;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.config.IWailaConfig.IconMode;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.ITooltipRenderer;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.Tooltip.Line;
import snownee.jade.impl.config.WailaConfig.ConfigOverlay;
import snownee.jade.util.ClientProxy;

public class TooltipRenderer implements ITooltipRenderer {

	private final Tooltip tooltip;
	private final boolean showIcon;
	private Vec2 totalSize;
	private IElement icon;
	// top, right, bottom, left
	private final int[] padding = new int[] { 4, 3, 1, 4 };
	private Rect2i realRect;
	private float realScale = 1;

	public TooltipRenderer(Tooltip tooltip, boolean showIcon) {
		this.showIcon = showIcon;
		this.tooltip = tooltip;
		if (showIcon) {
			icon = RayTracing.INSTANCE.getIcon();
		}
	}

	@Override
	public int getPadding(int i) {
		return padding[i];
	}

	@Override
	public void setPadding(int i, int value) {
		padding[i] = value;
	}

	public void recalculateSize() {
		float width = 0, height = 0;
		for (Line line : tooltip.lines) {
			Vec2 size = line.getSize();
			width = Math.max(width, size.x);
			height += size.y;
		}
		float contentHeight = height;
		if (hasIcon()) {
			Vec2 size = icon.getCachedSize();
			padding[LEFT] += size.x + 3;
			height = Math.max(height, size.y);
		}
		width += padding[LEFT] + padding[RIGHT];
		height += padding[TOP] + padding[BOTTOM];
		totalSize = new Vec2(width, height);

		if (hasIcon() && icon.getCachedSize().y > contentHeight) {
			padding[TOP] += (icon.getCachedSize().y - contentHeight) / 2;
		}
	}

	public void draw(GuiGraphics guiGraphics) {
		float x = getPadding(LEFT);
		float y = getPadding(TOP);

		for (Line line : tooltip.lines) {
			Vec2 size = line.getSize();
			line.render(guiGraphics, x, y, totalSize.x - getPadding(RIGHT), size.y);
			y += size.y;
		}

		if (tooltip.sneakyDetails) {
			Minecraft mc = Minecraft.getInstance();
			x = (totalSize.x - mc.font.width("▾") + 1) / 2f;
			float yOffset = (OverlayRenderer.ticks / 5) % 8 - 2;
			if (yOffset <= 4) {
				y = totalSize.y - 6 + yOffset;
				float alpha = 1 - Math.abs(yOffset) / 2;
				int alphaChannel = (int) (0xFF * Mth.clamp(alpha, 0, 1));
				if (alphaChannel > 4) {
					guiGraphics.pose().pushPose();
					guiGraphics.pose().translate(x, y, 0);
					DisplayHelper.INSTANCE.drawText(guiGraphics, "▾", 0, 0, IThemeHelper.get().theme().infoColor & 0x00FFFFFF | alphaChannel << 24);
					guiGraphics.pose().popPose();
				}
			}
		}

		IElement icon = getIcon();
		if (icon != null) {
			Vec2 size = icon.getCachedSize();
			Vec2 offset = icon.getTranslation();
			float offsetY = offset.y;
			float min = getPadding(TOP) + getPadding(BOTTOM) + size.y;
			if (IWailaConfig.get().getOverlay().getIconMode() == IconMode.TOP && min < totalSize.y) {
				offsetY += getPadding(TOP);
			} else {
				offsetY += (totalSize.y - size.y) / 2;
			}
			float offsetX = getPadding(LEFT) + offset.x - size.x - 3;
			Tooltip.drawBorder(guiGraphics, offsetX, offsetY, icon);
			icon.render(guiGraphics, offsetX, offsetY, offsetX + size.x, offsetY + size.y);
		}
	}

	@Override
	public Tooltip getTooltip() {
		return tooltip;
	}

	@Override
	public boolean hasIcon() {
		return showIcon && Jade.CONFIG.get().getOverlay().shouldShowIcon() && icon != null;
	}

	@Override
	public IElement getIcon() {
		return hasIcon() ? icon : null;
	}

	@Override
	public void setIcon(IElement icon) {
		this.icon = icon;
	}

	@Override
	public Rect2i getPosition() {
		Window window = Minecraft.getInstance().getWindow();
		IConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		int x = (int) (window.getGuiScaledWidth() * overlay.tryFlip(overlay.getOverlayPosX()));
		int y = (int) (window.getGuiScaledHeight() * (1.0F - overlay.getOverlayPosY()));
		int width = (int) totalSize.x;
		int height = (int) totalSize.y;
		return new Rect2i(x, y, width, height);
	}

	@Override
	public Vec2 getSize() {
		return totalSize;
	}

	@Override
	public void setSize(Vec2 totalSize) {
		this.totalSize = totalSize;
	}

	@Override
	public float getRealScale() {
		return realScale;
	}

	@Override
	@Nullable
	public Rect2i getRealRect() {
		return realRect;
	}

	@Override
	public void recalculateRealRect() {
		Rect2i position = getPosition();
		ConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		if (!overlay.getSquare() || IThemeHelper.get().theme().backgroundTexture != null) {
			position.setWidth(position.getWidth() + 2);
			position.setHeight(position.getHeight() + 2);
			position.setPosition(position.getX() + 1, position.getY() + 1);
		}

		realScale = overlay.getOverlayScale();
		Window window = Minecraft.getInstance().getWindow();
		float thresholdHeight = window.getGuiScaledHeight() * overlay.getAutoScaleThreshold();
		if (totalSize.y * realScale > thresholdHeight) {
			realScale = Math.max(realScale * 0.5f, thresholdHeight / totalSize.y);
		}

		position.setWidth((int) (position.getWidth() * realScale));
		position.setHeight((int) (position.getHeight() * realScale));
		position.setX((int) (position.getX() - position.getWidth() * overlay.tryFlip(overlay.getAnchorX())));
		position.setY((int) (position.getY() - position.getHeight() * overlay.getAnchorY()));

		BossBarOverlapMode mode = Jade.CONFIG.get().getGeneral().getBossBarOverlapMode();
		if (mode == BossBarOverlapMode.PUSH_DOWN) {
			Rect2i rect = ClientProxy.getBossBarRect();
			if (rect != null) {
				int tw = position.getWidth();
				int th = position.getHeight();
				int rw = rect.getWidth();
				int rh = rect.getHeight();
				int tx = position.getX();
				int ty = position.getY();
				int rx = rect.getX();
				int ry = rect.getY();
				rw += rx;
				rh += ry;
				tw += tx;
				th += ty;
				// check if tooltip intersects with boss bar
				if (rw > tx && rh > ty && tw > rx && th > ry) {
					position.setY(rect.getHeight());
				}
			}
		}
		realRect = position;
	}

	public void setPaddingFromTheme(Theme theme) {
		for (int i = 0; i < 4; i++) {
			setPadding(i, theme.padding[i]);
		}
		recalculateSize();
	}
}
