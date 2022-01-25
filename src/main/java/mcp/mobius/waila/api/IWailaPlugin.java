package mcp.mobius.waila.api;

import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * Main interface used for Waila plugins. Provides a valid instance of {@link IRegistrar}.
 */
public interface IWailaPlugin {

	/**
	 * @param registrar - An instance of IWailaRegistrar to register your providers with.
	 */
	default void register(IRegistrar registrar) {

	}

	@Experimental
	default void register(IWailaCommonRegistration registration) {

	}

	@Experimental
	default void registerClient(IWailaClientRegistration registration) {

	}

}
