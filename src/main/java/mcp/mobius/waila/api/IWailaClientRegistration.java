package mcp.mobius.waila.api;

import org.jetbrains.annotations.ApiStatus.Experimental;

import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.ui.IDisplayHelper;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

@Experimental
public interface IWailaClientRegistration {

	/**
     * Register an {@link IComponentProvider} instance to allow overriding the icon for a block via the
     * {@link IComponentProvider#getIcon(BlockAccessor, IPluginConfig, IElement)} method.
     *
     * @param dataProvider The data provider instance
     * @param block The highest level class to apply to
     */
	void registerIconProvider(IComponentProvider dataProvider, Class<? extends Block> block);

	/**
     * Register an {@link IComponentProvider} instance for appending informations to
     * the tooltip.
     *
     * @param dataProvider The data provider instance
     * @param position The position on the tooltip this applies to
     * @param block The highest level class to apply to
     */
	void registerComponentProvider(IComponentProvider dataProvider, TooltipPosition position, Class<? extends Block> block);

	/**
     * Register an {@link IEntityComponentProvider} instance to allow overriding the icon for a entity via the
     * {@link IEntityComponentProvider#getIcon(EntityAccessor, IPluginConfig, IElement)} method.
     *
     * @param dataProvider The data provider instance
     * @param entity The highest level class to apply to
     */
	void registerIconProvider(IEntityComponentProvider dataProvider, Class<? extends Entity> entity);

	/**
     * Register an {@link IEntityComponentProvider} instance for appending {@link net.minecraft.util.text.Component}
     * to the tooltip.
     *
     * @param dataProvider The data provider instance
     * @param position The position on the tooltip this applies to
     * @param entity The highest level class to apply to
     */
	void registerComponentProvider(IEntityComponentProvider dataProvider, TooltipPosition position, Class<? extends Entity> entity);

	/**
	 * Mark a block as hidden in tooltip.
	 */
	void hideTarget(Block block);

	/**
	 * Mark an entity as hidden in tooltip. If player is aiming to this entity, it will be ignored and
	 * try to find next possible target.
	 */
	void hideTarget(EntityType<?> entityType);

	/**
	 * Mark a block to show name of the picked result, rather than block name.
	 */
	void usePickedResult(Block block);

	IElementHelper getElementHelper();

	/**
	 * Client only
	 */
	IDisplayHelper getDisplayHelper();

	WailaConfig getConfig();

	BlockAccessor createBlockAccessor(BlockState blockState, BlockEntity blockEntity, Level level, Player player, CompoundTag serverData, BlockHitResult hit, boolean serverConnected);

	EntityAccessor createEntityAccessor(Entity entity, Level level, Player player, CompoundTag serverData, EntityHitResult hit, boolean serverConnected);

}
