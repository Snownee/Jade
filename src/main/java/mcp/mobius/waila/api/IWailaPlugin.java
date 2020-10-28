package mcp.mobius.waila.api;

/**
 * Main interface used for Waila plugins. Provides a valid instance of {@link IRegistrar}.
 * <p>
 * Include this class in the <code>initializers</code> field in your <code>fabric.mod.json</code> file.
 */
public interface IWailaPlugin {

    /**
     * @param registrar - An instance of IWailaRegistrar to register your providers with.
     */
    void register(IRegistrar registrar);
}
