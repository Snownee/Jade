package snownee.jade.addon.core;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.JadeCommonConfig;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.impl.WailaClientRegistration;

public enum ObjectNameProvider
		implements IBlockComponentProvider, IEntityComponentProvider, IServerDataProvider<BlockEntity> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Component name = null;
		if (accessor.getServerData().contains("givenName", Tag.TAG_STRING)) {
			name = Component.Serializer.fromJson(accessor.getServerData().getString("givenName"));
		}
		if (name == null && accessor.isFakeBlock()) {
			name = accessor.getFakeBlock().getHoverName();
		}
		if (name == null && WailaClientRegistration.INSTANCE.shouldPick(accessor.getBlockState())) {
			ItemStack pick = accessor.getPickedResult();
			if (pick != null && !pick.isEmpty())
				name = pick.getHoverName();
		}
		if (name == null) {
			String key = accessor.getBlock().getDescriptionId();
			if (I18n.exists(key)) {
				name = accessor.getBlock().getName();
			} else {
				ItemStack pick = accessor.getPickedResult();
				if (pick != null && !pick.isEmpty()) {
					name = pick.getHoverName();
				} else {
					name = Component.literal(key);
				}
			}
		}
		if (name != null) {
			IWailaConfig wailaConfig = config.getWailaConfig();
			tooltip.add(wailaConfig.getFormatting().title(name));
		}
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Component name = getEntityName(accessor.getEntity());
		IWailaConfig wailaConfig = config.getWailaConfig();
		tooltip.add(wailaConfig.getFormatting().title(name));
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity t, boolean showDetails) {
		if (t instanceof Nameable && JadeCommonConfig.shouldShowCustomName(t)) {
			Nameable nameable = (Nameable) t;
			if (nameable.hasCustomName()) {
				data.putString("givenName", Component.Serializer.toJson(nameable.getCustomName()));
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.CORE_OBJECT_NAME;
	}

	public static Component getEntityName(Entity entity) {
		if (!entity.hasCustomName()) {
			if (WailaClientRegistration.INSTANCE.shouldPick(entity)) {
				ItemStack stack = entity.getPickResult();
				if (stack != null && !stack.isEmpty()) {
					return stack.getHoverName();
				}
			}
			if (entity instanceof Villager) {
				return entity.getType().getDescription();
			}
			if (entity instanceof ItemEntity) {
				return ((ItemEntity) entity).getItem().getHoverName();
			}
		}
		return entity.getName();
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.HEAD - 1;
	}

}
