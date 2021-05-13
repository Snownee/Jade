package mcp.mobius.waila.api;

import java.util.List;

import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Tooltip that you can append text and other renderable stuffs to.
 * 
 * @author Snownee
 */
public interface ITooltip {

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
	default void add(ITextComponent component) {
		add(component, null);
	}

	/**
	 * Add a tagged text to a new line
	 */
	default void add(ITextComponent component, ResourceLocation tag) {
		add(size(), component, tag);
	}

	default void add(int index, ITextComponent component) {
		add(index, component, null);
	}

	default void add(int index, ITextComponent component, ResourceLocation tag) {
		add(index, getElementHelper().text(component).tag(tag));
	}

	default void addAll(List<ITextComponent> components) {
		components.forEach(this::add);
	}

	/**
	 * Add a renderable element to a new line
	 */
	default void add(IElement element) {
		add(size(), element);
	}

	default void add(int index, List<IElement> elements) {
		boolean first = true;
		for (IElement element : elements) {
			if (first) {
				add(index, element);
			} else {
				append(index, element);
			}
			first = false;
		}
	}

	default void add(List<IElement> elements) {
		add(size(), elements);
	}

	void add(int index, IElement element);

	/**
	 * Append a text to the last line
	 */
	default void append(ITextComponent component) {
		append(component, null);
	}

	/**
	 * Append a tagged text to the last line
	 */
	default void append(ITextComponent component, ResourceLocation tag) {
		append(getElementHelper().text(component).tag(tag));
	}

	/**
	 * Append a renderable element to the last line
	 */
	default void append(IElement element) {
		append(size() - 1, element);
	}

	/**
	 * Append renderable elements to the last line
	 */
	default void append(int index, List<IElement> elements) {
		for (IElement element : elements) {
			append(index, element);
		}
	}

	void append(int index, IElement element);

	/**
	 * Clear all elements that are tagged with this tag
	 */
	void remove(ResourceLocation tag);

	IElementHelper getElementHelper();

	/**
	 * Get all elements that are tagged with this tag
	 */
	List<IElement> get(ResourceLocation tag);
}
