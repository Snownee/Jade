package snownee.jade.api.config;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;

public class IgnoreList<T> {
	public List<String> values = List.of();
	public int version = 1;

	public void reload(Registry<T> registry, Consumer<T> consumer) {
		List<Pattern> patterns = Lists.newArrayList();
		for (String value : values) {
			try {
				if (value.startsWith("/") && value.endsWith("/") && value.length() > 1) {
					patterns.add(Pattern.compile(value.substring(1, value.length() - 1)));
				} else {
					ResourceLocation id = new ResourceLocation(value);
					Optional<T> optional = registry.getOptional(id);
					if (optional.isPresent()) {
						consumer.accept(registry.get(id));
					} else {
						throw new IllegalArgumentException("Unknown id: " + id);
					}
				}
			} catch (Exception e) {
				Jade.LOGGER.error("Failed to parse ignore list entry: %s".formatted(value), e);
			}
		}
		if (patterns.isEmpty()) {
			return;
		}
		for (Holder<T> holder : registry.asHolderIdMap()) {
			String s = holder.unwrapKey().orElseThrow().location().toString();
			for (Pattern pattern : patterns) {
				if (pattern.matcher(s).find()) {
					consumer.accept(holder.value());
					break;
				}
			}
		}
	}
}
