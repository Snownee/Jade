package mcp.mobius.waila.overlay;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.impl.DataAccessor;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

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

        this.target = this.rayTrace(viewpoint, mc.playerController.getBlockReachDistance(), 0);
    }

    public RayTraceResult getTarget() {
        return this.target;
    }

    public ItemStack getTargetStack() {
        return target != null && target.getType() != RayTraceResult.Type.MISS ? getIdentifierStack() : ItemStack.EMPTY;
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
        EntityRayTraceResult rayTraceResult = ProjectileHelper.rayTraceEntities(world, entity, eyePosition, traceEnd, bound, predicate);
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

    public ItemStack getIdentifierStack() {
        List<ItemStack> items = this.getIdentifierItems();

        if (items.isEmpty())
            return ItemStack.EMPTY;

        return items.get(0);
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

    public List<ItemStack> getIdentifierItems() {
        List<ItemStack> items = Lists.newArrayList();

        if (this.target == null)
            return items;

        switch (this.target.getType()) {
        case ENTITY: {
            List<IEntityComponentProvider> providers = WailaRegistrar.INSTANCE.getEntityStackProviders(((EntityRayTraceResult) target).getEntity());
            for (IEntityComponentProvider provider : providers) {
                ItemStack providerStack = provider.getDisplayItem(DataAccessor.INSTANCE, PluginConfig.INSTANCE);
                if (providerStack.isEmpty())
                    continue;
                items.add(providerStack);
            }
            break;
        }
        case BLOCK: {
            World world = mc.world;
            BlockPos pos = ((BlockRayTraceResult) target).getPos();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock().isAir(state, world, pos))
                return items;

            handleStackProviders(items, WailaRegistrar.INSTANCE.getBlockStackProviders(state.getBlock()));

            if (!items.isEmpty())
                return items;

            ItemStack pick = state.getBlock().getPickBlock(state, target, world, pos, mc.player);
            if (!pick.isEmpty())
                return Collections.singletonList(pick);

            if (items.isEmpty() && state.getBlock().asItem() != Items.AIR)
                items.add(new ItemStack(state.getBlock()));

            break;
        }
        default:
            break;
        }

        return items;

    }

    private void handleStackProviders(List<ItemStack> items, List<IComponentProvider> providers) {
        for (IComponentProvider provider : providers) {
            ItemStack providerStack = provider.getStack(DataAccessor.INSTANCE, PluginConfig.INSTANCE);
            if (providerStack.isEmpty())
                continue;

            items.add(providerStack);
        }
    }
}
