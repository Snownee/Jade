package snownee.jade;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.util.CommonProxy;

public final class JadeCommonConfig {

	private static final Set<String> inventoryBlacklist = Sets.newHashSet();
	public static boolean bypassLockedContainer = false;
	private static boolean onlyShowVanilla = false;
	private static final Set<String> modBlacklist = Sets.newHashSet();
	//
	//	private static IntValue inventorySneakShowAmountVal;
	//	private static IntValue inventoryNormalShowAmountVal;
	//	private static IntValue inventoryShowItemPreLineVal;
	//	private static ConfigValue<List<? extends String>> inventoryBlacklistVal;
	//	private static BooleanValue bypassLockedContainerVal;
	//	private static BooleanValue onlyShowVanillaVal;
	//	private static ConfigValue<List<? extends String>> modBlacklistVal;
	//
	//	static final ForgeConfigSpec spec = new ForgeConfigSpec.Builder().configure(JadeCommonConfig::new).getRight();
	//
	//	private JadeCommonConfig(ForgeConfigSpec.Builder builder) {
	//		builder.push("inventory");
	//		inventorySneakShowAmountVal = builder.defineInRange("sneakShowAmount", inventoryDetailedShowAmount, 0, 54);
	//		inventoryNormalShowAmountVal = builder.defineInRange("normalShowAmount", inventoryNormalShowAmount, 0, 54);
	//		inventoryShowItemPreLineVal = builder.defineInRange("showItemPreLine", inventoryShowItemPreLine, 1, 18);
	//		inventoryBlacklistVal = builder.defineList("blacklist", () -> Collections.singletonList("refinedstorage:disk_drive"), Predicates.alwaysTrue());
	//		bypassLockedContainerVal = builder.define("bypassLockedContainer", bypassLockedContainer);
	//		builder.pop();
	//		builder.push("customContainerName");
	//		onlyShowVanillaVal = builder.define("onlyShowVanilla", onlyShowVanilla);
	//		modBlacklistVal = builder.defineList("blacklist", () -> Collections.singletonList("thermal"), Predicates.alwaysTrue());
	//	}
	//
	//	public static void refresh() {
	//		inventoryDetailedShowAmount = inventorySneakShowAmountVal.get();
	//		inventoryNormalShowAmount = inventoryNormalShowAmountVal.get();
	//		inventoryShowItemPreLine = inventoryShowItemPreLineVal.get();
	//		bypassLockedContainer = bypassLockedContainerVal.get();
	//		inventoryBlacklist.clear();
	//		inventoryBlacklist.addAll(BlockInventoryProvider.INVENTORY_IGNORE);
	//		inventoryBlacklist.addAll(inventoryBlacklistVal.get());
	//
	//		onlyShowVanilla = onlyShowVanillaVal.get();
	//		modBlacklist.clear();
	//		modBlacklist.addAll(modBlacklistVal.get());
	//	}
	//
	//	@SubscribeEvent
	//	public static void onConfigReload(ModConfigEvent.Reloading event) {
	//		((CommentedFileConfig) event.getConfig().getConfigData()).load();
	//		refresh();
	//	}

	public static boolean shouldIgnoreTE(String id) {
		return inventoryBlacklist.contains(id);
	}

	public static boolean shouldShowCustomName(BlockEntity t) {
		String modid = CommonProxy.getId(t.getType()).getNamespace();
		if (onlyShowVanilla) {
			return ResourceLocation.DEFAULT_NAMESPACE.equals(modid);
		} else {
			return !modBlacklist.contains(modid);
		}
	}

}
