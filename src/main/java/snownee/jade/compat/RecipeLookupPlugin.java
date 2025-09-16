package snownee.jade.compat;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface RecipeLookupPlugin {
	RecipeLookupResult lookup(ItemStack itemStack, @Nullable ResourceLocation specialId, boolean uses);
}
