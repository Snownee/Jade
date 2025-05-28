package snownee.jade.util;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import snownee.jade.Jade;
import snownee.jade.api.JadeIds;

public class JadeLanguages implements KeyedResourceManagerReloadListener, WordCutter.TokenClassifier {
	public static final ResourceLocation ID = JadeIds.JADE("languages");
	public static final JadeLanguages INSTANCE = new JadeLanguages();
	private final EnumMap<WordCutter.TokenType, Pattern> tokens = new EnumMap<>(WordCutter.TokenType.class);
	private final Cache<String, WordCutter.TokenType> tokenCache = CacheBuilder.newBuilder().maximumSize(100).build();
	private Map<String, Pattern> nameClasses = Map.of();
	private final Cache<String, String> nameClassCache = CacheBuilder.newBuilder().maximumSize(100).build();
	private Locale locale = Locale.ENGLISH;
	private boolean rtl;

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		tokens.clear();
		tokenCache.invalidateAll();
		nameClasses = Map.of();
		nameClassCache.invalidateAll();
		try {
			JsonObject jsonObject = JsonConfig.GSON.fromJson(I18n.get("jade.metadata"), JsonObject.class);
			Metadata metadata = Metadata.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
			String langCode = Minecraft.getInstance().getLanguageManager().getSelected();
			if (!metadata.lang.contains(langCode)) {
				return;
			}
			String[] langSplit = langCode.split("_", 2);
			//noinspection deprecation
			locale = langSplit.length == 1 ? new Locale(langSplit[0]) : new Locale(langSplit[0], langSplit[1]);
			rtl = metadata.rtl;
			Preconditions.checkState(!metadata.tokens.containsKey(WordCutter.TokenType.WORD), "Word token type is not allowed");
			tokens.putAll(metadata.tokens);
			nameClasses = metadata.nameClasses;
		} catch (Throwable e) {
			Jade.LOGGER.error("Failed to load Jade language metadata", e);
		}
	}

	public boolean isRTL() {
		return rtl;
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

	public Locale getLocale() {
		return locale;
	}

	@Override
	public ResourceLocation getUid() {
		return ID;
	}

	@Override
	public WordCutter.TokenType classify(String s) {
		if (s.isBlank()) {
			return WordCutter.TokenType.SEPARATOR;
		}
		if (tokens.isEmpty()) {
			return switch (s) {
				case "(", "[", "<" -> WordCutter.TokenType.LEFT_BRACKET;
				case ")", "]", ">" -> WordCutter.TokenType.RIGHT_BRACKET;
				case ":" -> WordCutter.TokenType.COLON;
				case "|", "-", ",", "/", "&" -> WordCutter.TokenType.SYMBOL;
				default -> WordCutter.TokenType.WORD;
			};
		} else {
			try {
				return tokenCache.get(
						s, () -> {
							for (Map.Entry<WordCutter.TokenType, Pattern> entry : tokens.entrySet()) {
								if (entry.getValue().matcher(s).matches()) {
									return entry.getKey();
								}
							}
							return WordCutter.TokenType.WORD;
						});
			} catch (ExecutionException e) {
				return WordCutter.TokenType.WORD;
			}
		}
	}

	private record Metadata(List<String> lang, boolean rtl, Map<WordCutter.TokenType, Pattern> tokens, Map<String, Pattern> nameClasses) {
		static final Codec<Metadata> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.listOf().fieldOf("lang").forGetter(Metadata::lang),
				Codec.BOOL.optionalFieldOf("rtl", false).forGetter(Metadata::rtl),
				Codec.unboundedMap(StringRepresentable.fromEnum(WordCutter.TokenType::values), ExtraCodecs.PATTERN)
						.optionalFieldOf("tokens", Map.of())
						.forGetter(Metadata::tokens),
				Codec.unboundedMap(Codec.STRING, ExtraCodecs.PATTERN)
						.optionalFieldOf("nameClasses", Map.of())
						.forGetter(Metadata::nameClasses)).apply(i, Metadata::new));
	}
}
