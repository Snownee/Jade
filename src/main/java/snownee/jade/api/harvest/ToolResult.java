package snownee.jade.api.harvest;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;

public sealed interface ToolResult permits ToolResult.Fail, ToolResult.Success {
	static ToolResult fail() {
		return Fail.INSTANCE;
	}

	static ToolResult of(ItemStack stack) {
		Preconditions.checkArgument(!stack.isEmpty(), "stack cannot be empty");
		return new Success(stack);
	}

	ItemStack displayStack();

	default boolean isSuccess() {
		return getClass() == Success.class;
	}

	record Fail() implements ToolResult {
		static final Fail INSTANCE = new Fail();

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
