package snownee.jade.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public interface IServerDataProvider<T> extends IJadeProvider {

	/**
	 * Callback used server side to return a custom synchronization NBTTagCompound.</br>
	 * Will only be called if the implementing class is registered via {@link IWailaCommonRegistration#registerBlockDataProvider} or {@link IWailaCommonRegistration#registerEntityDataProvider}.</br>
	 *
	 * @param data        Current synchronization tag (might have been processed by other providers and might be processed by other providers).
	 * @param player      The player requesting data synchronization (The owner of the current connection).
	 * @param world       The world.
	 * @param t           The type targeted for synchronization.
	 * @param showDetails Should show details (for example: show more inventory items in tooltip while player is pressing SHIFT by default).
	 */
	void appendServerData(CompoundTag data, ServerPlayer player, Level world, T t, boolean showDetails);
}
