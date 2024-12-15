package snownee.jade.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ExtraCodecs;
import snownee.jade.Jade;
import snownee.jade.api.JadeIds;

public class JadeLanguages implements ResourceManagerReloadListener {
	public static final JadeLanguages INSTANCE = new JadeLanguages();
	public static final ResourceLocation ID = JadeIds.JADE("languages");
	private Map<String, Pattern> nameClasses = Map.of();
	private final Cache<String, String> nameClassCache = CacheBuilder.newBuilder().maximumSize(1000).build();

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		nameClasses = Map.of();
		nameClassCache.invalidateAll();
		try {
			JsonObject jsonObject = JsonConfig.GSON.fromJson(I18n.get("jade.metadata"), JsonObject.class);
			Metadata metadata = Metadata.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
			if (!metadata.lang.contains(Minecraft.getInstance().getLanguageManager().getSelected())) {
				return;
			}
			nameClasses = metadata.nameClasses;
		} catch (Throwable e) {
			Jade.LOGGER.error("Failed to load Jade language metadata", e);
		}
	}

	public String getNameClass(String name) {
		if (nameClasses.isEmpty()) {
			return "other";
		}
		try {
			return nameClassCache.get(
					name, () -> {
						for (Map.Entry<String, Pattern> entry : nameClasses.entrySet()) {
							if (entry.getValue().matcher(name).matches()) {
								return entry.getKey();
							}
						}
						return "other";
					});
		} catch (ExecutionException e) {
			return "other";
		}
	}

	private record Metadata(List<String> lang, Map<String, Pattern> nameClasses) {
		static final Codec<Metadata> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.listOf().fieldOf("lang").forGetter(Metadata::lang),
				Codec.unboundedMap(Codec.STRING, ExtraCodecs.PATTERN)
						.optionalFieldOf("nameClasses", Map.of())
						.forGetter(Metadata::nameClasses)
		).apply(i, Metadata::new));
	}
}
