package snownee.jade.overlay;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.config.IWailaConfig.IconMode;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.ITooltipRenderer;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.Tooltip.Line;

public class TooltipRenderer implements ITooltipRenderer {

	private final Tooltip tooltip;
	private final boolean showIcon;
	private Vec2 totalSize;
	private IElement icon;
	// top, right, bottom, left
	private float[] padding = new float[] { 4, 3, 1, 4 };

	public TooltipRenderer(Tooltip tooltip, boolean showIcon) {
		this.showIcon = showIcon;
		this.tooltip = tooltip;

		//		setPadding(0, 0);
		//		setPadding(1, 0);
		//		setPadding(2, 0);
		//		setPadding(3, 0);

		if (showIcon) {
			icon = RayTracing.INSTANCE.getIcon();
		}

		recalculateSize();
	}

	@Override
	public float getPadding(int i) {
		return padding[i];
	}

	@Override
	public void setPadding(int i, float value) {
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

	public void draw(PoseStack matrixStack) {
		float x = getPadding(LEFT);
		float y = getPadding(TOP);

		for (Line line : tooltip.lines) {
			Vec2 size = line.getSize();
			line.render(matrixStack, x, y, totalSize.x - getPadding(RIGHT), size.y);
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
				if (alphaChannel > 4)
					mc.font.draw(matrixStack, "▾", x, y, 0xFFFFFF | alphaChannel << 24);
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
			Tooltip.drawBorder(matrixStack, offsetX, offsetY, icon);
			icon.render(matrixStack, offsetX, offsetY, offsetX + size.x, offsetY + size.y);
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

}
