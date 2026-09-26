package snownee.jade.gui.config;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Registry of {@code ${NAME}} placeholders that can appear in option titles and descriptions.
 * <p>
 * The options UI itself has no knowledge of any placeholder; implementations register the ones they need on the
 * registry of the {@link OptionsList} instance they are configuring (for example Jade registers key bind dependent
 * variables for its own config screens).
 */
public final class BuiltInVariables {

	private final Map<String, Supplier<Component>> variables = Maps.newLinkedHashMap();

	public void register(String name, Supplier<Component> supplier) {
		variables.put(name, supplier);
	}

	public Component process(Component component) {
		for (Map.Entry<String, Supplier<Component>> entry : variables.entrySet()) {
			String variable = "${" + entry.getKey() + "}";
			if (component.getString().contains(variable)) {
				component = replace(component, variable, entry.getValue().get());
			}
		}
		return component;
	}

	private static Component replace(Component component, String source, Component replacement) {
		MutableComponent newComponent = Component.empty().withStyle(component.getStyle());
		for (Component part : component.toFlatList()) {
			String partString = part.getString();
			if (partString.contains(source)) {
				boolean first = true;
				for (String s : StringUtils.splitByWholeSeparatorPreserveAllTokens(partString, source)) {
					if (first) {
						first = false;
					} else {
						newComponent.append(replacement);
					}
					if (!s.isEmpty()) {
						newComponent.append(Component.literal(s));
					}
				}
			} else {
				newComponent.append(part);
			}
		}
		return newComponent;
	}
}
