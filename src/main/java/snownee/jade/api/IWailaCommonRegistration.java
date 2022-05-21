package snownee.jade.api;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

@NonExtendable
public interface IWailaCommonRegistration {

	/**
	 * Register an {@link IServerDataProvider<BlockEntity>} instance for data syncing purposes.
	 *
	 * @param dataProvider The data provider instance
	 * @param block The highest level class to apply to
	 */
	void registerBlockDataProvider(IServerDataProvider<BlockEntity> dataProvider, Class<? extends BlockEntity> block);

	/**
	 * Register an {@link IServerDataProvider<Entity>} instance for data syncing purposes.
	 *
	 * @param dataProvider The data provider instance
	 * @param entity The highest level class to apply to
	 */
	void registerEntityDataProvider(IServerDataProvider<Entity> dataProvider, Class<? extends Entity> entity);

}
