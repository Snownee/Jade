package snownee.jade.api.view;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import snownee.jade.api.ui.Element;

/**
 * Client-side item entry used by Jade's storage views.
 */
public class ItemView {

	/**
	 * Item stack to render.
	 */
	public ItemStack item;
	/**
	 * Optional amount label.
	 */
	@Nullable
	public String amountText;
	/**
	 * Optional description elements.
	 */
	@Nullable
	public List<Element> description;

	/**
	 * Creates a view for the given item stack.
	 *
	 * @param item item stack to render
	 */
	public ItemView(ItemStack item) {
		Objects.requireNonNull(item);
		this.item = item;
	}

	/**
	 * Sets the displayed amount text.
	 *
	 * @param amountText amount label
	 * @return this view
	 */
	@Contract("_ -> this")
	public ItemView amountText(String amountText) {
		this.amountText = amountText;
		return this;
	}

	/**
	 * Sets the description elements.
	 *
	 * @param description description elements
	 * @return this view
	 */
	@Contract("_ -> this")
	public ItemView description(List<Element> description) {
		this.description = description;
		return this;
	}

}
