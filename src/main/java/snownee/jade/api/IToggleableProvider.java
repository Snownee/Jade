package snownee.jade.api;

import snownee.jade.api.config.ConfigIcon;

/**
 * Extension point for providers that can be enabled or disabled from Jade's plugin configuration.
 */
public interface IToggleableProvider extends IJadeProvider {

	/**
	 * Returns the icon displayed for this provider's config entry.
	 *
	 * @return the config entry icon, or {@link ConfigIcon#none()} for no icon
	 */
	default ConfigIcon getConfigIcon() {
		return ConfigIcon.none();
	}

	/**
	 * Returns whether this provider must always stay enabled.
	 *
	 * @return {@code true} if the provider is required and cannot be disabled
	 */
	default boolean isRequired() {
		return false;
	}

	/**
	 * Returns whether this provider should be enabled by default.
	 *
	 * @return {@code true} if the provider starts enabled
	 */
	default boolean enabledByDefault() {
		return true;
	}

}
