package snownee.jade.api.view;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import snownee.jade.api.ui.Element;

public class ItemView {

	public ItemStack item;
	@Nullable
	public String amountText;
	@Nullable
	public List<Element> description;

	public ItemView(ItemStack item) {
		Objects.requireNonNull(item);
		this.item = item;
	}

	@Contract("_ -> this")
	public ItemView amountText(String amountText) {
		this.amountText = amountText;
		return this;
	}

	@Contract("_ -> this")
	public ItemView description(List<Element> description) {
		this.description = description;
		return this;
	}

}
