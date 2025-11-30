package snownee.jade.addon.access;

import java.util.Map;

import com.mojang.datafixers.util.Either;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class EntityVariantProvider implements IEntityComponentProvider {
	private static final Map<Markings, String> MARKINGS = Map.of(
			Markings.NONE, "none",
			Markings.WHITE, "white",
			Markings.WHITE_FIELD, "white_field",
			Markings.WHITE_DOTS, "white_dots",
			Markings.BLACK_DOTS, "black_dots");

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Entity entity = accessor.getEntity();
		Either<String, Component> variantName = EntityVariantHelper.getVariantName(entity, false);
		if (variantName == null) {
			return;
		}
		variantName.ifLeft(s -> {
			String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toShortLanguageKey();
			String key = "jade.access.entity.%s.%s".formatted(type, s);
			if (I18n.exists(key) || (config.get(JadeIds.DEBUG_SPECIAL_REGISTRY_NAME) && !accessor.showDetails())) {
				s = I18n.get(key);
			} else {
				s = s.replace('.', ' ').replace('_', ' ');
			}
			tooltip.add(Component.translatable("jade.access.entity.variant", s));
		}).ifRight(component -> tooltip.add(Component.translatable("jade.access.entity.variant", component)));
		if (entity instanceof Horse horse) {
			Markings markings = horse.getMarkings();
			String s = MARKINGS.get(markings);
			if (s == null) {
				s = markings.name();
			}
			String key = "jade.access.entity.horse_markings.%s".formatted(s);
			if (I18n.exists(key) || (config.get(JadeIds.DEBUG_SPECIAL_REGISTRY_NAME) && !accessor.showDetails())) {
				s = I18n.get(key);
			} else {
				s = s.replace('_', ' ');
			}
			tooltip.add(Component.translatable("jade.access.entity.horse_markings", s));
		}
	}

	@Override
	public Identifier getUid() {
		return JadeIds.ACCESS_ENTITY_VARIANT;
	}
}
