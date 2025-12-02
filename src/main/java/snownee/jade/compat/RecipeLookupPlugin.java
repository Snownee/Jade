package snownee.jade.compat;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public interface RecipeLookupPlugin {
	RecipeLookupResult lookup(ItemStack itemStack, @Nullable Identifier specialId, boolean uses);
}
