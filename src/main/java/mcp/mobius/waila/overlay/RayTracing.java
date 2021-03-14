package mcp.mobius.waila.overlay;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IElement;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.Size;
import mcp.mobius.waila.api.impl.DataAccessor;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import mcp.mobius.waila.overlay.element.FluidStackElement;
import mcp.mobius.waila.overlay.element.ItemStackElement;
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

        this.target = this.rayTrace(viewpoint, mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks());
    }

    public RayTraceResult getTarget() {
        return this.target;
    }

    public Entity getTargetEntity() {
        return target.getType() == RayTraceResult.Type.ENTITY ? getIdentifierEntity() : null;
    }

    public RayTraceResult rayTrace(Entity entity, double playerReach, float partialTicks) {
        Vector3d eyePosition = entity.getEyePosition(partialTicks);
        Vector3d traceEnd;
        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == Type.BLOCK) {
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
        EntityRayTraceResult rayTraceResult = rayTraceEntities(world, entity, eyePosition, traceEnd, bound, predicate);
        if (rayTraceResult != null) {
            return rayTraceResult;
        }

        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == Type.BLOCK) {
            Vector3d lookVector = entity.getLook(partialTicks);
            traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);
        }

        RayTraceContext.FluidMode fluidView = Waila.CONFIG.get().getGeneral().getDisplayFluids();
        RayTraceContext context = new RayTraceContext(eyePosition, traceEnd, RayTraceContext.BlockMode.OUTLINE, fluidView, entity);

        return world.rayTraceBlocks(context);
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
        List<IEntityComponentProvider> providers = WailaRegistrar.INSTANCE.getOverrideEntityProviders(entity);
        for (IEntityComponentProvider provider : providers)
            entities.add(provider.getOverride(DataAccessor.INSTANCE, PluginConfig.INSTANCE));

        return entities.size() > 0 ? entities.get(0) : entity;
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
                IElement element = provider.getIcon(DataAccessor.INSTANCE, PluginConfig.INSTANCE);
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
                IElement element = provider.getIcon(DataAccessor.INSTANCE, PluginConfig.INSTANCE, icon);
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
