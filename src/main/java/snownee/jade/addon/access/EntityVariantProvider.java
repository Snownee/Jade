package snownee.jade.addon.access;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class EntityVariantProvider implements IEntityComponentProvider {

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Entity entity = accessor.getEntity();
		String variantName = EntityVariantHelper.getVariantName(entity, false);
		if (variantName != null) {
			String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toShortLanguageKey();
			String key = "jade.access.entity.%s.%s".formatted(type, variantName);
			if (I18n.exists(key) || (config.get(JadeIds.DEBUG_SPECIAL_REGISTRY_NAME) && !accessor.showDetails())) {
				variantName = I18n.get(key);
			} else {
				variantName = variantName.replace('.', ' ').replace('_', ' ');
			}
			tooltip.add(Component.translatable("jade.access.entity.variant", variantName));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_ENTITY_VARIANT;
	}
}
