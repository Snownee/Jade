package snownee.jade.api.config;

import net.minecraft.resources.Identifier;

/**
 * Icon displayed for a primary plugin config entry in the plugin config screen.
 * <p>
 * An icon is {@link None} (no icon), a GUI {@link Sprite}, or an {@link Item}. The item form takes a plain
 * {@link net.minecraft.world.item.Item} so it can be created during plugin registration, before any level is
 * loaded. Only primary config keys can have an icon.
 */
public sealed interface ConfigIcon {

	final class None implements ConfigIcon {
		private static final None INSTANCE = new None();

		private None() {
		}
	}

	record Sprite(Identifier sprite) implements ConfigIcon {
	}

	record Item(net.minecraft.world.item.Item item) implements ConfigIcon {
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
		return new Item(item);
	}
}
