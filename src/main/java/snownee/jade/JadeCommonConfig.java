package snownee.jade;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import snownee.jade.addon.forge.BlockInventoryProvider;
import snownee.jade.util.PlatformProxy;

public final class JadeCommonConfig {

	public static final Set<String> inventoryBlacklist = Sets.newHashSet();
	public static boolean bypassLockedContainer = false;
	public static boolean onlyShowVanilla = false;
	public static final Set<String> modBlacklist = Sets.newHashSet();

	public static boolean shouldIgnoreTE(String id) {
		return inventoryBlacklist.contains(id);
	}

	public static boolean shouldShowCustomName(BlockEntity t) {
		String modid = PlatformProxy.getId(t.getType()).getNamespace();
		if (onlyShowVanilla) {
			return "minecraft".equals(modid);
		} else {
			return !modBlacklist.contains(modid);
		}
	}

}
