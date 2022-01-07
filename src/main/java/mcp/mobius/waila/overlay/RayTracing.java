package mcp.mobius.waila.overlay;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.impl.DataAccessor;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.ModList;

public class RayTracing {

	public static final RayTracing INSTANCE = new RayTracing();
	private RayTraceResult target = null;
	private Minecraft mc = Minecraft.getInstance();

	private RayTracing() {
	}

	public void fire() {
		Entity viewpoint = mc.getRenderViewEntity();
		if (viewpoint == null)
			return;

		if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
			Entity targetEntity = ((EntityRayTraceResult) mc.objectMouseOver).getEntity();
			if (canBeTarget(targetEntity, viewpoint)) {
				this.target = mc.objectMouseOver;
				return;
			}
		}

		float reach = mc.playerController.getBlockReachDistance() + Waila.CONFIG.get().getGeneral().getReachDistance();
		this.target = this.rayTrace(viewpoint, reach, mc.getRenderPartialTicks());
	}

	public RayTraceResult getTarget() {
		return this.target;
	}

	public ItemStack getTargetStack() {
		return target != null && target.getType() == RayTraceResult.Type.BLOCK ? getIdentifierStack() : ItemStack.EMPTY;
	}

	public Entity getTargetEntity() {
		return target.getType() == RayTraceResult.Type.ENTITY ? getIdentifierEntity() : null;
	}

	public RayTraceResult rayTrace(Entity entity, double playerReach, float partialTicks) {
		Vector3d eyePosition = entity.getEyePosition(partialTicks);
		Vector3d traceEnd;
		if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == Type.BLOCK) {
			traceEnd = mc.objectMouseOver.getHitVec();
			traceEnd = eyePosition.add(traceEnd.subtract(eyePosition).scale(1.01));
		} else {
			Vector3d lookVector = entity.getLook(partialTicks);
			traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);
		}

		World world = entity.getEntityWorld();
		AxisAlignedBB bound = new AxisAlignedBB(eyePosition, traceEnd);
		Predicate<Entity> predicate = e -> canBeTarget(e, entity);
		EntityRayTraceResult entityResult = rayTraceEntities(world, entity, eyePosition, traceEnd, bound, predicate);

		if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == Type.BLOCK) {
			Vector3d lookVector = entity.getLook(partialTicks);
			traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);
		}

		Block eyeBlock = world.getBlockState(new BlockPos(eyePosition.x, eyePosition.y, eyePosition.z)).getBlock();
		RayTraceContext.FluidMode fluidView = FluidMode.NONE;
		if (!(eyeBlock instanceof FlowingFluidBlock)) {
			fluidView = Waila.CONFIG.get().getGeneral().getDisplayFluids();
		}
		RayTraceContext context = new RayTraceContext(eyePosition, traceEnd, RayTraceContext.BlockMode.OUTLINE, fluidView, entity);

		BlockRayTraceResult blockResult = world.rayTraceBlocks(context);
		if (entityResult != null && blockResult != null && blockResult.getType() == Type.BLOCK) {
			double entityDist = entityResult.getHitVec().squareDistanceTo(eyePosition);
			double blockDist = blockResult.getHitVec().squareDistanceTo(eyePosition);
			if (entityDist > blockDist) {
				return blockResult;
			}
		}
		return MoreObjects.firstNonNull(entityResult, blockResult);
	}

	public ItemStack getIdentifierStack() {
		List<ItemStack> items = this.getIdentifierItems();

		if (items.isEmpty())
			return ItemStack.EMPTY;

		return items.get(0);
	}

	private boolean canBeTarget(Entity target, Entity viewEntity) {
		if (target.isSpectator())
			return false;
		if (target == viewEntity.getRidingEntity())
			return false;
		if (viewEntity instanceof PlayerEntity) {
			if (target.isInvisibleToPlayer((PlayerEntity) viewEntity))
				return false;
		} else {
			if (target.isInvisible())
				return false;
		}
		return true;
	}

	// from ProjectileHelper
	@Nullable
	public static EntityRayTraceResult rayTraceEntities(World worldIn, Entity projectile, Vector3d startVec, Vector3d endVec, AxisAlignedBB boundingBox, Predicate<Entity> filter) {
		double d0 = Double.MAX_VALUE;
		Entity entity = null;

		for (Entity entity1 : worldIn.getEntitiesInAABBexcluding(projectile, boundingBox, filter)) {
			AxisAlignedBB axisalignedbb = entity1.getBoundingBox();
			if (axisalignedbb.getAverageEdgeLength() < 0.3) {
				axisalignedbb = axisalignedbb.grow(0.3);
			}
			Optional<Vector3d> optional = axisalignedbb.rayTrace(startVec, endVec);
			if (optional.isPresent()) {
				double d1 = startVec.squareDistanceTo(optional.get());
				if (d1 < d0) {
					entity = entity1;
					d0 = d1;
				}
			}
		}

		return entity == null ? null : new EntityRayTraceResult(entity);
	}

	public Entity getIdentifierEntity() {
		if (this.target == null || this.target.getType() != RayTraceResult.Type.ENTITY)
			return null;

		List<Entity> entities = Lists.newArrayList();

		Entity entity = ((EntityRayTraceResult) target).getEntity();
		if (WailaRegistrar.INSTANCE.hasOverrideEntityProviders(entity)) {
			Collection<List<IEntityComponentProvider>> overrideProviders = WailaRegistrar.INSTANCE.getOverrideEntityProviders(entity).values();
			for (List<IEntityComponentProvider> providers : overrideProviders)
				for (IEntityComponentProvider provider : providers)
					entities.add(provider.getOverride(DataAccessor.INSTANCE, PluginConfig.INSTANCE));
		}

		return entities.size() > 0 ? entities.get(0) : entity;
	}

	public List<ItemStack> getIdentifierItems() {
		List<ItemStack> items = Lists.newArrayList();

		if (this.target == null)
			return items;

		switch (this.target.getType()) {
		case ENTITY: {
			if (WailaRegistrar.INSTANCE.hasStackEntityProviders(((EntityRayTraceResult) target).getEntity())) {
				Collection<List<IEntityComponentProvider>> providers = WailaRegistrar.INSTANCE.getStackEntityProviders(((EntityRayTraceResult) target).getEntity()).values();
				for (List<IEntityComponentProvider> providersList : providers) {
					for (IEntityComponentProvider provider : providersList) {
						ItemStack providerStack = provider.getDisplayItem(DataAccessor.INSTANCE, PluginConfig.INSTANCE);
						if (providerStack.isEmpty())
							continue;

						items.add(providerStack);
					}
				}
			}
			break;
		}
		case BLOCK: {
			World world = mc.world;
			BlockPos pos = ((BlockRayTraceResult) target).getPos();
			BlockState state = world.getBlockState(pos);
			if (state.getBlock().isAir(state, world, pos))
				return items;

			TileEntity tile = world.getTileEntity(pos);

			if (WailaRegistrar.INSTANCE.hasStackProviders(state.getBlock()))
				handleStackProviders(items, WailaRegistrar.INSTANCE.getStackProviders(state.getBlock()).values());

			if (tile != null && WailaRegistrar.INSTANCE.hasStackProviders(tile))
				handleStackProviders(items, WailaRegistrar.INSTANCE.getStackProviders(tile).values());

			if (!items.isEmpty())
				return items;

			ItemStack pick = state.getBlock().getPickBlock(state, target, world, pos, mc.player);
			if (!pick.isEmpty())
				return Collections.singletonList(pick);

			if (state.getBlock().asItem() != Items.AIR)
				return Collections.singletonList(new ItemStack(state.getBlock()));

			if (state.getBlock() instanceof FlowingFluidBlock) {
				FlowingFluidBlock block = (FlowingFluidBlock) state.getBlock();
				Fluid fluid = block.getFluid();
				return Collections.singletonList(FluidUtil.getFilledBucket(new FluidStack(fluid, 1)));
			}

			break;
		}
		default:
			break;
		}

		return items;
	}

	private void handleStackProviders(List<ItemStack> items, Collection<List<IComponentProvider>> providers) {
		for (List<IComponentProvider> providersList : providers) {
			for (IComponentProvider provider : providersList) {
				ItemStack providerStack = provider.getStack(DataAccessor.INSTANCE, PluginConfig.INSTANCE);
				if (providerStack.isEmpty())
					continue;

				items.add(providerStack);
			}
		}
	}
}
