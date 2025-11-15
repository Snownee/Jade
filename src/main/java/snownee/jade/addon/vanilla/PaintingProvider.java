package snownee.jade.addon.vanilla;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.Painting;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public class PaintingProvider implements IEntityComponentProvider {
	public static final PaintingProvider INSTANCE = new PaintingProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Painting painting = (Painting) accessor.getEntity();
		Identifier id = painting.getVariant().unwrapKey().orElseThrow().identifier();
		tooltip.add(IThemeHelper.get().warning(Component.translatable(id.toLanguageKey("painting", "title"))));
		tooltip.add(Component.translatable(id.toLanguageKey("painting", "author")));
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_PAINTING;
	}
}
