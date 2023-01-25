package snownee.jade;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
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
			return ResourceLocation.DEFAULT_NAMESPACE.equals(modid);
		} else {
			return !modBlacklist.contains(modid);
		}
	}

}
