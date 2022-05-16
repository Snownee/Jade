package mcp.mobius.waila.api;

/**
 * Main interface used for Waila plugins. Provides a valid instance of {@link IRegistrar}.
 */
public interface IWailaPlugin {

	default void register(IWailaCommonRegistration registration) {

	}

	default void registerClient(IWailaClientRegistration registration) {

	}

}
