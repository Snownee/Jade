package snownee.jade.addon.access;

import com.mojang.datafixers.util.Either;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class EntityVariantProvider implements IEntityComponentProvider {

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Either<String, Component> variantName = EntityVariantHelper.getVariantName(accessor.getEntity(), false);
		if (variantName == null) {
			return;
		}
		variantName.ifLeft(s -> {
			String type = BuiltInRegistries.ENTITY_TYPE.getKey(accessor.getEntity().getType()).toShortLanguageKey();
			String key = "jade.access.entity.%s.%s".formatted(type, s);
			if (I18n.exists(key) || (config.get(JadeIds.DEBUG_SPECIAL_REGISTRY_NAME) && !accessor.showDetails())) {
				s = I18n.get(key);
			} else {
				s = s.replace('.', ' ').replace('_', ' ');
			}
			tooltip.add(Component.translatable("jade.access.entity.variant", s));
		}).ifRight(component -> tooltip.add(Component.translatable("jade.access.entity.variant", component)));
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_ENTITY_VARIANT;
	}
}
