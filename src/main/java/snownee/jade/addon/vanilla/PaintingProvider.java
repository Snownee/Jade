package snownee.jade.addon.vanilla;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Painting;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum PaintingProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Painting painting = (Painting) accessor.getEntity();
		if (painting.motive == null) {
			return;
		}
		String name = painting.motive.getRegistryName().getPath().replace('_', ' ');
		tooltip.add(new TextComponent(name));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_PAINTING;
	}
}
