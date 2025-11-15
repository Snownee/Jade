package snownee.jade.util;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import snownee.jade.api.IJadeProvider;

public interface KeyedReloadListener extends PreparableReloadListener, IJadeProvider, IdentifiableResourceReloadListener {
	@Override
	default Identifier getFabricId() {
		return getUid();
	}
}
