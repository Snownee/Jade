package snownee.jade.api;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.config.TargetOperationRepository;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;

/**
 * Common registration entry point for server-side Jade integrations.
 */
@NonExtendable
public interface IWailaCommonRegistration {

	/**
	 * Registers a block data provider.
	 *
	 * @param dataProvider provider that writes synchronized block data
	 * @param blockOrBlockEntityClass highest-level block or block entity class to match
	 */
	void registerBlockDataProvider(IServerDataProvider<BlockAccessor> dataProvider, Class<?> blockOrBlockEntityClass);

	/**
	 * Registers an entity data provider.
	 *
	 * @param dataProvider provider that writes synchronized entity data
	 * @param entityClass highest-level entity class to match
	 */
	void registerEntityDataProvider(IServerDataProvider<EntityAccessor> dataProvider, Class<? extends Entity> entityClass);

	/**
	 * Returns the block operation repository.
	 *
	 * @return block operation repository
	 */
	TargetOperationRepository<Block, BlockState> blockOperations();

	/**
	 * Returns the entity-type operation repository.
	 *
	 * @return entity-type operation repository
	 */
	TargetOperationRepository<EntityType<?>, Entity> entityTypeOperations();

	/**
	 * Returns the mob-effect operation repository.
	 *
	 * @return mob-effect operation repository
	 */
	TargetOperationRepository<MobEffect, MobEffectInstance> mobEffectOperations();

	/**
	 * Registers a storage extension provider for item stacks.
	 *
	 * @param provider storage provider
	 * @param clazz target class to match
	 * @param <T> target type
	 */
	<T> void registerItemStorage(IServerExtensionProvider<ItemStack> provider, Class<? extends T> clazz);

	/**
	 * Registers a storage extension provider for fluids.
	 *
	 * @param provider storage provider
	 * @param clazz target class to match
	 * @param <T> target type
	 */
	<T> void registerFluidStorage(IServerExtensionProvider<FluidView.Data> provider, Class<? extends T> clazz);

	/**
	 * Registers a storage extension provider for energy.
	 *
	 * @param provider storage provider
	 * @param clazz target class to match
	 * @param <T> target type
	 */
	<T> void registerEnergyStorage(IServerExtensionProvider<EnergyView.Data> provider, Class<? extends T> clazz);

	/**
	 * Registers a storage extension provider for progress values.
	 *
	 * @param provider storage provider
	 * @param clazz target class to match
	 * @param <T> target type
	 */
	<T> void registerProgress(IServerExtensionProvider<ProgressView.Data> provider, Class<? extends T> clazz);

}
