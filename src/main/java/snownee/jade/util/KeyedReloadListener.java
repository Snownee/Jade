package snownee.jade.util;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import snownee.jade.api.IJadeProvider;

public interface KeyedReloadListener extends PreparableReloadListener, IJadeProvider, IdentifiableResourceReloadListener {
	@Override
	default ResourceLocation getFabricId() {
		return getUid();
	}
}
