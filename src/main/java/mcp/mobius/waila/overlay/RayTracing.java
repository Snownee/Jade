package mcp.mobius.waila.overlay;

import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mcp.mobius.waila.impl.WailaRegistrar;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.impl.ui.FluidStackElement;
import mcp.mobius.waila.impl.ui.ItemStackElement;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class RayTracing {

	public static final RayTracing INSTANCE = new RayTracing();
	private RayTraceResult target = null;
	private Minecraft mc = Minecraft.getInstance();

	private RayTracing() {
	}

	public void fire() {
		if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
			this.target = mc.objectMouseOver;
			return;
		}

		Entity viewpoint = mc.getRenderViewEntity();
		if (viewpoint == null)
			return;

		float reach = Waila.CONFIG.get().getGeneral().getReachDistance();
		if (reach == 0) {
			reach = mc.playerController.getBlockReachDistance();
		}
		this.target = this.rayTrace(viewpoint, reach, mc.getRenderPartialTicks());
	}

	public RayTraceResult getTarget() {
		return this.target;
	}

	public RayTraceResult rayTrace(Entity entity, double playerReach, float partialTicks) {
		Vector3d eyePosition = entity.getEyePosition(partialTicks);
		Vector3d traceEnd;
		boolean defaultReach = Waila.CONFIG.get().getGeneral().getReachDistance() == 0;
		if (defaultReach && mc.objectMouseOver != null && mc.objectMouseOver.getType() == Type.BLOCK) {
			traceEnd = mc.objectMouseOver.getHitVec();
		} else {
			Vector3d lookVector = entity.getLook(partialTicks);
			traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);
		}

		World world = entity.getEntityWorld();
		AxisAlignedBB bound = new AxisAlignedBB(eyePosition, traceEnd);
		Entity riding = entity.getRidingEntity();
		Predicate<Entity> predicate = null;
		if (riding != null) {
			predicate = e -> e != riding;
		}
		EntityRayTraceResult entityResult = rayTraceEntities(world, entity, eyePosition, traceEnd, bound, predicate);
		if (defaultReach && entityResult != null) {
			return entityResult;
		}

		if (defaultReach && mc.objectMouseOver != null && mc.objectMouseOver.getType() == Type.BLOCK) {
			Vector3d lookVector = entity.getLook(partialTicks);
			traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);
		}

		RayTraceContext.FluidMode fluidView = Waila.CONFIG.get().getGeneral().getDisplayFluids();
		RayTraceContext context = new RayTraceContext(eyePosition, traceEnd, RayTraceContext.BlockMode.OUTLINE, fluidView, entity);

		BlockRayTraceResult blockResult = world.rayTraceBlocks(context);
		if (entityResult != null && blockResult != null && blockResult.getType() == Type.BLOCK) {
			double entityDist = entityResult.getHitVec().squareDistanceTo(eyePosition);
			double blockDist = blockResult.getHitVec().squareDistanceTo(eyePosition);
			if (entityDist < blockDist) {
				return entityResult;
			}
		}
		return blockResult;
	}

	// from ProjectileHelper
	@Nullable
	public static EntityRayTraceResult rayTraceEntities(World worldIn, Entity projectile, Vector3d startVec, Vector3d endVec, AxisAlignedBB boundingBox, Predicate<Entity> filter) {
		double d0 = Double.MAX_VALUE;
		Entity entity = null;

		for (Entity entity1 : worldIn.getEntitiesInAABBexcluding(projectile, boundingBox, filter)) {
			if (entity1.isSpectator()) {
				continue;
			}
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

	public IElement getIcon() {
		if (this.target == null)
			return null;

		IElement icon = null;
		switch (this.target.getType()) {
		case ENTITY: {
			Entity entity = ((EntityRayTraceResult) target).getEntity();
			if (entity instanceof ItemEntity) {
				icon = ItemStackElement.of(((ItemEntity) entity.getEntity()).getItem());
			} else {
				ItemStack stack = entity.getPickedResult(target);
				if (!(stack.getItem() instanceof SpawnEggItem && entity instanceof LivingEntity))
					icon = ItemStackElement.of(stack);
			}

			for (IEntityComponentProvider provider : WailaRegistrar.INSTANCE.getEntityStackProviders(entity)) {
				IElement element = provider.getIcon((EntityAccessor) ObjectDataCenter.get(), PluginConfig.INSTANCE, icon);
				if (!isEmpty(element))
					icon = element;
			}
			break;
		}
		case BLOCK: {
			World world = mc.world;
			BlockPos pos = ((BlockRayTraceResult) target).getPos();
			BlockState state = world.getBlockState(pos);
			if (state.getBlock().isAir(state, world, pos))
				break;

			ItemStack pick = state.getBlock().getPickBlock(state, target, world, pos, mc.player);
			if (!pick.isEmpty())
				icon = ItemStackElement.of(pick);

			if (isEmpty(icon) && state.getBlock().asItem() != Items.AIR)
				icon = ItemStackElement.of(new ItemStack(state.getBlock()));

			if (isEmpty(icon) && state.getBlock() instanceof FlowingFluidBlock) {
				FlowingFluidBlock block = (FlowingFluidBlock) state.getBlock();
				Fluid fluid = block.getFluid();
				FluidStack fluidStack = new FluidStack(fluid, 1);
				icon = new FluidStackElement(fluidStack);//.size(new Size(18, 18));
			}

			for (IComponentProvider provider : WailaRegistrar.INSTANCE.getBlockStackProviders(state.getBlock())) {
				IElement element = provider.getIcon((BlockAccessor) ObjectDataCenter.get(), PluginConfig.INSTANCE, icon);
				if (!isEmpty(element))
					icon = element;
			}
			break;
		}
		default:
			break;
		}

		if (isEmpty(icon))
			return null;
		else
			return icon;
	}

	private static boolean isEmpty(IElement element) {
		return element == null || element == ItemStackElement.EMPTY;
	}

}
