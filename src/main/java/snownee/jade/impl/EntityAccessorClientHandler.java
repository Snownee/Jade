package snownee.jade.impl;

import java.util.function.Function;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import snownee.jade.api.AccessorClientHandler;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.ui.ElementHelper;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.overlay.RayTracing;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.WailaExceptionHandler;

public class EntityAccessorClientHandler implements AccessorClientHandler<EntityAccessor> {

	@Override
	public boolean shouldDisplay(EntityAccessor accessor) {
		IWailaConfig.General general = IWailaConfig.get().general();
		if (!general.getDisplayEntities()) {
			return false;
		}
		if (!general.getDisplayBosses() && CommonProxy.isBoss(accessor.getEntity())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean shouldRequestData(EntityAccessor accessor) {
		for (IServerDataProvider<EntityAccessor> provider : WailaCommonRegistration.instance()
				.getEntityNBTProviders(accessor.getEntity())) {
			if (provider.shouldRequestData(accessor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void requestData(EntityAccessor accessor) {
		ClientProxy.requestEntityData(accessor);
	}

	@Override
	public IElement getIcon(EntityAccessor accessor) {
		IElement icon = null;
		Entity entity = accessor.getEntity();
		if (entity instanceof ItemEntity) {
			icon = ItemStackElement.of(((ItemEntity) entity).getItem());
		} else {
			ItemStack stack = accessor.getPickedResult();
			if ((!(stack.getItem() instanceof SpawnEggItem) || !(entity instanceof LivingEntity))) {
				icon = ItemStackElement.of(stack);
			}
		}

		for (var provider : WailaClientRegistration.instance().getEntityIconProviders(entity, this::isEnabled)) {
			try {
				IElement element = provider.getIcon(accessor, PluginConfig.INSTANCE, icon);
				if (!RayTracing.isEmptyElement(element)) {
					icon = element;
				}
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider, null);
			}
		}
		return icon;
	}

	@Override
	public void gatherComponents(EntityAccessor accessor, Function<IJadeProvider, ITooltip> tooltipProvider) {
		for (var provider : WailaClientRegistration.instance().getEntityProviders(accessor.getEntity(), this::isEnabled)) {
			ITooltip tooltip = tooltipProvider.apply(provider);
			try {
				ElementHelper.INSTANCE.setCurrentUid(provider.getUid());
				provider.appendTooltip(tooltip, accessor, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider, tooltip::add);
			} finally {
				ElementHelper.INSTANCE.setCurrentUid(null);
			}
		}
	}

}
