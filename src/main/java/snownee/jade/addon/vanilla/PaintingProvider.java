package snownee.jade.addon.vanilla;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Painting;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.util.CommonProxy;

public enum PaintingProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Painting painting = (Painting) accessor.getEntity();
		ResourceLocation id = CommonProxy.getId(painting.getVariant().value());
		tooltip.add(IThemeHelper.get().warning(Component.translatable(id.toLanguageKey("painting", "title"))));
		tooltip.add(Component.translatable(id.toLanguageKey("painting", "author")));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_PAINTING;
	}
}
