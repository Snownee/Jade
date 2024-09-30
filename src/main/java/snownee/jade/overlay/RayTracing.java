package snownee.jade.overlay;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Camera;
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
			if (blockState.is(Blocks.BARRIER) && WailaClientRegistration.INSTANCE.shouldHide(blockState)) {
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
	public static EntityHitResult getEntityHitResult(
			Level worldIn,
			Entity projectile,
			Vec3 startVec,
			Vec3 endVec,
			AABB boundingBox,
			Predicate<Entity> filter) {
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
		if (viewpoint == null || mc.gameMode == null) {
			return;
		}

		if (mc.hitResult != null && mc.hitResult.getType() == Type.ENTITY) {
			Entity targetEntity = ((EntityHitResult) mc.hitResult).getEntity();
			if (canBeTarget(targetEntity, viewpoint)) {
				target = mc.hitResult;
				return;
			}
		}

		float reach = mc.gameMode.getPickRange() + Jade.CONFIG.get().getGeneral().getReachDistance();
		target = rayTrace(viewpoint, reach);
	}

	public HitResult getTarget() {
		return target;
	}

	public HitResult rayTrace(Entity entity, double playerReach) {
		Camera camera = mc.gameRenderer.getMainCamera();
		Vec3 eyePosition = entity.getEyePosition();
		Vec3 cameraPosition = camera.getPosition();
		if (!eyePosition.equals(cameraPosition)) {
			playerReach += eyePosition.distanceTo(cameraPosition);
		}
		Vec3 traceEnd;
		// when it comes to a block hit, we only need to find entities that closer than the block
		if (mc.hitResult == null) {
			Vec3 lookVector = new Vec3(camera.getLookVector().mul((float) playerReach));
			traceEnd = cameraPosition.add(lookVector);
		} else if (mc.hitResult.getType() != Type.BLOCK) {
			traceEnd = mc.hitResult.getLocation();
			traceEnd = cameraPosition.add(traceEnd.subtract(cameraPosition).normalize().scale(playerReach * 1.001));
		} else {
			traceEnd = mc.hitResult.getLocation();
			traceEnd = cameraPosition.add(traceEnd.subtract(cameraPosition));
		}

		Level world = entity.level();
		AABB bound = new AABB(cameraPosition, traceEnd);
		Predicate<Entity> predicate = e -> canBeTarget(e, entity);
		EntityHitResult entityResult = getEntityHitResult(world, entity, cameraPosition, traceEnd, bound, predicate);

		// after getting entities, we still need to extend the endpoint in case we want a liquid target
		if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK) {
			traceEnd = mc.hitResult.getLocation();
			traceEnd = cameraPosition.add(traceEnd.subtract(cameraPosition).normalize().scale(playerReach * 1.001));
		}

		BlockState eyeBlock = world.getBlockState(BlockPos.containing(eyePosition));
		ClipContext.Fluid fluidView = ClipContext.Fluid.NONE;
		if (eyeBlock.getFluidState().isEmpty()) {
			fluidView = Jade.CONFIG.get().getGeneral().getDisplayFluids().ctx;
		}
		ClipContext context = new ClipContext(cameraPosition, traceEnd, ClipContext.Block.OUTLINE, fluidView, entity);

		BlockHitResult blockResult = world.clip(context);
		if (entityResult != null) {
			if (blockResult.getType() == Type.BLOCK) {
				double entityDist = entityResult.getLocation().distanceToSqr(cameraPosition);
				double blockDist = blockResult.getLocation().distanceToSqr(cameraPosition);
				if (entityDist < blockDist) {
					return entityResult;
				}
			} else {
				return entityResult;
			}
		}
		if (blockResult.getType() == Type.MISS && mc.hitResult instanceof BlockHitResult hit) {
			// weird, we didn't hit a block in our way. try the vanilla result
			blockResult = hit;
		}
		if (blockResult.getType() == Type.BLOCK) {
			CollisionContext collisionContext = CollisionContext.of(entity);
			BlockState state = wrapBlock(world, blockResult, collisionContext);
			if (WailaClientRegistration.INSTANCE.shouldHide(state)) {
				return null;
			}
		}
		return blockResult;
	}

	private boolean canBeTarget(Entity target, Entity viewEntity) {
		if (target.isRemoved()) {
			return false;
		}
		if (target.isSpectator()) {
			return false;
		}
		if (target == viewEntity.getVehicle()) {
			return false;
		}
		if (target instanceof Projectile projectile && projectile.tickCount <= 10) {
			return false;
		}
		if (CommonProxy.isMultipartEntity(target) && !target.isPickable()) {
			return false;
		}
		if (viewEntity instanceof Player player) {
			if (target.isInvisibleTo(player)) {
				return false;
			}
			if (mc.gameMode != null && mc.gameMode.isDestroying() && target.getType() == EntityType.ITEM) {
				return false;
			}
		} else {
			if (target.isInvisible()) {
				return false;
			}
		}
		return !WailaClientRegistration.INSTANCE.shouldHide(target) && ENTITY_FILTER.test(target);
	}

	public IElement getIcon() {
		Accessor<?> accessor = ObjectDataCenter.get();
		if (accessor == null) {
			return null;
		}

		IElement icon = ObjectDataCenter.getIcon();
		if (isEmptyElement(icon)) {
			return null;
		} else {
			return icon;
		}
	}

}
