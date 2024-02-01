package snownee.jade.overlay;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import snownee.jade.Jade;
import snownee.jade.api.Accessor;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.util.CommonProxy;

public class RayTracing {

	public static final RayTracing INSTANCE = new RayTracing();
	public static Predicate<Entity> ENTITY_FILTER = entity -> true;
	private final Minecraft mc = Minecraft.getInstance();
	private HitResult target = null;

	private RayTracing() {
	}

	public static BlockState wrapBlock(BlockGetter level, BlockHitResult hit, CollisionContext context) {
		if (hit.getType() != HitResult.Type.BLOCK) {
			return Blocks.AIR.defaultBlockState();
		}
		BlockState blockState = level.getBlockState(hit.getBlockPos());
		FluidState fluidState = blockState.getFluidState();
		if (!fluidState.isEmpty()) {
			if (blockState.is(Blocks.BARRIER) && WailaClientRegistration.instance().shouldHide(blockState)) {
				return fluidState.createLegacyBlock();
			}
			if (blockState.getShape(level, hit.getBlockPos(), context).isEmpty()) {
				return fluidState.createLegacyBlock();
			}
		}
		return blockState;
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
			if (axisalignedbb.contains(startVec)) {
				entity = entity1;
				d0 = 0;
				break;
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

	public static boolean isEmptyElement(IElement element) {
		return element == null || element == ItemStackElement.EMPTY;
	}

	public void fire() {
		Entity viewpoint = mc.getCameraEntity();
		if (viewpoint == null)
			return;

		if (mc.hitResult != null && mc.hitResult.getType() == Type.ENTITY) {
			Entity targetEntity = ((EntityHitResult) mc.hitResult).getEntity();
			if (canBeTarget(targetEntity, viewpoint)) {
				target = mc.hitResult;
				return;
			}
		}

		float reach = mc.gameMode.getPickRange() + Jade.CONFIG.get().getGeneral().getReachDistance();
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

		Level world = entity.level();
		AABB bound = new AABB(eyePosition, traceEnd);
		Predicate<Entity> predicate = e -> canBeTarget(e, entity);
		EntityHitResult entityResult = getEntityHitResult(world, entity, eyePosition, traceEnd, bound, predicate);

		if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK) {
			Vec3 lookVector = entity.getViewVector(partialTicks);
			traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);
		}

		BlockState eyeBlock = world.getBlockState(BlockPos.containing(eyePosition));
		ClipContext.Fluid fluidView = ClipContext.Fluid.NONE;
		if (eyeBlock.getFluidState().isEmpty()) {
			fluidView = Jade.CONFIG.get().getGeneral().getDisplayFluids().ctx;
		}
		CollisionContext collisionContext = CollisionContext.of(entity);
		ClipContext context = new ClipContext(eyePosition, traceEnd, ClipContext.Block.OUTLINE, fluidView, collisionContext);

		BlockHitResult blockResult = world.clip(context);
		if (entityResult != null) {
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
		if (blockResult.getType() == Type.BLOCK) {
			BlockState state = wrapBlock(world, blockResult, collisionContext);
			if (WailaClientRegistration.instance().shouldHide(state)) {
				return null;
			}
		}
		return blockResult;
	}

	private boolean canBeTarget(Entity target, Entity viewEntity) {
		if (target.isRemoved())
			return false;
		if (target.isSpectator())
			return false;
		if (target == viewEntity.getVehicle())
			return false;
		if (target instanceof Projectile projectile && projectile.tickCount <= 10 && !target.level().tickRateManager().isEntityFrozen(target))
			return false;
		if (CommonProxy.isMultipartEntity(target) && !target.isPickable())
			return false;
		if (viewEntity instanceof Player player) {
			if (target.isInvisibleTo(player))
				return false;
			if (mc.gameMode.isDestroying() && target.getType() == EntityType.ITEM)
				return false;
		} else {
			if (target.isInvisible())
				return false;
		}
		return !WailaClientRegistration.instance().shouldHide(target) && ENTITY_FILTER.test(target);
	}

	public IElement getIcon() {
		Accessor<?> accessor = ObjectDataCenter.get();
		if (accessor == null)
			return null;

		IElement icon = ObjectDataCenter.getIcon();
		if (isEmptyElement(icon))
			return null;
		else
			return icon;
	}

}
