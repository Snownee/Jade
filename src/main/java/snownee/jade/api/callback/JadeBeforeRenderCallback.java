package snownee.jade.api.callback;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Rect2i;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;

@FunctionalInterface
public interface JadeBeforeRenderCallback {

	class ColorSetting {
		public float alpha;
		public int backgroundColor;
		public int gradientStart;
		public int gradientEnd;
	}

	boolean beforeRender(ITooltip tooltip, Rect2i rect, PoseStack matrixStack, Accessor<?> accessor, ColorSetting color);

}
