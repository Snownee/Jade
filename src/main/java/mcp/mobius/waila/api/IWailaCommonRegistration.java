package mcp.mobius.waila.api;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

@Experimental
public interface IWailaCommonRegistration {

	/**
     * Register a namespaced config key to be accessed within data providers.
     *
     * @param key the namespaced key
     * @param defaultValue the default value
     */
	void addConfig(ResourceLocation key, boolean defaultValue);

	/**
     * Register a namespaced config key to be accessed within data providers. These values are sent from the server to
     * the client upon connection.
     *
     * @param key The namespaced key
     * @param defaultValue The default value
     */
	void addSyncedConfig(ResourceLocation key, boolean defaultValue);

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
