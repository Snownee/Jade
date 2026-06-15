package snownee.jade.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import snownee.jade.Jade;
import snownee.jade.api.JadeIds;

public class JadeLanguages implements WordCutter.TokenClassifier {
	public static final Identifier ID = JadeIds.JADE("languages");
	public static final JadeLanguages INSTANCE = new JadeLanguages();
	private final EnumMap<WordCutter.TokenType, Pattern> tokens = new EnumMap<>(WordCutter.TokenType.class);
	private final Cache<String, WordCutter.TokenType> tokenCache = CacheBuilder.newBuilder().maximumSize(100).build();
	private Map<String, Pattern> nameClasses = Map.of();
	private final Cache<String, String> nameClassCache = CacheBuilder.newBuilder().maximumSize(100).build();
	private Locale locale = Locale.ENGLISH;
	private boolean rtl;
	private static final List<String> hackyPackFeatures = List.of(
			"container.crafter",
			"container.inventory",
			"container.crafting",
			"container.chest",
			"entity.minecraft.chest_boat");
	private Map<String, String> cleanTranslations = Map.of();
	private final Cache<String, ComponentContents> cleanTranslationCache = CacheBuilder.newBuilder().maximumSize(100).build();

	public Component toCleanTranslation(Component text) {
		if (text.getContents() instanceof TranslatableContents contents && contents.getArgs().length == 0) {
			String key = contents.getKey();
			try {
				ComponentContents clean = cleanTranslationCache.get(
						key,
						() -> new PlainTextContents.LiteralContents(getCleanTranslation(key)));
				return MutableComponent.create(clean).withStyle(text.getStyle());
			} catch (ExecutionException e) {
				return text;
			}
		}
		return text;
	}

	public void onResourceManagerReload(ResourceManager resourceManager, List<String> languageStack) {
		tokens.clear();
		tokenCache.invalidateAll();
		nameClasses = Map.of();
		nameClassCache.invalidateAll();
		try {
			JsonObject jsonObject = JsonConfig.GSON.fromJson(I18n.get("jade.metadata"), JsonObject.class);
			Metadata metadata = Metadata.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
			String langCode = Minecraft.getInstance().getLanguageManager().getSelected();
			if (metadata.lang.contains(langCode)) {
				String[] langSplit = langCode.split("_", 2);
				//noinspection deprecation
				locale = langSplit.length == 1 ? new Locale(langSplit[0]) : new Locale(langSplit[0], langSplit[1]);
				rtl = metadata.rtl;
				Preconditions.checkState(!metadata.tokens.containsKey(WordCutter.TokenType.WORD), "Word token type is not allowed");
				tokens.putAll(metadata.tokens);
				nameClasses = metadata.nameClasses;
			}
		} catch (Throwable e) {
			Jade.LOGGER.error("Failed to load Jade language metadata", e);
		}

		if (hasHackyPack()) {
			Set<String> hackyKeys = Sets.newHashSet();
			Map<String, String> translations = Maps.newHashMap();

			for (String languageCode : languageStack) {
				String path = String.format(Locale.ROOT, "lang/%s.json", languageCode);

				for (String namespace : resourceManager.getNamespaces()) {
					try {
						Identifier location = Identifier.fromNamespaceAndPath(namespace, path);
						appendFrom(languageCode, resourceManager.getResourceStack(location), hackyKeys, translations);
					} catch (Exception var10) {
						Jade.LOGGER.warn("Skipped language file: {}:{} ({})", namespace, path, var10.toString());
					}
				}
			}

			translations.keySet().removeIf(key -> !hackyKeys.contains(key));
			cleanTranslations = Map.copyOf(translations);
		} else {
			cleanTranslations = Map.of();
		}
	}

	public String getCleanTranslation(String key) {
		String s = cleanTranslations.get(key);
		return s != null ? s : I18n.get(key);
	}

	private static boolean hasHackyPack() {
		for (String key : hackyPackFeatures) {
			if (isPuaString(I18n.get(key))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isPuaString(String s) {
		return s.codePoints().allMatch(codePoint -> {
			int type = Character.getType(codePoint);
			return type == Character.PRIVATE_USE || type == Character.SPACE_SEPARATOR;
		});
	}

	private static void appendFrom(
			String languageCode,
			List<Resource> resources,
			Set<String> hackyKeys,
			Map<String, String> globalTranslations) {
		for (Resource resource : resources) {
			try {
				InputStream inputStream = resource.open();

				try {
					JsonObject entries = Language.GSON.fromJson(
							new InputStreamReader(inputStream, StandardCharsets.UTF_8),
							JsonObject.class);
					Map<String, String> translations = Maps.newHashMap();

					for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
						String text = Language.UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString(
								entry.getValue(),
								entry.getKey())).replaceAll("%$1s");
						translations.put(entry.getKey(), text);
					}

					boolean hacky = false;
					for (String key : hackyPackFeatures) {
						if (translations.containsKey(key) && isPuaString(translations.get(key))) {
							hacky = true;
							break;
						}
					}
					if (hacky) {
						List<String> keysToRemove = Lists.newArrayList();
						translations.forEach((key, value) -> {
							if (isPuaString(value)) {
								keysToRemove.add(key);
							}
						});
						for (String key : keysToRemove) {
							translations.remove(key);
							hackyKeys.add(key);
						}
					}
					globalTranslations.putAll(translations);
				} catch (Throwable var9) {
					try {
						inputStream.close();
					} catch (Throwable var8) {
						var9.addSuppressed(var8);
					}

					throw var9;
				}

				inputStream.close();
			} catch (IOException var10) {
				Jade.LOGGER.warn("Failed to load translations for {} from pack {}", languageCode, resource.sourcePackId(), var10);
			}
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
