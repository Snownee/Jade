package mcp.mobius.waila.api;

import java.util.List;

import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public interface ITooltip {

	void clear();

	int size();

	default boolean isEmpty() {
		return size() == 0;
	}

	default void add(ITextComponent component) {
		add(component, null);
	}

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

	default void append(ITextComponent component) {
		append(component, null);
	}

	default void append(ITextComponent component, ResourceLocation tag) {
		append(getElementHelper().text(component).tag(tag));
	}

	default void append(IElement element) {
		append(size() - 1, element);
	}

	default void append(int index, List<IElement> elements) {
		for (IElement element : elements) {
			append(index, element);
		}
	}

	void append(int index, IElement element);

	void remove(ResourceLocation tag);

	IElementHelper getElementHelper();

	List<IElement> get(ResourceLocation tag);
}
