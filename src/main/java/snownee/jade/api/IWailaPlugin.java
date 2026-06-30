package snownee.jade.api;

/**
 * Main interface used for Waila plugins. Provides a valid instance of {@link IWailaCommonRegistration} and {@link IWailaClientRegistration}.
 */
public interface IWailaPlugin {
	/**
	 * Registers server-side integrations.
	 *
	 * @param registration common registration API
	 */
	default void register(IWailaCommonRegistration registration) {

	}

	/**
	 * Registers client-side integrations.
	 *
	 * @param registration client registration API
	 */
	default void registerClient(IWailaClientRegistration registration) {

	}
}
