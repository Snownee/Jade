package snownee.jade.api.callback;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Rect2i;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;

public interface JadeAfterRenderCallback {

	void afterRender(ITooltip tooltip, Rect2i rect, PoseStack matrixStack, Accessor<?> accessor);

}
