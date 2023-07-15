package snownee.jade.api;

public interface IToggleableProvider extends IJadeProvider {

	/**
	 * Whether this provider can be disabled in config.
	 */
	default boolean isRequired() {
		return false;
	}

	default boolean enabledByDefault() {
		return true;
	}

}
