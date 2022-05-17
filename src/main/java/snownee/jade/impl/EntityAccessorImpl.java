package snownee.jade.impl;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import snownee.jade.Waila;
import snownee.jade.WailaClient;
import snownee.jade.api.AccessorImpl;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.network.RequestEntityPacket;
import snownee.jade.overlay.RayTracing;
import snownee.jade.util.WailaExceptionHandler;

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

		for (IEntityComponentProvider provider : WailaClientRegistration.INSTANCE.getEntityIconProviders(entity)) {
			try {
				IElement element = provider.getIcon(this, PluginConfig.INSTANCE, icon);
				if (!RayTracing.isEmptyElement(element))
					icon = element;
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider.getClass().toString(), null);
			}
		}
		return icon;
	}

	@Override
	public void _gatherComponents(ITooltip tooltip) {
		List<IEntityComponentProvider> providers = WailaClientRegistration.INSTANCE.getEntityProviders(getEntity(), getTooltipPosition());
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
		return WailaClient.CONFIG.get().getGeneral().getDisplayEntities();
	}

	@Override
	public void _requestData(boolean showDetails) {
		Waila.NETWORK.sendToServer(new RequestEntityPacket(entity, showDetails));
	}

	@Override
	public boolean shouldRequestData() {
		return !WailaCommonRegistration.INSTANCE.getEntityNBTProviders(entity).isEmpty();
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
