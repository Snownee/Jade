package mcp.mobius.waila.api;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

/**
 * Main interface used for Waila plugins. Provides a valid instance of {@link IRegistrar}.
 */
public interface IWailaPlugin {

	/**
	 * @param registrar - An instance of IWailaRegistrar to register your providers with.
	 */
	@Deprecated(forRemoval = true, since = "1.19")
	@ScheduledForRemoval(inVersion = "1.19")
	default void register(IRegistrar registrar) {

	}

	default void register(IWailaCommonRegistration registration) {

	}

	default void registerClient(IWailaClientRegistration registration) {

	}

}
