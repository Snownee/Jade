package snownee.jade.api;

/**
 * Extension point for providers that can be enabled or disabled from Jade's plugin configuration.
 */
public interface IToggleableProvider extends IJadeProvider {

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
