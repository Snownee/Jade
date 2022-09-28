package snownee.jade.addon.forge;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import snownee.jade.api.view.ItemView;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.util.ClientPlatformProxy;
import snownee.jade.util.PlatformProxy;

public enum BlockInventoryProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {

	INSTANCE;

	// A set of tile names that need to be ignored in order to avoid network overload
	// Yay hardcoding, but it's better than nothing for now
	public static final Set<String> INVENTORY_IGNORE = Collections.synchronizedSet(Sets.newHashSet());

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getBlockEntity() == null || accessor.getBlockEntity() instanceof AbstractFurnaceBlockEntity)
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
			ListTag nbtList = accessor.getServerData().getCompound("JadeHandler").getList("Items", Tag.TAG_COMPOUND);

			int drawnCount = 0;
			int realSize = PluginConfig.INSTANCE.getInt(ClientPlatformProxy.isShowDetailsPressed() ? Identifiers.MC_BLOCK_INVENTORY_DETAILED_AMOUNT : Identifiers.MC_BLOCK_INVENTORY_NORMAL_AMOUNT);
			realSize = Math.min(nbtList.size(), realSize);
			boolean showName = realSize < PluginConfig.INSTANCE.getInt(Identifiers.MC_BLOCK_INVENTORY_SHOW_NAME_AMOUNT);
			IElementHelper helper = tooltip.getElementHelper();
			List<IElement> elements = Lists.newArrayList();
			for (int i = 0; i < realSize; i++) {
				CompoundTag itemTag = nbtList.getCompound(i);
				ItemStack stack = ItemStack.of(itemTag);
				if (stack.isEmpty())
					continue;
				if (itemTag.contains("NewCount"))
					stack.setCount(itemTag.getInt("NewCount"));
				if (i > 0 && (showName || drawnCount >= PluginConfig.INSTANCE.getInt(Identifiers.MC_BLOCK_INVENTORY_ITEMS_PER_LINE))) {
					tooltip.add(elements);
					elements.clear();
					drawnCount = 0;
				}

				if (showName) {
					ItemStack copy = stack.copy();
					copy.setCount(1);
					elements.add(Jade.smallItem(helper, copy).clearCachedMessage());
					elements.add(helper.text(Component.literal(Integer.toString(stack.getCount())).append("× ").append(stack.getHoverName())).message(null));
				} else if (itemTag.contains("Text")) {
					elements.add(helper.item(stack, 1, itemTag.getString("Text")));
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
		if (JadeCommonConfig.shouldIgnoreTE(tag.getString("id")) || te instanceof AbstractFurnaceBlockEntity) {
			return;
		}

		if (te instanceof RandomizableContainerBlockEntity && ((RandomizableContainerBlockEntity) te).lootTable != null) {
			tag.putBoolean("Loot", true);
			return;
		}

		if (!JadeCommonConfig.bypassLockedContainer && !player.isCreative() && !player.isSpectator() && te instanceof BaseContainerBlockEntity lockableBlockEntity) {
			if (lockableBlockEntity.lockKey != LockCode.NO_LOCK) {
				tag.putBoolean("Locked", true);
				return;
			}
		}

		putInvData(tag, PlatformProxy.wrapBlockInv(te, player));
	}

	public static void putInvData(CompoundTag tag, List<ItemView> views) {
		if (views == null || views.isEmpty())
			return;
		ListTag nbtTagList = new ListTag();
		for (int i = 0; i < views.size(); i++) {
			ItemView view = views.get(i);
			int count = view.item.getCount();
			CompoundTag itemTag = new CompoundTag();
			if (count > 64)
				view.item.setCount(1);
			view.item.save(itemTag);
			if (count > 64)
				itemTag.putInt("NewCount", count);
			if (view.text != null)
				itemTag.putString("Text", view.text);
			nbtTagList.add(itemTag);
		}
		CompoundTag nbt = new CompoundTag();
		nbt.put("Items", nbtTagList);
		tag.put("JadeHandler", nbt);
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.FORGE_BLOCK_INVENTORY;
	}

}
