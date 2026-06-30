package snownee.jade.api;

import net.minecraft.nbt.CompoundTag;

/**
 * Supplies server-side data that Jade synchronizes to the client for a specific accessor type.
 *
 * @param <T> accessor type handled by this provider
 */
public interface IServerDataProvider<T extends Accessor<?>> extends IJadeProvider {

	/**
	 * Populates the synchronization tag that will be sent to the client.
	 *
	 * <p>This method is only invoked on the logical server after the provider has been registered through
	 * {@link IWailaCommonRegistration#registerBlockDataProvider(IServerDataProvider, Class)} or
	 * {@link IWailaCommonRegistration#registerEntityDataProvider(IServerDataProvider, Class)}.
	 *
	 * @param data mutable synchronization data shared with other server data providers
	 * @param accessor accessor describing the current target and context
	 */
	void appendServerData(CompoundTag data, T accessor);

	/**
	 * Returns whether the client should request data for this accessor.
	 *
	 * @param accessor accessor describing the current target and context
	 * @return {@code true} if data should be requested from the server
	 */
	default boolean shouldRequestData(T accessor) {
		return true;
	}
}
