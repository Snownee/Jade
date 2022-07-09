package snownee.jade.overlay;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.Tooltip.Line;

public class TooltipRenderer {

	private final Tooltip tooltip;
	private final boolean showIcon;
	private Vec2 totalSize;
	private float contentHeight;
	IElement icon;

	public TooltipRenderer(Tooltip tooltip, boolean showIcon) {
		this.showIcon = showIcon;
		this.tooltip = tooltip;
		if (showIcon) {
			icon = RayTracing.INSTANCE.getIcon();
		}

		computeSize();
	}

	public float getPadding() {
		return 2;
	}

	public void computeSize() {
		float width = 0, height = 0;
		for (Line line : tooltip.lines) {
			Vec2 size = line.getSize();
			width = Math.max(width, size.x);
			height += size.y;
		}
		contentHeight = height;
		float padding = getPadding() * 2;
		if (hasIcon()) {
			Vec2 size = icon.getCachedSize();
			width += 2 + size.x;
			height = Math.max(height, size.y - 2);
		}
		width += padding + 4;
		height += padding;
		totalSize = new Vec2(width, height);
	}

	public void draw(PoseStack matrixStack) {
		float padding = getPadding();
		float x = padding + 3;
		float y = padding + 1;
		if (hasIcon()) {
			x += icon.getCachedSize().x + 2;
			if (icon.getCachedSize().y > contentHeight) {
				y += (icon.getCachedSize().y - contentHeight) / 2 - 1;
			}
		}

		for (Line line : tooltip.lines) {
			Vec2 size = line.getSize();
			line.render(matrixStack, x, y, totalSize.x - padding - 1, size.y);
			y += size.y;
		}

		if (tooltip.sneakyDetails) {
			Minecraft mc = Minecraft.getInstance();
			x = (totalSize.x - mc.font.width("▾") + 1) / 2f;
			float yOffset = (OverlayRenderer.ticks / 5) % 8 - 2;
			if (yOffset > 4)
				return;
			y = totalSize.y - 6 + yOffset;
			float alpha = 1 - Math.abs(yOffset) / 2;
			int alphaChannel = (int) (0xFF * Mth.clamp(alpha, 0, 1));
			if (alphaChannel > 4)
				mc.font.draw(matrixStack, "▾", x, y, 0xFFFFFF | alphaChannel << 24);
		}
	}

	public Tooltip getTooltip() {
		return tooltip;
	}

	public boolean hasIcon() {
		return showIcon && Jade.CONFIG.get().getOverlay().shouldShowIcon() && icon != null;
	}

	public Rect2i getPosition() {
		Window window = Minecraft.getInstance().getWindow();
		IConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		int x = (int) (window.getGuiScaledWidth() * overlay.tryFlip(overlay.getOverlayPosX()));
		int y = (int) (window.getGuiScaledHeight() * (1.0F - overlay.getOverlayPosY()));
		int width = (int) totalSize.x;
		int height = (int) totalSize.y;
		return new Rect2i(x, y, width, height);
	}

	public Vec2 getSize() {
		return totalSize;
	}

}
