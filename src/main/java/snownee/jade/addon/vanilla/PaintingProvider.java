package snownee.jade.addon.vanilla;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Painting;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum PaintingProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Painting painting = (Painting) accessor.getEntity();
		ResourceLocation id = painting.getVariant().unwrapKey().orElseThrow().location();
		tooltip.add(IThemeHelper.get().warning(Component.translatable(id.toLanguageKey("painting", "title"))));
		tooltip.add(Component.translatable(id.toLanguageKey("painting", "author")));
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_PAINTING;
	}
}
