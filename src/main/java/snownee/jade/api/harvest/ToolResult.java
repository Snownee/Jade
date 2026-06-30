package snownee.jade.api.harvest;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;

public sealed interface ToolResult permits ToolResult.Pass, ToolResult.Success {
	static ToolResult pass() {
		return Pass.INSTANCE;
	}

	static ToolResult of(ItemStack stack) {
		Preconditions.checkArgument(!stack.isEmpty(), "stack cannot be empty");
		return new Success(stack);
	}

	ItemStack displayStack();

	default boolean isSuccess() {
		return getClass() == Success.class;
	}

	record Pass() implements ToolResult {
		static final Pass INSTANCE = new Pass();

		@Override
		public ItemStack displayStack() {
			return ItemStack.EMPTY;
		}
	}

	record Success(ItemStack stack) implements ToolResult {
		@Override
		public ItemStack displayStack() {
			return stack;
		}
	}
}
