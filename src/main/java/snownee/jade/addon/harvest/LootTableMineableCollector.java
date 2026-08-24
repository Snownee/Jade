package snownee.jade.addon.harvest;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import snownee.jade.Jade;
import snownee.jade.util.CommonProxy;

public class LootTableMineableCollector {
	private static List<Block> shearableBlocks = List.of();
	private final HolderLookup.RegistryLookup<LootTable> lootRegistry;
	private final ItemStack toolItem;

	public LootTableMineableCollector(HolderLookup.RegistryLookup<LootTable> lootRegistry, ItemStack toolItem) {
		this.lootRegistry = lootRegistry;
		this.toolItem = toolItem;
	}

	public static List<Block> execute(HolderLookup.RegistryLookup<LootTable> lootRegistry, ItemStack toolItem) {
		Stopwatch stopwatch = null;
		if (CommonProxy.isDevEnv()) {
			stopwatch = Stopwatch.createStarted();
		}
		LootTableMineableCollector collector = new LootTableMineableCollector(lootRegistry, toolItem);
		List<Block> list = Lists.newArrayList();
		for (Block block : BuiltInRegistries.BLOCK) {
			if (block.getLootTable().isEmpty()) {
				continue;
			}
			if (!ShearsToolHandler.getInstance().test(block.defaultBlockState()).isEmpty()) {
				continue;
			}
			LootTable lootTable = lootRegistry.get(block.getLootTable().get()).map(Holder::value).orElse(null);
			if (collector.doLootTable(lootTable)) {
				list.add(block);
//				Jade.LOGGER.info("block: {}", BuiltInRegistries.BLOCK.getKey(block));
			}
		}
		if (stopwatch != null) {
			Jade.LOGGER.info("LootTableMineableCollector took {}", stopwatch.stop());
		}
		return list;
	}

	private boolean doLootTable(@Nullable LootTable lootTable) {
		if (lootTable == null || lootTable == LootTable.EMPTY) {
			return false;
		}
		for (LootPool pool : lootTable.pools) {
			if (doLootPool(pool)) {
				return true;
			}
		}
		return false;
	}

	private boolean doLootPool(LootPool lootPool) {
		if (lootPool.entries.isEmpty()) {
			return false;
		}
		if (lootPool.condition.isPresent() && CommonProxy.isCorrectConditions(lootPool.condition.get().value(), toolItem)) {
			return true;
		}
		for (LootPoolEntryContainer entry : lootPool.entries) {
			if (doLootPoolEntry(entry)) {
				return true;
			}
		}
		return false;
	}

	private boolean doLootPoolEntry(LootPoolEntryContainer entry) {
		if (entry instanceof AlternativesEntry alternativesEntry) {
			for (LootPoolEntryContainer child : alternativesEntry.children) {
				if (doLootPoolEntry(child)) {
					return true;
				}
			}
		} else if (entry instanceof NestedLootTable nestedLootTable) {
			Optional<LootTable> lootTable = nestedLootTable.contents.map(
					$ -> lootRegistry.get($).map(Holder::value),
					Optional::of);
			return doLootTable(lootTable.orElse(null));
		} else {
			return CommonProxy.isCorrectConditions(entry.conditions, toolItem);
		}
		return false;
	}

	public static void onTagsUpdated(HolderLookup.Provider lookupProvider, boolean client) {
		//TODO execute on a thread?
		try {
			shearableBlocks = LootTableMineableCollector.execute(
					lookupProvider.lookupOrThrow(Registries.LOOT_TABLE),
					Items.SHEARS.getDefaultInstance());
		} catch (Throwable e) {
			Jade.LOGGER.error("Failed to collect shearable blocks", e);
		}
	}

	public static List<Block> getShearableBlocks() {
		return shearableBlocks;
	}
}
