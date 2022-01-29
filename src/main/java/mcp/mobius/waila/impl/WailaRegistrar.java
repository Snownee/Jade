package mcp.mobius.waila.impl;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.ui.IDisplayHelper;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.impl.config.ConfigEntry;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.impl.ui.ElementHelper;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class WailaRegistrar implements IRegistrar {

	public static final WailaRegistrar INSTANCE = new WailaRegistrar();

	/* CONFIG HANDLING */

	@Override
	public void addConfig(ResourceLocation key, boolean defaultValue) {
		if (FMLEnvironment.dist.isClient()) {
			PluginConfig.INSTANCE.addConfig(new ConfigEntry(key, defaultValue, false));
		}
	}

	@Override
	public void addSyncedConfig(ResourceLocation key, boolean defaultValue) {
		PluginConfig.INSTANCE.addConfig(new ConfigEntry(key, defaultValue, true));
	}

	/* REGISTRATION METHODS */

	@Override
	public void registerIconProvider(IComponentProvider dataProvider, Class<? extends Block> block) {
		WailaClientRegistration.INSTANCE.registerIconProvider(dataProvider, block);
	}

	@Override
	public void registerComponentProvider(IComponentProvider dataProvider, TooltipPosition position, Class<? extends Block> block) {
		WailaClientRegistration.INSTANCE.registerComponentProvider(dataProvider, position, block);
	}

	@Override
	public void registerIconProvider(IEntityComponentProvider dataProvider, Class<? extends Entity> entity) {
		WailaClientRegistration.INSTANCE.registerIconProvider(dataProvider, entity);
	}

	@Override
	public void registerComponentProvider(IEntityComponentProvider dataProvider, TooltipPosition position, Class<? extends Entity> entity) {
		WailaClientRegistration.INSTANCE.registerIconProvider(dataProvider, entity);
	}

	@Override
	public void registerBlockDataProvider(IServerDataProvider<BlockEntity> dataProvider, Class<? extends BlockEntity> block) {
		WailaCommonRegistration.INSTANCE.registerBlockDataProvider(dataProvider, block);
	}

	@Override
	public void registerEntityDataProvider(IServerDataProvider<Entity> dataProvider, Class<? extends Entity> entity) {
		WailaCommonRegistration.INSTANCE.registerEntityDataProvider(dataProvider, entity);
	}

	@Override
	public IElementHelper getElementHelper() {
		return ElementHelper.INSTANCE;
	}

	@Override
	public IDisplayHelper getDisplayHelper() {
		return FMLEnvironment.dist.isClient() ? DisplayHelper.INSTANCE : null;
	}

	@Override
	public WailaConfig getConfig() {
		return Waila.CONFIG.get();
	}

	@Override
	public void hideTarget(Block block) {
		WailaClientRegistration.INSTANCE.hideTarget(block);
	}

	@Override
	public void hideTarget(EntityType<?> entityType) {
		WailaClientRegistration.INSTANCE.hideTarget(entityType);
	}

	@Override
	public void usePickedResult(Block block) {
		WailaClientRegistration.INSTANCE.usePickedResult(block);
	}

	@Override
	public BlockAccessor createBlockAccessor(BlockState blockState, BlockEntity blockEntity, Level level, Player player, CompoundTag serverData, BlockHitResult hit, boolean serverConnected) {
		return WailaClientRegistration.INSTANCE.createBlockAccessor(blockState, blockEntity, level, player, serverData, hit, serverConnected);
	}

	@Override
	public EntityAccessor createEntityAccessor(Entity entity, Level level, Player player, CompoundTag serverData, EntityHitResult hit, boolean serverConnected) {
		return WailaClientRegistration.INSTANCE.createEntityAccessor(entity, level, player, serverData, hit, serverConnected);
	}

}
