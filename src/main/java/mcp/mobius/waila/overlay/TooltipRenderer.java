package mcp.mobius.waila.overlay;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.config.WailaConfig.ConfigOverlay;
import mcp.mobius.waila.api.event.WailaTooltipEvent;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mcp.mobius.waila.impl.Tooltip;
import mcp.mobius.waila.impl.Tooltip.Line;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.MinecraftForge;

public class TooltipRenderer {

	private final Tooltip tooltip;
	private final boolean showIcon;
	private Vec2 totalSize;
	private float contentHeight;
	IElement icon;

	public TooltipRenderer(Tooltip tooltip, boolean showIcon) {
		WailaTooltipEvent event = new WailaTooltipEvent(tooltip, ObjectDataCenter.get());
		MinecraftForge.EVENT_BUS.post(event);
		this.showIcon = showIcon;
		this.tooltip = tooltip;
		if (showIcon) {
			icon = RayTracing.INSTANCE.getIcon();
		}

		computeSize();
	}

	public void computeSize() {
		float width = 0, height = 0;
		for (Line line : tooltip.lines) {
			Vec2 size = line.getSize();
			width = Math.max(width, size.x);
			height += size.y;
		}
		contentHeight = height;
		if (hasIcon()) {
			Vec2 size = icon.getCachedSize();
			width += 12 + size.x;
			height = Math.max(height, size.y - 2);
		} else {
			width += 10;
		}
		height += 6;
		totalSize = new Vec2(width, height);
	}

	public void draw(PoseStack matrixStack) {
		float x = 6;
		float y = 4;
		if (hasIcon()) {
			x = icon.getCachedSize().x + 8;
			if (icon.getCachedSize().y > contentHeight) {
				y += (icon.getCachedSize().y - contentHeight) / 2 - 1;
			}
		}

		for (Line line : tooltip.lines) {
			Vec2 size = line.getSize();
			line.render(matrixStack, x, y, totalSize.x, size.y);
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
			if (alphaChannel > 4) //dont know why
				mc.font.draw(matrixStack, "▾", x, y, 0xFFFFFF | alphaChannel << 24);
		}
	}

	public Tooltip getTooltip() {
		return tooltip;
	}

	public boolean hasIcon() {
		return showIcon && Waila.CONFIG.get().getGeneral().shouldShowIcon() && icon != null;
	}

	public Rect2i getPosition() {
		Window window = Minecraft.getInstance().getWindow();
		ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
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
