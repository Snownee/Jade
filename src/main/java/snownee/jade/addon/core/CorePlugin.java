package snownee.jade.addon.core;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.impl.BlockAccessorClientHandler;
import snownee.jade.impl.EntityAccessorClientHandler;
import snownee.jade.impl.WailaClientRegistration;

@WailaPlugin
public class CorePlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ObjectNameProvider.Server.INSTANCE, BlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerAccessorHandler(BlockAccessor.class, new BlockAccessorClientHandler());
		registration.registerAccessorHandler(EntityAccessor.class, new EntityAccessorClientHandler());

		registration.addConfig(JadeIds.CORE_DISTANCE, false);
		registration.addConfig(JadeIds.CORE_COORDINATES, false);
		registration.addConfig(JadeIds.CORE_REL_COORDINATES, false);
		registration.addConfig(JadeIds.CORE_MOD_NAME, ModNameProvider.Mode.ON);

		registration.registerBlockComponent(ObjectNameProvider.ForBlock.INSTANCE, Block.class);
		registration.registerBlockComponent(ModNameProvider.ForBlock.INSTANCE, Block.class);
		registration.registerBlockComponent(DistanceProvider.ForBlock.INSTANCE, Block.class);
		registration.registerBlockComponent(BlockFaceProvider.INSTANCE, Block.class);

		registration.registerEntityComponent(ObjectNameProvider.ForEntity.INSTANCE, Entity.class);
		registration.registerEntityComponent(ModNameProvider.ForEntity.INSTANCE, Entity.class);
		registration.registerEntityComponent(DistanceProvider.ForEntity.INSTANCE, Entity.class);

		registration.markAsClientFeature(JadeIds.CORE_OBJECT_NAME);
		registration.markAsClientFeature(JadeIds.CORE_DISTANCE);
		registration.markAsClientFeature(JadeIds.CORE_COORDINATES);
		registration.markAsClientFeature(JadeIds.CORE_REL_COORDINATES);
		registration.markAsClientFeature(JadeIds.CORE_MOD_NAME);
		registration.markAsClientFeature(JadeIds.CORE_BLOCK_FACE);

		registration.addRayTraceCallback(-10000, this::hideBlocks);
	}

	private @Nullable Accessor<?> hideBlocks(HitResult hit, @Nullable Accessor<?> accessor, @Nullable Accessor<?> original) {
		if (accessor instanceof BlockAccessor blockAccessor && !blockAccessor.isFakeBlock() &&
				WailaClientRegistration.instance().shouldHide(blockAccessor.getBlockState())) {
			BlockState blockState = blockAccessor.getBlockState();
			FluidState fluidState = blockState.getFluidState();
			if (!fluidState.isEmpty()) {
				if (blockState.getShape(accessor.getLevel(), blockAccessor.getPosition(), CollisionContext.of(accessor.getPlayer()))
						.isEmpty() || blockState.is(Blocks.BARRIER) && WailaClientRegistration.instance().shouldHide(blockState)) {
					return WailaClientRegistration.instance()
							.blockAccessor()
							.from(blockAccessor)
							.blockState(fluidState.createLegacyBlock())
							.build();
				}
			}
			return null;
		}
		return accessor;
	}
}
