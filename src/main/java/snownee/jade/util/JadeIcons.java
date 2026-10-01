package snownee.jade.util;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import snownee.jade.Jade;
import snownee.jade.api.config.ConfigIconRenderer;

public final class JadeIcons {

	private JadeIcons() {
	}

	/**
	 * Creates an {@link ItemStack} that is safe to render even when no level is loaded.
	 * <p>
	 * Item components are bound to their holders only during datapack loading or registry sync, i.e. when a
	 * level is loaded or a server is joined. Before that, {@code item.builtInRegistryHolder().components()}
	 * throws, making {@code new ItemStack(item)} unusable (for example on the title screen). This replaces the
	 * registry-backed holder with a {@link Holder.Direct} that supplies the components itself.
	 * <p>
	 * The result only carries the item model, so state-dependent visuals (enchantment glint, potion color,
	 * banner patterns, ...) fall back to their defaults.
	 */
	public static ItemStack of(Item item) {
		return of(item, 1);
	}

	public static ItemStack of(Item item, int count) {
		return of(new ItemStackTemplate(item, count));
	}

	public static ItemStack of(ItemStackTemplate template) {
		if (template.is(Items.AIR)) {
			return ItemStack.EMPTY;
		}
		Holder<Item> item = template.item();
		if (item.areComponentsBound()) {
			return template.create();
		}
		Identifier model = BuiltInRegistries.ITEM.getKey(item.value());
		DataComponentMap.Builder builder = DataComponentMap.builder();
		builder.addAll(template.components().split().added());
		if (!builder.contains(DataComponents.ITEM_MODEL)) {
			builder.set(DataComponents.ITEM_MODEL, model);
		}
		return new ItemStack(new Holder.Direct<>(item.value(), builder.build()), template.count());
	}

	public static void render(
			GuiGraphicsExtractor guiGraphics,
			@Nullable ConfigIconRenderer renderer,
			int x,
			int y,
			int size) {
		if (renderer == null) {
			return;
		}
		try {
			renderer.render(guiGraphics, x, y, size);
		} catch (Throwable e) {
			Jade.LOGGER.error("Failed to render config icon {}", renderer, e);
		}
	}
}
