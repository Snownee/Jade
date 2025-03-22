package snownee.jade.addon.access;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.EitherHolder;

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
		ResourceLocation key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
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
	public static synchronized String getVariantName(Entity entity, boolean isColor) {
		Object variant = getVariant(entity, isColor);
		if (variant instanceof Holder<?> holder) {
			ResourceLocation id = holder.unwrapKey().map(ResourceKey::location).orElse(null);
			variant = id != null ? id : holder.value();
		} else if (variant instanceof EitherHolder<?> holder) {
			variant = holder.key().map(ResourceKey::location).orElse(null);
		}
		String name = null;
		if (variant instanceof ResourceLocation id) {
			name = id.toShortLanguageKey();
		} else if (variant instanceof String) {
			name = variant.toString();
		} else if (variant instanceof StringRepresentable stringRepresentable) {
			name = stringRepresentable.getSerializedName();
		} else if (variant instanceof Enum<?> enumValue) {
			name = enumValue.name();
		}
		return name == null ? null : name.toLowerCase(Locale.ENGLISH);
	}

	private static boolean isVariantType(DataComponentType<?> type) {
		if (isVariantType.containsKey(type)) {
			return isVariantType.getBoolean(type);
		}
		ResourceLocation key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
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
		entity.applyImplicitComponents(new DataComponentGetter() {
			@Override
			public @Nullable <T> T get(DataComponentType<? extends T> dataComponentType) {
				types.add(dataComponentType);
				return null;
			}
		});
		return types;
	}
}
