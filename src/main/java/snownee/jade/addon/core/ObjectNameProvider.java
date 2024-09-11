package snownee.jade.addon.core;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.mixin.EntityAccess;

public abstract class ObjectNameProvider implements IToggleableProvider {
	private static final MapCodec<Component> GIVEN_NAME_CODEC = ComponentSerialization.CODEC.fieldOf("given_name");

	public static ForBlock getBlock() {
		return ForBlock.INSTANCE;
	}

	public static ForEntity getEntity() {
		return ForEntity.INSTANCE;
	}

	public static Component getEntityName(Entity entity, boolean withType) {
		Component typeName = null;
		normally:
		if (withType || !entity.hasCustomName()) {
			if (WailaClientRegistration.instance().shouldPick(entity)) {
				ItemStack stack = entity.getPickResult();
				if (stack != null && !stack.isEmpty()) {
					typeName = stack.getHoverName();
					break normally;
				}
			}
			typeName = switch (entity) {
				case Player ignored -> {
					withType = false;
					yield entity.getDisplayName();
				}
				case Villager ignored -> {
					withType = false;
					yield entity.getType().getDescription();
				}
				case ItemEntity itemEntity -> {
					withType = false;
					yield itemEntity.getItem().getHoverName();
				}
				case ItemDisplay itemDisplay when !itemDisplay.getSlot(0).get().isEmpty() -> itemDisplay.getSlot(0).get().getHoverName();
				case BlockDisplay blockDisplay when !blockDisplay.getBlockState().isAir() ->
						blockDisplay.getBlockState().getBlock().getName();
				default -> entity.hasCustomName() ? ((EntityAccess) entity).callGetTypeName() : null;
			};
		}
		Component displayName = entity.getName();
		if (typeName != null) {
			if (withType && !typeName.getString().equals(displayName.getString())) {
				return Component.translatable("jade.customNameEntity", displayName, typeName);
			} else {
				return typeName;
			}
		}
		return displayName;
	}

	public static class ForBlock extends ObjectNameProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
		private static final ForBlock INSTANCE = new ForBlock();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			Component name = accessor.readData(GIVEN_NAME_CODEC).orElse(null);
			if (name == null && accessor.isFakeBlock()) {
				name = accessor.getFakeBlock().getHoverName();
			}
			if (name == null && WailaClientRegistration.instance().shouldPick(accessor.getBlockState())) {
				ItemStack pick = accessor.getPickedResult();
				if (pick != null && !pick.isEmpty()) {
					name = pick.getHoverName();
				}
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
			tooltip.add(IThemeHelper.get().title(name));
		}

		@Override
		public void appendServerData(CompoundTag data, BlockAccessor accessor) {
			BlockEntity blockEntity = accessor.getBlockEntity();
			if (blockEntity instanceof Nameable nameable) {
				Component name = null;
				if (blockEntity instanceof ChestBlockEntity && accessor.getBlock() instanceof ChestBlock) {
					MenuProvider menuProvider = accessor.getBlockState().getMenuProvider(accessor.getLevel(), accessor.getPosition());
					if (menuProvider != null) {
						name = menuProvider.getDisplayName();
					}
				} else if (nameable.hasCustomName()) {
					name = nameable.getDisplayName();
				}
				if (name != null) {
					accessor.writeData(GIVEN_NAME_CODEC, name);
				}
			}
		}

		@Override
		public boolean shouldRequestData(BlockAccessor accessor) {
			return accessor.getBlockEntity() instanceof Nameable;
		}
	}

	public static class ForEntity extends ObjectNameProvider implements IEntityComponentProvider {
		private static final ForEntity INSTANCE = new ForEntity();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			Component name = getEntityName(
					accessor.getEntity(),
					IWailaConfig.get().getGeneral().getEnableAccessibilityPlugin() && config.get(JadeIds.ACCESS_ENTITY_DETAILS));
			tooltip.add(IThemeHelper.get().title(name));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.CORE_OBJECT_NAME;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.HEAD - 100;
	}
}
