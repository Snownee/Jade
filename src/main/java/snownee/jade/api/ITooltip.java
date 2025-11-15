package snownee.jade.api;

import java.util.List;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.ScreenDirection;

/**
 * Tooltip that you can append text and other render-able stuffs to.
 *
 * @author Snownee
 */
@NonExtendable
public interface ITooltip extends NarrationSupplier {

	void clear();

	/**
	 * Returns tooltip's number of lines
	 */
	int size();

	default boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Add a text to a new line
	 */
	default void add(Component component) {
		add(component, null);
	}

	/**
	 * Add a tagged text to a new line
	 */
	default void add(Component component, Identifier tag) {
		add(size(), component, tag);
	}

	default void add(int index, Component component) {
		add(index, component, null);
	}

	default void add(int index, Component component, Identifier tag) {
		add(index, JadeUI.text(component).tag(tag));
	}

	default void addAll(List<Component> components) {
		components.forEach(this::add);
	}

	/**
	 * Add a render-able element to a new line
	 */
	default void add(LayoutElement element) {
		add(size(), element);
	}

	default void add(int index, List<? extends LayoutElement> elements) {
		boolean first = true;
		for (LayoutElement element : elements) {
			if (first) {
				add(index, element);
			} else {
				append(index, element);
			}
			first = false;
		}
	}

	default void add(List<? extends LayoutElement> elements) {
		add(size(), elements);
	}

	void add(int index, LayoutElement element);

	/**
	 * Append a text to the last line
	 * <p>
	 * IMPORTANT: DO NOT use this to concat texts
	 */
	default void append(Component component) {
		append(component, null);
	}

	/**
	 * Append a tagged text to the last line
	 * <p>
	 * IMPORTANT: DO NOT use this to concat texts
	 */
	default void append(Component component, Identifier tag) {
		append(JadeUI.text(component).tag(tag));
	}

	/**
	 * Append a render-able element to the last line
	 */
	default void append(LayoutElement element) {
		append(size() - 1, element);
	}

	/**
	 * Append render-able elements to the last line
	 */
	default void append(int index, List<? extends LayoutElement> elements) {
		for (LayoutElement element : elements) {
			append(index, element);
		}
	}

	void append(int index, LayoutElement element);

	/**
	 * Clear all elements that are tagged with this tag
	 *
	 * @return true if any element is removed
	 */
	boolean remove(Identifier tag);

	/**
	 * Replace all elements that are tagged with this tag at the position of the first found element
	 *
	 * @return true if any element is replaced
	 */
	boolean replace(Identifier tag, UnaryOperator<List<List<LayoutElement>>> elements);

	boolean replace(Identifier tag, Component component);

	/**
	 * Get all elements that are tagged with this tag
	 */
	List<LayoutElement> get(Identifier tag);

	void setLineMargin(int index, ScreenDirection side, int margin);

	void setLineSettings(int index, UnaryOperator<LayoutSettings> settings);

	String getNarration();

	String getString(Identifier tag);

	@Nullable Element getIcon();
}
