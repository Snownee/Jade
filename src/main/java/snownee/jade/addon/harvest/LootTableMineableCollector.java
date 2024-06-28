package snownee.jade.addon.harvest;

import java.util.List;
import java.util.function.Function;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import snownee.jade.Jade;
import snownee.jade.util.CommonProxy;

public class LootTableMineableCollector {
	private final Registry<LootTable> lootRegistry;
	private final ItemStack toolItem;

	public LootTableMineableCollector(Registry<LootTable> lootRegistry, ItemStack toolItem) {
		this.lootRegistry = lootRegistry;
		this.toolItem = toolItem;
	}

	public static List<Block> execute(Registry<LootTable> lootRegistry, ItemStack toolItem) {
		Stopwatch stopwatch = null;
		if (CommonProxy.isDevEnv()) {
			stopwatch = Stopwatch.createStarted();
		}
		LootTableMineableCollector collector = new LootTableMineableCollector(lootRegistry, toolItem);
		List<Block> list = Lists.newArrayList();
		for (Block block : BuiltInRegistries.BLOCK) {
			if (!ShearsToolHandler.getInstance().test(block.defaultBlockState()).isEmpty()) {
				continue;
			}
			LootTable lootTable = lootRegistry.get(block.getLootTable());
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

	private boolean doLootTable(LootTable lootTable) {
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
			LootTable lootTable = nestedLootTable.contents.map(lootRegistry::get, Function.identity());
			return doLootTable(lootTable);
		} else {
			return CommonProxy.isCorrectConditions(entry.conditions, toolItem);
		}
		return false;
	}
}
