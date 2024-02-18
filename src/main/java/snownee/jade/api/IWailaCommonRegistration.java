package snownee.jade.api;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.view.IServerExtensionProvider;

@NonExtendable
public interface IWailaCommonRegistration {

	/**
	 * Register an {@link IServerDataProvider<BlockAccessor>} instance for data syncing purposes.
	 *
	 * @param dataProvider            The data provider instance
	 * @param blockOrBlockEntityClass The highest level class to apply to
	 */
	void registerBlockDataProvider(IServerDataProvider<BlockAccessor> dataProvider, Class<?> blockOrBlockEntityClass);

	/**
	 * Register an {@link IServerDataProvider<EntityAccessor>} instance for data syncing purposes.
	 *
	 * @param dataProvider The data provider instance
	 * @param entityClass  The highest level class to apply to
	 */
	void registerEntityDataProvider(IServerDataProvider<EntityAccessor> dataProvider, Class<? extends Entity> entityClass);

	<T> void registerItemStorage(IServerExtensionProvider<ItemStack> provider, Class<? extends T> clazz);

	<T> void registerFluidStorage(IServerExtensionProvider<CompoundTag> provider, Class<? extends T> clazz);

	<T> void registerEnergyStorage(IServerExtensionProvider<CompoundTag> provider, Class<? extends T> clazz);

	<T> void registerProgress(IServerExtensionProvider<CompoundTag> provider, Class<? extends T> clazz);

}
