package snownee.jade.addon.universal;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import snownee.jade.JadeCommonConfig;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.ui.HorizontalLineElement;
import snownee.jade.impl.ui.ScaledTextElement;
import snownee.jade.util.PlatformProxy;

public enum ItemStorageProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity>,
		IServerExtensionProvider<Object, ItemStack>, IClientExtensionProvider<ItemStack, ItemView> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getBlockEntity() instanceof AbstractFurnaceBlockEntity)
			return;
		append(tooltip, accessor, config);
	}

	public static void append(ITooltip tooltip, Accessor<?> accessor, IPluginConfig config) {
		if (accessor.getServerData().contains("JadeItemStorage")) {
			var provider = Optional.ofNullable(ResourceLocation.tryParse(accessor.getServerData().getString("JadeItemStorageUid"))).map(WailaClientRegistration.INSTANCE.itemStorageProviders::get);
			if (provider.isPresent()) {
				var groups = provider.get().getClientGroups(accessor, ViewGroup.readList(accessor.getServerData(), "JadeItemStorage", itemTag -> {
					ItemStack item = ItemStack.of(itemTag);
					if (!item.isEmpty() && itemTag.contains("NewCount"))
						item.setCount(itemTag.getInt("NewCount"));
					return item;
				}));

				if (groups.isEmpty()) {
					return;
				}

				MutableBoolean showName = new MutableBoolean(true);
				{
					int totalSize = 0;
					for (var group : groups) {
						if (group.views.size() == 1 && "10k+".equals(group.views.get(0).text)) {
							++totalSize;
							continue;
						}
						for (var view : group.views) {
							if (view.text != null) {
								showName.setFalse();
							}
							if (!view.item.isEmpty()) {
								++totalSize;
							}
						}
					}
					if (showName.isTrue())
						showName.setValue(totalSize < PluginConfig.INSTANCE.getInt(Identifiers.MC_BLOCK_INVENTORY_SHOW_NAME_AMOUNT));
				}

				IElementHelper helper = IElementHelper.get();
				boolean renderGroup = groups.size() > 1 || groups.get(0).shouldRenderGroup();
				ClientViewGroup.tooltip(tooltip, groups, renderGroup, (theTooltip, group) -> {
					if (renderGroup) {
						theTooltip.add(new HorizontalLineElement());
						if (group.title != null) {
							theTooltip.append(new ScaledTextElement(group.title, 0.5F));
							theTooltip.append(new HorizontalLineElement());
						}
					}
					int drawnCount = 0;
					int realSize = PluginConfig.INSTANCE.getInt(accessor.showDetails() ? Identifiers.MC_BLOCK_INVENTORY_DETAILED_AMOUNT : Identifiers.MC_BLOCK_INVENTORY_NORMAL_AMOUNT);
					realSize = Math.min(group.views.size(), realSize);
					List<IElement> elements = Lists.newArrayList();
					for (int i = 0; i < realSize; i++) {
						ItemView itemView = group.views.get(i);
						ItemStack stack = itemView.item;
						if (stack.isEmpty())
							continue;
						if (i > 0 && (showName.isTrue() || drawnCount >= PluginConfig.INSTANCE.getInt(Identifiers.MC_BLOCK_INVENTORY_ITEMS_PER_LINE))) {
							theTooltip.add(elements);
							elements.clear();
							drawnCount = 0;
						}

						if (showName.isTrue()) {
							ItemStack copy = stack.copy();
							copy.setCount(1);
							elements.add(helper.smallItem(copy).clearCachedMessage());
							elements.add(helper.text(Component.literal(itemView.text != null ? itemView.text : IDisplayHelper.get().humanReadableNumber(stack.getCount(), "", false)).append("× ").append(IDisplayHelper.get().stripColor(stack.getHoverName()))).message(null));
						} else if (itemView.text != null) {
							elements.add(helper.item(stack, 1, itemView.text));
						} else {
							elements.add(helper.item(stack));
						}
						drawnCount += 1;
					}

					if (!elements.isEmpty()) {
						theTooltip.add(elements);
					}
				});
			}
		} else if (accessor.getServerData().getBoolean("Loot")) {
			tooltip.add(Component.translatable("jade.not_generated"));
		} else if (accessor.getServerData().getBoolean("Locked")) {
			tooltip.add(Component.translatable("jade.locked"));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, BlockEntity te, boolean showDetails) {
		if (JadeCommonConfig.shouldIgnoreTE(tag.getString("id")) || te instanceof AbstractFurnaceBlockEntity) {
			return;
		}
		putData(tag, player, te, showDetails);
	}

	public static void putData(CompoundTag tag, ServerPlayer player, Object target, boolean showDetails) {
		var list = WailaCommonRegistration.INSTANCE.itemStorageProviders.get(target);
		for (var provider : list) {
			var groups = provider.getGroups(player, player.getLevel(), target, showDetails);
			if (groups != null) {
				if (ViewGroup.saveList(tag, "JadeItemStorage", groups, item -> {
					CompoundTag itemTag = new CompoundTag();
					int count = item.getCount();
					if (count > 64)
						item.setCount(1);
					item.save(itemTag);
					if (count > 64)
						itemTag.putInt("NewCount", count);
					return itemTag;
				})) {
					tag.putString("JadeItemStorageUid", provider.getUid().toString());
				} else {
					if (target instanceof RandomizableContainerBlockEntity te && te.lootTable != null) {
						tag.putBoolean("Loot", true);
						return;
					}
					if (target instanceof ContainerEntity containerEntity && containerEntity.getLootTable() != null) {
						tag.putBoolean("Loot", true);
						return;
					}
					if (!JadeCommonConfig.bypassLockedContainer && !player.isCreative() && !player.isSpectator() && target instanceof BaseContainerBlockEntity te) {
						if (te.lockKey != LockCode.NO_LOCK) {
							tag.putBoolean("Locked", true);
							return;
						}
					}
				}
				return;
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.UNIVERSAL_ITEM_STORAGE;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.BODY + 1000;
	}

	@Override
	public List<ViewGroup<ItemStack>> getGroups(ServerPlayer player, ServerLevel world, Object target, boolean showDetails) {
		if (target instanceof RandomizableContainerBlockEntity te && te.lootTable != null) {
			return List.of();
		}
		if (target instanceof ContainerEntity containerEntity && containerEntity.getLootTable() != null) {
			return List.of();
		}
		if (!JadeCommonConfig.bypassLockedContainer && !player.isCreative() && !player.isSpectator() && target instanceof BaseContainerBlockEntity te) {
			if (te.lockKey != LockCode.NO_LOCK) {
				return List.of();
			}
		}

		return PlatformProxy.wrapItemStorage(target, player);
	}

	@Override
	public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> groups) {
		var clientGroups = ClientViewGroup.map(groups, ItemView::new, null);
		for (var clientGroup : clientGroups) {
			var views = clientGroup.views;
			if (views.size() == 1 && views.get(0).item.getCount() > 1000) {
				views.get(0).text = "10k+";
			}
		}
		return clientGroups;
	}

}
