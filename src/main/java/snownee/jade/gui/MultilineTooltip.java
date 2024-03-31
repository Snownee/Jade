package snownee.jade.gui;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class MultilineTooltip {
	public static Tooltip create(List<Component> components) {
		return create(components, components);
	}

	public static Tooltip create(List<Component> components, @Nullable List<Component> narration) {
		Component message = compose(components);
		Tooltip tooltip = Tooltip.create(message, narration == null ? null : compose(narration));
		tooltip.cachedTooltip = components.stream().flatMap($ -> Tooltip.splitTooltip(Minecraft.getInstance(), $).stream()).toList();
		return tooltip;
	}

	private static Component compose(List<Component> components) {
		Component linebreak = Component.literal("\n");
		return components.stream().reduce(
				Component.empty(),
				(a, b) -> a.append(linebreak).append(b),
				(a, b) -> a.append(linebreak).append(b));
	}
}
