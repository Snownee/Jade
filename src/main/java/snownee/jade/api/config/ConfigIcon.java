package snownee.jade.api.config;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * Icon displayed for a primary plugin config entry in the plugin config screen.
 * <p>
 * An icon is {@link None} (no icon), a GUI {@link Sprite}, or an {@link Item}. The item form takes an
 * {@link ItemStackTemplate} so it can be created during plugin registration, before any level is loaded. Only
 * primary config keys can have an icon.
 */
public interface ConfigIcon {

	final class None implements ConfigIcon {
		private static final None INSTANCE = new None();

		private None() {
		}
	}

	record Sprite(Identifier sprite) implements ConfigIcon {
	}

	record Item(ItemStackTemplate itemStack) implements ConfigIcon {
	}

	default boolean isNone() {
		return this instanceof None;
	}

	static ConfigIcon none() {
		return None.INSTANCE;
	}

	static ConfigIcon sprite(Identifier sprite) {
		return new Sprite(sprite);
	}

	static ConfigIcon item(net.minecraft.world.item.Item item) {
		return item(new ItemStackTemplate(item));
	}

	static ConfigIcon item(net.minecraft.world.item.Item item, DataComponentPatch components) {
		return item(new ItemStackTemplate(item, components));
	}

	static ConfigIcon item(ItemStackTemplate itemStack) {
		return new Item(itemStack);
	}
}
