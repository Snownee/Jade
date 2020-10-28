package mcp.mobius.waila.overlay;

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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        return target != null && target.getType() == RayTraceResult.Type.BLOCK ? getIdentifierStack() : ItemStack.EMPTY;
    }

    public Entity getTargetEntity() {
        return target.getType() == RayTraceResult.Type.ENTITY ? getIdentifierEntity() : null;
    }

    public RayTraceResult rayTrace(Entity entity, double playerReach, float partialTicks) {
        Vector3d eyePosition = entity.getEyePosition(partialTicks);
        Vector3d lookVector = entity.getLook(partialTicks);
        Vector3d traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);

        RayTraceContext.FluidMode fluidView = Waila.CONFIG.get().getGeneral().shouldDisplayFluids() ? RayTraceContext.FluidMode.SOURCE_ONLY : RayTraceContext.FluidMode.NONE;
        RayTraceContext context = new RayTraceContext(eyePosition, traceEnd, RayTraceContext.BlockMode.OUTLINE, fluidView, entity);
        return entity.getEntityWorld().rayTraceBlocks(context);
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
                if (state.isAir(world, pos))
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

                if (items.isEmpty() && state.getBlock().asItem() != Items.AIR)
                    items.add(new ItemStack(state.getBlock()));

                break;
            }
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
