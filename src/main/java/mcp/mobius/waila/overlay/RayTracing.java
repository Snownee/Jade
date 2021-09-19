package mcp.mobius.waila.overlay;

import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.Accessor;
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
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public class RayTracing {

	public static final RayTracing INSTANCE = new RayTracing();
	private HitResult target = null;
	private Minecraft mc = Minecraft.getInstance();

	private RayTracing() {
	}

	public void fire() {
		Entity viewpoint = mc.getCameraEntity();
		if (viewpoint == null)
			return;

		if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
			Entity targetEntity = ((EntityHitResult) mc.hitResult).getEntity();
			if (canBeTarget(targetEntity, viewpoint)) {
				target = mc.hitResult;
				return;
			}
		}

		float reach = mc.gameMode.getPickRange() + Waila.CONFIG.get().getGeneral().getReachDistance();
		target = rayTrace(viewpoint, reach, mc.getFrameTime());
	}

	public HitResult getTarget() {
		return target;
	}

	public HitResult rayTrace(Entity entity, double playerReach, float partialTicks) {
		Vec3 eyePosition = entity.getEyePosition(partialTicks);
		Vec3 traceEnd;
		if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK) {
			traceEnd = mc.hitResult.getLocation();
			traceEnd = eyePosition.add(traceEnd.subtract(eyePosition).scale(1.01));
		} else {
			Vec3 lookVector = entity.getViewVector(partialTicks);
			traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);
		}

		Level world = entity.level;
		AABB bound = new AABB(eyePosition, traceEnd);
		Predicate<Entity> predicate = e -> canBeTarget(e, entity);
		EntityHitResult entityResult = getEntityHitResult(world, entity, eyePosition, traceEnd, bound, predicate);

		if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK) {
			Vec3 lookVector = entity.getViewVector(partialTicks);
			traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);
		}

		ClipContext.Fluid fluidView = Waila.CONFIG.get().getGeneral().getDisplayFluids();
		ClipContext context = new ClipContext(eyePosition, traceEnd, ClipContext.Block.OUTLINE, fluidView, entity);

		BlockHitResult blockResult = world.clip(context);
		if (entityResult != null && blockResult != null) {
			if (blockResult.getType() == Type.BLOCK) {
				double entityDist = entityResult.getLocation().distanceToSqr(eyePosition);
				double blockDist = blockResult.getLocation().distanceToSqr(eyePosition);
				if (entityDist < blockDist) {
					return entityResult;
				}
			} else {
				return entityResult;
			}
		}
		if (blockResult != null) {
			BlockState state = world.getBlockState(blockResult.getBlockPos());
			if (WailaRegistrar.INSTANCE.shouldHide(state)) {
				return null;
			}
		}
		return blockResult;
	}

	private boolean canBeTarget(Entity target, Entity viewEntity) {
		if (target.isSpectator())
			return false;
		if (target == viewEntity.getVehicle())
			return false;
		if (viewEntity instanceof Player) {
			if (target.isInvisibleTo((Player) viewEntity))
				return false;
		} else {
			if (target.isInvisible())
				return false;
		}
		return !WailaRegistrar.INSTANCE.shouldHide(target);
	}

	// from ProjectileUtil
	@Nullable
	public static EntityHitResult getEntityHitResult(Level worldIn, Entity projectile, Vec3 startVec, Vec3 endVec, AABB boundingBox, Predicate<Entity> filter) {
		double d0 = Double.MAX_VALUE;
		Entity entity = null;

		for (Entity entity1 : worldIn.getEntities(projectile, boundingBox, filter)) {
			AABB axisalignedbb = entity1.getBoundingBox();
			if (axisalignedbb.getSize() < 0.3) {
				axisalignedbb = axisalignedbb.inflate(0.3);
			}
			Optional<Vec3> optional = axisalignedbb.clip(startVec, endVec);
			if (optional.isPresent()) {
				double d1 = startVec.distanceToSqr(optional.get());
				if (d1 < d0) {
					entity = entity1;
					d0 = d1;
				}
			}
		}

		return entity == null ? null : new EntityHitResult(entity);
	}

	public IElement getIcon() {
		Accessor accessor = ObjectDataCenter.get();
		if (accessor == null)
			return null;

		//TODO 1.18: Accessor Factory
		IElement icon = null;
		if (accessor instanceof EntityAccessor) {
			Entity entity = ((EntityAccessor) accessor).getEntity();
			if (entity instanceof ItemEntity) {
				icon = ItemStackElement.of(((ItemEntity) entity).getItem());
			} else {
				ItemStack stack = entity.getPickedResult(accessor.getHitResult());
				if ((!(stack.getItem() instanceof SpawnEggItem) || !(entity instanceof LivingEntity)))
					icon = ItemStackElement.of(stack);
			}

			for (IEntityComponentProvider provider : WailaRegistrar.INSTANCE.getEntityIconProviders(entity)) {
				IElement element = provider.getIcon((EntityAccessor) accessor, PluginConfig.INSTANCE, icon);
				if (!isEmpty(element))
					icon = element;
			}
		} else if (accessor instanceof BlockAccessor) {
			BlockAccessor blockAccessor = (BlockAccessor) accessor;
			Level world = blockAccessor.getLevel();
			BlockPos pos = blockAccessor.getHitResult().getBlockPos();
			BlockState state = blockAccessor.getBlockState();
			if (state.isAir())
				return null;

			ItemStack pick = state.getBlock().getPickBlock(state, target, world, pos, mc.player);
			if (!pick.isEmpty())
				icon = ItemStackElement.of(pick);

			if (isEmpty(icon) && state.getBlock().asItem() != Items.AIR)
				icon = ItemStackElement.of(new ItemStack(state.getBlock()));

			if (isEmpty(icon) && state.getBlock() instanceof LiquidBlock) {
				LiquidBlock block = (LiquidBlock) state.getBlock();
				Fluid fluid = block.getFluid();
				FluidStack fluidStack = new FluidStack(fluid, 1);
				icon = new FluidStackElement(fluidStack);//.size(new Size(18, 18));
			}

			for (IComponentProvider provider : WailaRegistrar.INSTANCE.getBlockIconProviders(state.getBlock())) {
				IElement element = provider.getIcon(blockAccessor, PluginConfig.INSTANCE, icon);
				if (!isEmpty(element))
					icon = element;
			}
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
