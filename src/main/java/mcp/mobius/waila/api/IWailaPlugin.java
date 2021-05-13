package mcp.mobius.waila.api;

/**
 * Main interface used for Waila plugins. Provides a valid instance of {@link IRegistrar}.
 */
public interface IWailaPlugin {

	/**
	 * @param registrar - An instance of IWailaRegistrar to register your providers with.
	 */
	void register(IRegistrar registrar);
}
