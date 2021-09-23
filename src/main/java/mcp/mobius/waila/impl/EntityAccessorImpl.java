package mcp.mobius.waila.impl;

import java.util.List;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.AccessorImpl;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.impl.ui.ItemStackElement;
import mcp.mobius.waila.network.RequestEntityPacket;
import mcp.mobius.waila.overlay.RayTracing;
import mcp.mobius.waila.utils.WailaExceptionHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Class to get information of entity target and context.
 */
public class EntityAccessorImpl extends AccessorImpl<EntityHitResult> implements EntityAccessor {

	private final Entity entity;

	public EntityAccessorImpl(Entity entity, Level level, Player player, CompoundTag serverData, EntityHitResult hit, boolean serverConnected) {
		super(level, player, serverData, hit, serverConnected);
		this.entity = entity;
	}

	@Override
	public Entity getEntity() {
		return entity;
	}

	@Override
	public ItemStack getPickedResult() {
		return getEntity().getPickedResult(getHitResult());
	}

	@Override
	public IElement _getIcon() {
		IElement icon = null;
		if (entity instanceof ItemEntity) {
			icon = ItemStackElement.of(((ItemEntity) entity).getItem());
		} else {
			ItemStack stack = getPickedResult();
			if ((!(stack.getItem() instanceof SpawnEggItem) || !(entity instanceof LivingEntity)))
				icon = ItemStackElement.of(stack);
		}

		for (IEntityComponentProvider provider : WailaRegistrar.INSTANCE.getEntityIconProviders(entity)) {
			IElement element = provider.getIcon(this, PluginConfig.INSTANCE, icon);
			if (!RayTracing.isEmptyElement(element))
				icon = element;
		}
		return icon;
	}

	@Override
	public void _gatherComponents(ITooltip tooltip) {
		List<IEntityComponentProvider> providers = WailaRegistrar.INSTANCE.getEntityProviders(getEntity(), getTooltipPosition());
		for (IEntityComponentProvider provider : providers) {
			try {
				provider.appendTooltip(tooltip, this, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider.getClass().toString(), tooltip);
			}
		}
	}

	@Override
	public boolean shouldDisplay() {
		return Waila.CONFIG.get().getGeneral().getDisplayEntities();
	}

	@Override
	public void _requestData(boolean showDetails) {
		Waila.NETWORK.sendToServer(new RequestEntityPacket(entity, showDetails));
	}

	@Override
	public boolean shouldRequestData() {
		return !WailaRegistrar.INSTANCE.getEntityNBTProviders(entity).isEmpty();
	}

	@Override
	public boolean _verifyData(CompoundTag serverData) {
		if (!serverData.contains("WailaEntityID"))
			return false;
		return serverData.getInt("WailaEntityID") == entity.getId();
	}

	@Override
	public Object _getTrackObject() {
		return getEntity();
	}

}
