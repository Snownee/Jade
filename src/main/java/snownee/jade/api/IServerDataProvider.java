package snownee.jade.api;

import net.minecraft.nbt.CompoundTag;

public interface IServerDataProvider<T extends Accessor<?>> extends IJadeProvider {

	/**
	 * Callback used server side to return a custom synchronization NBTTagCompound.</br>
	 * Will only be called if the implementing class is registered via {@link IWailaCommonRegistration#registerBlockDataProvider} or {@link IWailaCommonRegistration#registerEntityDataProvider}.</br>
	 *
	 * @param data     Current synchronization tag (might have been processed by other providers and might be processed by other providers).
	 * @param accessor Contains the relevant information about the current environment.
	 */
	void appendServerData(CompoundTag data, T accessor);

	default boolean shouldRequestData(T accessor) {
		return true;
	}
}
