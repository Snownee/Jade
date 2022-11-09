package snownee.jade.api;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.view.IServerExtensionProvider;

@NonExtendable
public interface IWailaCommonRegistration {

	/**
	 * Register an {@link IServerDataProvider<BlockEntity>} instance for data syncing purposes.
	 *
	 * @param dataProvider The data provider instance
	 * @param block        The highest level class to apply to
	 */
	void registerBlockDataProvider(IServerDataProvider<BlockEntity> dataProvider, Class<? extends BlockEntity> block);

	/**
	 * Register an {@link IServerDataProvider<Entity>} instance for data syncing purposes.
	 *
	 * @param dataProvider The data provider instance
	 * @param entity       The highest level class to apply to
	 */
	void registerEntityDataProvider(IServerDataProvider<Entity> dataProvider, Class<? extends Entity> entity);

	@Experimental
	<T> void registerItemStorage(IServerExtensionProvider<T, ItemStack> provider, Class<? extends T> clazz);

	@Experimental
	<T> void registerFluidStorage(IServerExtensionProvider<T, CompoundTag> provider, Class<? extends T> clazz);

	@Experimental
	<T> void registerEnergyStorage(IServerExtensionProvider<T, CompoundTag> provider, Class<? extends T> clazz);

	@Experimental
	<T> void registerProgress(IServerExtensionProvider<T, CompoundTag> provider, Class<? extends T> clazz);

}
