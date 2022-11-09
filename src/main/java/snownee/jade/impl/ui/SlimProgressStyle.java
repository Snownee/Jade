package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IProgressStyle;
import snownee.jade.overlay.DisplayHelper;

public class SlimProgressStyle implements IProgressStyle {

	public int color;
	@Nullable
	public IElement overlay;

	@Override
	public IProgressStyle color(int color, int color2) {
		if (color != color2) {
			throw new UnsupportedOperationException();
		}
		this.color = color;
		return this;
	}

	@Override
	public IProgressStyle textColor(int color) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IProgressStyle vertical(boolean vertical) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IProgressStyle overlay(IElement overlay) {
		this.overlay = overlay;
		return this;
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float w, float h, float progress, Component text) {
		if (overlay != null) {
			Vec2 size = new Vec2(w * progress, h);
			overlay.size(size);
			overlay.render(matrixStack, x, y - 1, size.x, size.y);
		} else {
			DisplayHelper.INSTANCE.drawGradientProgress(matrixStack, x, y - 1, w, h, progress, color);
		}
	}

}
