package snownee.jade.addon.vanilla;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.painting.Painting;
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
		painting.level().registryAccess().get(painting.getVariant().unwrapKey().orElseThrow()).map(Holder.Reference::value).ifPresent(variant -> {
			variant.title().map(IThemeHelper.get()::warning).ifPresent(tooltip::add);
			variant.author().ifPresent(tooltip::add);
		});
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_PAINTING;
	}
}
