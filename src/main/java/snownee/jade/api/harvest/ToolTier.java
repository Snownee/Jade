package snownee.jade.api.harvest;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.NonNull;

import snownee.jade.api.IJadeProvider;

public interface ToolTier extends IJadeProvider {

	List<ItemStack> getTools();

	boolean matches(ItemStack stack);

	static ToolTier of(Identifier uid, List<ItemStack> tools, Predicate<ItemStack> predicate) {
		Objects.requireNonNull(uid);
		Objects.requireNonNull(tools);
		Objects.requireNonNull(predicate);
		Preconditions.checkArgument(!tools.isEmpty(), "tools cannot be empty");
		return new SimpleToolTier(uid, List.copyOf(tools), predicate);
	}

	static ToolTier of(Identifier uid, ItemStack tool, Predicate<ItemStack> predicate) {
		Objects.requireNonNull(tool);
		return of(uid, List.of(tool), predicate);
	}

	static ToolTier item(Identifier uid, Item item) {
		Objects.requireNonNull(item);
		return stack(uid, item.getDefaultInstance());
	}

	static ToolTier stack(Identifier uid, ItemStack stack) {
		return of(uid, stack, _ -> true);
	}
}

record SimpleToolTier(Identifier uid, List<ItemStack> tools, Predicate<ItemStack> predicate) implements ToolTier {
	SimpleToolTier {
		Objects.requireNonNull(uid);
		Objects.requireNonNull(tools);
		Objects.requireNonNull(predicate);
		Preconditions.checkArgument(!tools.isEmpty(), "tools cannot be empty");
	}

	@Override
	public @NonNull Identifier getUid() {
		return uid;
	}

	@Override
	public List<ItemStack> getTools() {
		return tools;
	}

	@Override
	public boolean matches(ItemStack stack) {
		return predicate.test(stack);
	}
}
