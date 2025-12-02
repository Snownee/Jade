package snownee.jade.addon.access;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.EitherHolder;
import snownee.jade.mixin.EntityAccess;
import snownee.jade.util.CommonProxy;

public final class EntityVariantHelper {
	private static final Object2BooleanMap<DataComponentType<?>> isVariantType = new Object2BooleanOpenHashMap<>();
	private static final Set<DataComponentType<?>> isColorType = Sets.newHashSet();
	private static final Map<EntityType<?>, @Nullable DataComponentType<?>> variantTypeByEntity = Maps.newHashMap();

	public static synchronized void addVariantMapping(EntityType<?> entityType, @Nullable DataComponentType<?> variantType) {
		variantTypeByEntity.put(entityType, variantType);
		if (variantType != null && !isVariantType.containsKey(variantType)) {
			addVariantType(variantType, true);
		}
	}

	public static synchronized void addVariantType(DataComponentType<?> type, boolean isVariant) {
		Identifier key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
		if (key == null) {
			isVariant = false;
		}
		isVariantType.put(type, isVariant);
		boolean isColor = key != null && key.getPath().endsWith("/color");
		if (isColor) {
			isColorType.add(type);
		}
	}

	@Nullable
	public static synchronized DataComponentType<?> getVariantType(Entity entity) {
		EntityType<?> entityType = entity.getType();
		if (variantTypeByEntity.containsKey(entityType)) {
			return variantTypeByEntity.get(entityType);
		}
		for (DataComponentType<?> type : componentTypes(entity)) {
			if (isVariantType(type)) {
				variantTypeByEntity.put(entityType, type);
				return type;
			}
		}
		return null;
	}

	@Nullable
	public static synchronized Object getVariant(Entity entity, boolean isColor) {
		DataComponentType<?> type = getVariantType(entity);
		if (type == null || isColorType.contains(type) != isColor) {
			return null;
		}
		return entity.get(type);
	}

	@Nullable
	public static synchronized Either<String, Component> getVariantName(Entity entity, boolean isColor) {
		Object variant = getVariant(entity, isColor);
		switch (variant) {
			case null -> {
				return null;
			}
			case Holder<?> holder -> {
				Identifier id = holder.unwrapKey().map(ResourceKey::identifier).orElse(null);
				variant = id != null ? id : holder.value();
			}
			case EitherHolder<?> holder -> variant = holder.key().map(ResourceKey::identifier).orElse(null);
			default -> {
			}
		}
		String name = null;
		Either<String, Component> result = CommonProxy.getTranslatableName(variant);
		if (result != null) {
			return result;
		} else if (variant instanceof Identifier id) {
			name = id.toShortLanguageKey();
		} else if (variant instanceof String) {
			name = variant.toString();
		} else if (variant instanceof StringRepresentable stringRepresentable) {
			name = stringRepresentable.getSerializedName();
		} else if (variant instanceof Enum<?> enumValue) {
			name = enumValue.name();
		}
		return name == null ? null : Either.left(name.toLowerCase(Locale.ENGLISH));
	}

	private static boolean isVariantType(DataComponentType<?> type) {
		if (isVariantType.containsKey(type)) {
			return isVariantType.getBoolean(type);
		}
		Identifier key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
		if (key == null) {
			addVariantType(type, false);
			return false;
		}
		String name = key.getPath();
		boolean result = name.endsWith("/variant") || key.getPath().endsWith("/color");
		addVariantType(type, result);
		return result;
	}

	private static List<DataComponentType<?>> componentTypes(Entity entity) {
		List<DataComponentType<?>> types = Lists.newArrayList();
		((EntityAccess) entity).callApplyImplicitComponents(new DataComponentGetter() {
			@Override
			public @Nullable <T> T get(DataComponentType<? extends T> dataComponentType) {
				types.add(dataComponentType);
				return null;
			}
		});
		return types;
	}
}
