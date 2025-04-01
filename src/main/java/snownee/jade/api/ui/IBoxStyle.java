package snownee.jade.api.ui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.resources.ResourceLocation;

public interface IBoxStyle {

	float borderWidth();

	void render(PoseStack matrixStack, float x, float y, float w, float h);

	default void tag(ResourceLocation tag) {
	}

	enum Empty implements IBoxStyle {
		INSTANCE;

		@Override
		public float borderWidth() {
			return 0;
		}

		@Override
		public void render(PoseStack matrixStack, float x, float y, float w, float h) {
		}
	}

}
