package mcp.mobius.waila.impl;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.ui.IDisplayHelper;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.impl.ui.ElementHelper;
import mcp.mobius.waila.overlay.DisplayHelper;
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

public class WailaClientRegistration implements IWailaClientRegistration {

	public static final IWailaClientRegistration INSTANCE = new WailaClientRegistration();

	@Override
	public void registerIconProvider(IComponentProvider dataProvider, Class<? extends Block> block) {
		WailaRegistrar.INSTANCE.registerIconProvider(dataProvider, block);
	}

	@Override
	public void registerComponentProvider(IComponentProvider dataProvider, TooltipPosition position, Class<? extends Block> block) {
		WailaRegistrar.INSTANCE.registerComponentProvider(dataProvider, position, block);
	}

	@Override
	public void registerIconProvider(IEntityComponentProvider dataProvider, Class<? extends Entity> entity) {
		WailaRegistrar.INSTANCE.registerIconProvider(dataProvider, entity);
	}

	@Override
	public void registerComponentProvider(IEntityComponentProvider dataProvider, TooltipPosition position, Class<? extends Entity> entity) {
		WailaRegistrar.INSTANCE.registerComponentProvider(dataProvider, position, entity);
	}

	@Override
	public void hideTarget(Block block) {
		WailaRegistrar.INSTANCE.hideTarget(block);
	}

	@Override
	public void hideTarget(EntityType<?> entityType) {
		WailaRegistrar.INSTANCE.hideTarget(entityType);
	}

	@Override
	public void usePickedResult(Block block) {
		WailaRegistrar.INSTANCE.usePickedResult(block);
	}

	@Override
	public BlockAccessor createBlockAccessor(BlockState blockState, BlockEntity blockEntity, Level level, Player player, CompoundTag serverData, BlockHitResult hit, boolean serverConnected) {
		return WailaRegistrar.INSTANCE.createBlockAccessor(blockState, blockEntity, level, player, serverData, hit, serverConnected);
	}

	@Override
	public EntityAccessor createEntityAccessor(Entity entity, Level level, Player player, CompoundTag serverData, EntityHitResult hit, boolean serverConnected) {
		return WailaRegistrar.INSTANCE.createEntityAccessor(entity, level, player, serverData, hit, serverConnected);
	}

	@Override
	public IElementHelper getElementHelper() {
		return ElementHelper.INSTANCE;
	}

	@Override
	public IDisplayHelper getDisplayHelper() {
		return DisplayHelper.INSTANCE;
	}

	@Override
	public WailaConfig getConfig() {
		return Waila.CONFIG.get();
	}

}
