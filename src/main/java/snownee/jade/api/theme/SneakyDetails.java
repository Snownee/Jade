package snownee.jade.api.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import snownee.jade.api.JadeIds;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.overlay.OverlayRenderer;

public interface SneakyDetails {
	SneakyDetails DEFAULT = new Simple(JadeIds.JADE("details_arrow"), 7, 5, 0, 0, "breath", 1F, 12F);
	Codec<SneakyDetails> CODEC = Codec.STRING.dispatch(SneakyDetails::type, SneakyDetails::codec);

	static MapCodec<? extends SneakyDetails> codec(String s) {
		if ("simple".equals(s)) {
			return Simple.CODEC;
		}
		throw new UnsupportedOperationException();
	}

	void render(GuiGraphicsExtractor graphics, float partialTicks, BoxElement element);

	String type();

	record Simple(
			Identifier sprite,
			int width,
			int height,
			float offsetX,
			float offsetY,
			String animation,
			float animationDistance,
			float animationPeriod) implements SneakyDetails {
		public static final MapCodec<Simple> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Identifier.CODEC.fieldOf("sprite").forGetter(Simple::sprite),
				Codec.INT.fieldOf("width").forGetter(Simple::width),
				Codec.INT.fieldOf("height").forGetter(Simple::height),
				Codec.FLOAT.optionalFieldOf("offsetX", 0F).forGetter(Simple::offsetX),
				Codec.FLOAT.optionalFieldOf("offsetY", 0F).forGetter(Simple::offsetY),
				Codec.STRING.optionalFieldOf("animation", "").forGetter(Simple::animation),
				ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("animationDistance", 1F).forGetter(Simple::animationDistance),
				ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("animationPeriod", 12F).forGetter(Simple::animationPeriod)
		).apply(i, Simple::new));

		@Override
		public void render(GuiGraphicsExtractor graphics, float partialTicks, BoxElement element) {
			float x = element.getX() + element.getWidth() / 2f - width / 2f + offsetX;
			float y = element.getY() + element.getHeight() - height / 2f + offsetY;
			float alpha = 1f;
			if ("breath".equals(animation)) {
				float breath = (OverlayRenderer.ticks / 5) % animationPeriod - 2; //range: -2 to 6
				if (breath > 4) {
					return;
				}
				alpha = 1 - Math.abs(breath) / 2;
				y += animationDistance * breath;
				if (alpha < 0.016f) {
					return; // too transparent
				}
			}
			graphics.pose().pushMatrix();
			graphics.pose().translate(x, y);
			IDisplayHelper.get().blitSprite(graphics, RenderPipelines.GUI_TEXTURED, sprite, 0, 0, width, height, ARGB.white(alpha));
			graphics.pose().popMatrix();
		}

		@Override
		public String type() {
			return "simple";
		}
	}
}
