package mcp.mobius.waila.overlay;

import java.awt.Rectangle;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.config.WailaConfig.ConfigOverlay;
import mcp.mobius.waila.api.event.WailaTooltipEvent;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mcp.mobius.waila.impl.Tooltip;
import mcp.mobius.waila.impl.Tooltip.Line;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraftforge.common.MinecraftForge;

public class TooltipRenderer {

	private final Tooltip tooltip;
	private final boolean showIcon;
	private final Vector2f totalSize;
	IElement icon;

	public TooltipRenderer(Tooltip tooltip, boolean showIcon) {
		WailaTooltipEvent event = new WailaTooltipEvent(tooltip, ObjectDataCenter.get());
		MinecraftForge.EVENT_BUS.post(event);

		Minecraft.getInstance();
		this.showIcon = showIcon;
		this.tooltip = tooltip;
		if (showIcon) {
			icon = RayTracing.INSTANCE.getIcon();
		}

		totalSize = computeSize();
	}

	public Vector2f computeSize() {
		float width = 0, height = 0;
		for (Line line : tooltip.lines) {
			Vector2f size = line.getSize();
			width = Math.max(width, size.x);
			height += size.y;
		}
		if (hasIcon()) {
			Vector2f size = icon.getCachedSize();
			width += 12 + size.x;
			height = Math.max(height, size.y - 5);
		} else {
			width += 10;
		}
		height += 8;
		return new Vector2f(width, height);
	}

	public void draw(MatrixStack matrixStack) {
		Rectangle position = getPosition();

		float x = 6;
		if (hasIcon()) {
			x = icon.getCachedSize().x + 8;
		}
		float y = 6;

		for (Line line : tooltip.lines) {
			Vector2f size = line.getSize();
			line.render(matrixStack, x, y, totalSize.x, size.y);
			y += size.y;
		}
		position.width += x - 2;
	}

	public Tooltip getTooltip() {
		return tooltip;
	}

	public boolean hasIcon() {
		return showIcon && Waila.CONFIG.get().getGeneral().shouldShowIcon() && icon != null;
	}

	public Rectangle getPosition() {
		MainWindow window = Minecraft.getInstance().getMainWindow();
		ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
		//        int x = (int) (window.getScaledWidth() * overlay.tryFlip(overlay.getOverlayPosX()) - totalSize.width * overlay.tryFlip(overlay.getAnchorX()));
		//        int y = (int) (window.getScaledHeight() * (1.0F - overlay.getOverlayPosY()) - totalSize.height * overlay.getAnchorY());
		int x = (int) (window.getScaledWidth() * overlay.tryFlip(overlay.getOverlayPosX()));
		int y = (int) (window.getScaledHeight() * (1.0F - overlay.getOverlayPosY()));
		return new Rectangle(x, y, (int) totalSize.x, (int) totalSize.y);
	}

	public Vector2f getSize() {
		return totalSize;
	}

}
