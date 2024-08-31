package snownee.jade.api;

/**
 * Main interface used for Waila plugins. Provides a valid instance of {@link IWailaCommonRegistration} and {@link IWailaClientRegistration}.
 */
public interface IWailaPlugin {
	default void register(IWailaCommonRegistration registration) {

	}

	default void registerClient(IWailaClientRegistration registration) {

	}
}
