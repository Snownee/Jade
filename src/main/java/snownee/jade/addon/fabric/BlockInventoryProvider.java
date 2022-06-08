package snownee.jade.addon.fabric;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import snownee.jade.Jade;
import snownee.jade.JadeCommonConfig;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum BlockInventoryProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {

	INSTANCE;

	// A set of tile names that need to be ignored in order to avoid network overload
	// Yay hardcoding, but it's better than nothing for now
	public static final Set<String> INVENTORY_IGNORE = Collections.synchronizedSet(Sets.newHashSet());

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getBlockEntity() instanceof AbstractFurnaceBlockEntity)
			return;
		append(tooltip, accessor);
	}

	public static void append(ITooltip tooltip, Accessor<?> accessor) {
		if (accessor.getServerData().getBoolean("Loot")) {
			tooltip.add(Component.translatable("jade.not_generated"));
			return;
		}
		if (accessor.getServerData().getBoolean("Locked")) {
			tooltip.add(Component.translatable("jade.locked"));
			return;
		}

		if (accessor.getServerData().contains("JadeHandler")) {
			ListTag list = accessor.getServerData().getList("JadeHandler", Tag.TAG_COMPOUND);
			SimpleContainer container = new SimpleContainer(list.size());
			container.fromTag(list);

			int drawnCount = 0;
			int realSize = container.getContainerSize();
			boolean showName = realSize < 5;
			IElementHelper helper = tooltip.getElementHelper();
			List<IElement> elements = Lists.newArrayList();
			for (int i = 0; i < realSize; i++) {
				ItemStack stack = container.getItem(i);
				if (stack.isEmpty())
					break;
				if (i > 0 && (showName || drawnCount >= JadeCommonConfig.inventoryShowItemPreLine)) {
					tooltip.add(elements);
					elements.clear();
					drawnCount = 0;
				}

				if (showName) {
					ItemStack copy = stack.copy();
					copy.setCount(1);
					elements.add(Jade.smallItem(helper, copy).clearCachedMessage());
					elements.add(helper.text(Component.literal(Integer.toString(stack.getCount())).append("× ").append(stack.getHoverName())).message(null));
				} else {
					elements.add(helper.item(stack));
				}
				drawnCount += 1;
			}

			if (!elements.isEmpty())
				tooltip.add(elements);
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, BlockEntity te, boolean showDetails) {
		if (te == null || JadeCommonConfig.shouldIgnoreTE(tag.getString("id")) || te instanceof AbstractFurnaceBlockEntity) {
			return;
		}

		int size = showDetails ? JadeCommonConfig.inventoryDetailedShowAmount : JadeCommonConfig.inventoryNormalShowAmount;
		if (size == 0) {
			return;
		}

		if (te instanceof RandomizableContainerBlockEntity && ((RandomizableContainerBlockEntity) te).lootTable != null) {
			tag.putBoolean("Loot", true);
			return;
		}
		if (!JadeCommonConfig.bypassLockedContainer && !player.isCreative() && !player.isSpectator() && te instanceof BaseContainerBlockEntity) {
			BaseContainerBlockEntity lockableBlockEntity = (BaseContainerBlockEntity) te;
			if (!lockableBlockEntity.canOpen(player)) {
				tag.putBoolean("Locked", true);
				return;
			}
		}

		Container container = null;
		if (te instanceof Container) {
			container = (Container) te;
		} else if (te instanceof EnderChestBlockEntity) {
			container = player.getEnderChestInventory();
		}
		putInvData(tag, container, size, 0);
	}

	public static void putInvData(CompoundTag tag, Container container, int size, int start) {
		if (container == null || size == 0) {
			return;
		}
		SimpleContainer merged = new SimpleContainer(size);
		boolean empty = true;
		int max = Math.min(container.getContainerSize(), start + size * 3);
		items:
		for (int i = start; i < max; i++) {
			ItemStack stack = container.getItem(i);
			if (stack.hasTag() && stack.getTag().contains("CustomModelData")) {
				for (String key : stack.getTag().getAllKeys()) {
					if (key.toLowerCase(Locale.ENGLISH).endsWith("clear") && stack.getTag().getBoolean(key)) {
						continue items;
					}
				}
			}
			if (!stack.isEmpty()) {
				empty = false;
				merged.addItem(stack.copy());
				if (!merged.getItem(size - 1).isEmpty()) {
					break;
				}
			}
		}
		if (!empty) {
			tag.put("JadeHandler", merged.createTag());
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.FABRIC_BLOCK_INVENTORY;
	}

	@Override
	public int getDefaultPriority() {
		return 2000;
	}

}