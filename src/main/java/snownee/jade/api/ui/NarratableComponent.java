package snownee.jade.api.ui;

import java.util.List;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/**
 * Component wrapper that provides a dedicated narration string.
 */
public class NarratableComponent implements Component {
	private final Component component;
	private final @Nullable Supplier<String> narration;

	public NarratableComponent(Component component) {
		this(component, null);
	}

	public NarratableComponent(Component component, @Nullable Supplier<String> narration) {
		this.component = component;
		this.narration = narration;
	}

	@Override
	public Style getStyle() {
		return component.getStyle();
	}

	@Override
	public ComponentContents getContents() {
		return component.getContents();
	}

	@Override
	public List<Component> getSiblings() {
		return component.getSiblings();
	}

	@Override
	public FormattedCharSequence getVisualOrderText() {
		return component.getVisualOrderText();
	}

	@Override
	public String toString() {
		return component.getString();
	}

	public String getNarration() {
		return narration != null ? narration.get() : component.getString();
	}

	public static Component getNarration(FormattedText text) {
		if (text instanceof NarratableComponent narratable) {
			return Component.literal(narratable.getNarration());
		}
		if (text instanceof Component component) {
			return component;
		}
		return Component.literal(text.getString());
	}

	public static Component attach(Component component, Component narratable) {
		if (narratable instanceof NarratableComponent narratableComponent) {
			return new NarratableComponent(component, narratableComponent.narration);
		}
		return new NarratableComponent(component, narratable::getString);
	}

	public static Component translatable(String key, Object... objects) {
		boolean hasNarratable = false;
		for (Object object : objects) {
			if (object instanceof NarratableComponent) {
				hasNarratable = true;
				break;
			}
		}
		MutableComponent component = Component.translatable(key, objects);
		if (!hasNarratable) {
			return component;
		}
		Object[] narrationArgs = new Object[objects.length];
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof NarratableComponent narratable) {
				narrationArgs[i] = narratable.getNarration();
			} else {
				narrationArgs[i] = objects[i];
			}
		}
		return new NarratableComponent(component, () -> Component.translatable(key, narrationArgs).getString());
	}
}
