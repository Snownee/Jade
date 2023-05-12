package snownee.jade.api.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public interface IBoxStyle {

	float borderWidth();

	void render(GuiGraphics guiGraphics, float x, float y, float w, float h);

	default void tag(ResourceLocation tag) {
	}

	enum Empty implements IBoxStyle {
		INSTANCE;

		@Override
		public float borderWidth() {
			return 0;
		}

		@Override
		public void render(GuiGraphics guiGraphics, float x, float y, float w, float h) {
		}
	}

}
