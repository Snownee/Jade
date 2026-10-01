package snownee.jade.api.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import snownee.jade.util.JadeIcons;

/**
 * Client-side counterpart of {@link ConfigIcon} that caches whatever is needed to draw the icon (for example
 * the {@link ItemStack} resolved from an {@link ItemStackTemplate}) so rendering does not rebuild it every
 * frame. Create one with {@link #of(ConfigIcon)}.
 */
public interface ConfigIconRenderer {

	ConfigIconRenderer NONE = new None();

	void render(GuiGraphicsExtractor guiGraphics, int x, int y, int size);

	static ConfigIconRenderer of(ConfigIcon icon) {
		if (icon instanceof ConfigIcon.Sprite(Identifier sprite)) {
			return new Sprite(sprite);
		}
		if (icon instanceof ConfigIcon.Item(ItemStackTemplate itemStack)) {
			return new Item(JadeIcons.of(itemStack));
		}
		return NONE;
	}

	final class None implements ConfigIconRenderer {
		private None() {
		}

		@Override
		public void render(GuiGraphicsExtractor guiGraphics, int x, int y, int size) {
		}
	}

	record Sprite(Identifier sprite) implements ConfigIconRenderer {
		@Override
		public void render(GuiGraphicsExtractor guiGraphics, int x, int y, int size) {
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, size, size);
		}
	}

	record Item(ItemStack itemStack) implements ConfigIconRenderer {
		@Override
		public void render(GuiGraphicsExtractor guiGraphics, int x, int y, int size) {
			guiGraphics.fakeItem(itemStack, x, y);
		}
	}
}
