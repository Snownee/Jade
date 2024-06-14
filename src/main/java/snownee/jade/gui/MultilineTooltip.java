package snownee.jade.gui;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class MultilineTooltip {
	public static Tooltip create(List<Component> components) {
		return create(components, components);
	}

	public static Tooltip create(List<Component> components, @Nullable List<Component> narration) {
		return Tooltip.create(compose(components), narration == null ? null : compose(narration));
	}

	private static Component compose(List<Component> components) {
		if (components.isEmpty()) {
			return Component.empty();
		}
		if (components.size() == 1) {
			return components.getFirst();
		}
		Component linebreak = Component.literal("\n");
		return components.stream().skip(1).reduce(
				components.getFirst().copy(),
				(a, b) -> a.append(linebreak).append(b),
				(a, b) -> a.append(linebreak).append(b));
	}
}
