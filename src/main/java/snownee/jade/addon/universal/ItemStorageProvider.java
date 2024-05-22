package snownee.jade.addon.universal;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.LockCode;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.ScreenDirection;
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
import snownee.jade.util.CommonProxy;
import snownee.jade.util.WailaExceptionHandler;

public abstract class ItemStorageProvider<T extends Accessor<?>> implements IComponentProvider<T>, IServerDataProvider<T> {

	public static final Cache<Object, ItemCollector<?>> targetCache = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(
			60,
			TimeUnit.SECONDS).build();
	public static final Cache<Object, ItemCollector<?>> containerCache = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(
			120,
			TimeUnit.SECONDS).build();

	public static ForBlock getBlock() {
		return ForBlock.INSTANCE;
	}

	public static ForEntity getEntity() {
		return ForEntity.INSTANCE;
	}

	public static class ForBlock extends ItemStorageProvider<BlockAccessor> {
		private static final ForBlock INSTANCE = new ForBlock();
	}

	public static class ForEntity extends ItemStorageProvider<EntityAccessor> {
		private static final ForEntity INSTANCE = new ForEntity();
	}

	public static void append(ITooltip tooltip, Accessor<?> accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("JadeItemStorage")) {
			if (accessor.getServerData().getBoolean("Loot")) {
				tooltip.add(Component.translatable("jade.loot_not_generated"));
			} else if (accessor.getServerData().getBoolean("Locked")) {
				tooltip.add(Component.translatable("jade.locked"));
			}
			return;
		}
		var provider = Optional.ofNullable(ResourceLocation.tryParse(accessor.getServerData().getString("JadeItemStorageUid"))).map(
				WailaClientRegistration.instance().itemStorageProviders::get);
		if (provider.isEmpty()) {
			return;
		}
		var groups = provider.get().getClientGroups(accessor, ViewGroup.readList(accessor.getServerData(), "JadeItemStorage", itemTag -> {
			ItemStack item = ItemStack.parseOptional(accessor.getLevel().registryAccess(), itemTag);
			if (!item.isEmpty() && itemTag.contains("NewCount")) {
				item.setCount(itemTag.getInt("NewCount"));
			}
			return item;
		}));

		if (groups.isEmpty()) {
			return;
		}

		MutableBoolean showName = new MutableBoolean(true);
		{
			int totalSize = 0;
			for (var group : groups) {
				for (var view : group.views) {
					if (view.amountText != null) {
						showName.setFalse();
					}
					if (!view.item.isEmpty()) {
						++totalSize;
					}
				}
			}
			if (showName.isTrue()) {
				showName.setValue(totalSize < PluginConfig.INSTANCE.getInt(Identifiers.UNIVERSAL_ITEM_STORAGE_SHOW_NAME_AMOUNT));
			}
		}

		IElementHelper helper = IElementHelper.get();
		boolean renderGroup = groups.size() > 1 || groups.get(0).shouldRenderGroup();
		ClientViewGroup.tooltip(tooltip, groups, renderGroup, (theTooltip, group) -> {
			if (renderGroup) {
				theTooltip.add(new HorizontalLineElement());
				if (group.title != null) {
					theTooltip.append(helper.text(group.title).scale(0.5F));
					theTooltip.append(new HorizontalLineElement());
				}
			}
			if (group.views.isEmpty()) {
				CompoundTag data = group.extraData;
				if (data != null && data.contains("Collecting", Tag.TAG_ANY_NUMERIC)) {
					float progress = data.getFloat("Collecting");
					if (progress < 1) {
						MutableComponent component = Component.translatable("jade.collectingItems");
						if (progress > 0) {
							component.append(" %s%%".formatted((int) (progress * 100)));
						}
						theTooltip.add(component);
					}
				}
			}
			int drawnCount = 0;
			int realSize = PluginConfig.INSTANCE.getInt(accessor.showDetails() ?
					Identifiers.UNIVERSAL_ITEM_STORAGE_DETAILED_AMOUNT :
					Identifiers.UNIVERSAL_ITEM_STORAGE_NORMAL_AMOUNT);
			realSize = Math.min(group.views.size(), realSize);
			List<IElement> elements = Lists.newArrayList();
			for (int i = 0; i < realSize; i++) {
				ItemView itemView = group.views.get(i);
				ItemStack stack = itemView.item;
				if (stack.isEmpty()) {
					continue;
				}
				if (i > 0 && (
						showName.isTrue() ||
								drawnCount >= PluginConfig.INSTANCE.getInt(Identifiers.UNIVERSAL_ITEM_STORAGE_ITEMS_PER_LINE))) {
					theTooltip.add(elements);
					theTooltip.setLineMargin(-1, ScreenDirection.DOWN, -1);
					elements.clear();
					drawnCount = 0;
				}

				if (showName.isTrue()) {
					if (itemView.description != null) {
						elements.add(helper.smallItem(stack));
						elements.addAll(itemView.description);
					} else {
						elements.add(helper.smallItem(stack).clearCachedMessage());
						elements.add(helper.text(Component.literal(IDisplayHelper.get()
										.humanReadableNumber(stack.getCount(), "", false, null))
								.append("× ")
								.append(IDisplayHelper.get().stripColor(stack.getHoverName()))).message(null));
					}
				} else if (itemView.amountText != null) {
					elements.add(helper.item(stack, 1, itemView.amountText));
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

	public static void putData(Accessor<?> accessor) {
		CompoundTag tag = accessor.getServerData();
		Object target = accessor.getTarget();
		Player player = accessor.getPlayer();
		for (var provider : WailaCommonRegistration.instance().itemStorageProviders.get(accessor)) {
			var groups = provider.getGroups(accessor);
			if (groups == null) {
				continue;
			}
			if (ViewGroup.saveList(tag, "JadeItemStorage", groups, item -> {
				int count = item.getCount();
				if (count > item.getMaxStackSize()) {
					item.setCount(1);
				}
				CompoundTag itemTag = (CompoundTag) item.save(accessor.getLevel().registryAccess());
				if (count > item.getMaxStackSize()) {
					itemTag.putInt("NewCount", count);
					item.setCount(count);
				}
				return itemTag;
			})) {
				tag.putString("JadeItemStorageUid", provider.getUid().toString());
			} else if (target instanceof RandomizableContainer containerEntity && containerEntity.getLootTable() != null) {
				tag.putBoolean("Loot", true);
			} else if (!player.isCreative() && !player.isSpectator() && target instanceof BaseContainerBlockEntity te) {
				if (te.lockKey != LockCode.NO_LOCK) {
					tag.putBoolean("Locked", true);
				}
			}
			break;
		}
	}

	@Override
	public void appendTooltip(ITooltip tooltip, T accessor, IPluginConfig config) {
		if (accessor.getTarget() instanceof AbstractFurnaceBlockEntity) {
			return;
		}
		append(tooltip, accessor, config);
	}

	@Override
	public void appendServerData(CompoundTag tag, T accessor) {
		if (accessor.getTarget() instanceof AbstractFurnaceBlockEntity) {
			return;
		}
		putData(accessor);
	}

	@Override
	public boolean shouldRequestData(T accessor) {
		if (accessor.getTarget() instanceof AbstractFurnaceBlockEntity) {
			return false;
		}
		int amount;
		if (accessor.showDetails()) {
			amount = IWailaConfig.get().getPlugin().getInt(Identifiers.UNIVERSAL_ITEM_STORAGE_DETAILED_AMOUNT);
		} else {
			amount = IWailaConfig.get().getPlugin().getInt(Identifiers.UNIVERSAL_ITEM_STORAGE_NORMAL_AMOUNT);
		}
		if (amount == 0) {
			return false;
		}
		for (var provider : WailaCommonRegistration.instance().itemStorageProviders.get(accessor)) {
			if (provider.shouldRequestData(accessor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.UNIVERSAL_ITEM_STORAGE;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.BODY + 1000;
	}

	public enum Extension implements IServerExtensionProvider<ItemStack>, IClientExtensionProvider<ItemStack, ItemView> {
		INSTANCE;

		@Override
		public ResourceLocation getUid() {
			return Identifiers.UNIVERSAL_ITEM_STORAGE;
		}

		@Nullable
		@Override
		public List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
			Object target = accessor.getTarget();
			if (target == null && accessor instanceof BlockAccessor blockAccessor &&
					blockAccessor.getBlock() instanceof WorldlyContainerHolder holder) {
				WorldlyContainer container = holder.getContainer(
						blockAccessor.getBlockState(),
						accessor.getLevel(),
						blockAccessor.getPosition());
				return CommonProxy.containerGroup(container, accessor);
			}
			if (target == null) {
				return List.of();
			}
			if (target instanceof RandomizableContainer te && te.getLootTable() != null) {
				return List.of();
			}
			if (target instanceof ContainerEntity containerEntity && containerEntity.getLootTable() != null) {
				return List.of();
			}
			Player player = accessor.getPlayer();
			if (!player.isCreative() && !player.isSpectator() && target instanceof BaseContainerBlockEntity te) {
				if (te.lockKey != LockCode.NO_LOCK) {
					return List.of();
				}
			}
			if (target instanceof EnderChestBlockEntity) {
				PlayerEnderChestContainer inventory = player.getEnderChestInventory();
				return new ItemCollector<>(new ItemIterator.ContainerItemIterator(0)).update(inventory, accessor.getLevel().getGameTime());
			}
			ItemCollector<?> itemCollector;
			try {
				itemCollector = targetCache.get(target, () -> CommonProxy.createItemCollector(target, containerCache));
			} catch (ExecutionException e) {
				WailaExceptionHandler.handleErr(e, null, null, null);
				return null;
			}
			if (itemCollector == ItemCollector.EMPTY) {
				return null;
			}
			return itemCollector.update(target, accessor.getLevel().getGameTime());
		}

		@Override
		public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> groups) {
			return ClientViewGroup.map(groups, ItemView::new, null);
		}

		@Override
		public boolean shouldRequestData(Accessor<?> accessor) {
			return CommonProxy.hasDefaultItemStorage(accessor);
		}

		@Override
		public int getDefaultPriority() {
			return TooltipPosition.TAIL;
		}
	}

}
