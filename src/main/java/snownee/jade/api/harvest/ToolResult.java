package snownee.jade.api.harvest;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;

/**
 * Result of evaluating whether a tool tier matches a block state.
 */
public sealed interface ToolResult permits ToolResult.Fail, ToolResult.Success {
	/**
	 * Returns a failed result.
	 *
	 * @return failed result
	 */
	static ToolResult fail() {
		return Fail.INSTANCE;
	}

	/**
	 * Returns a successful result that displays the given stack.
	 *
	 * @param stack display stack
	 * @return successful result
	 */
	static ToolResult of(ItemStack stack) {
		Preconditions.checkArgument(!stack.isEmpty(), "stack cannot be empty");
		return new Success(stack);
	}

	/**
	 * Returns the stack shown for this result.
	 *
	 * @return display stack
	 */
	ItemStack displayStack();

	/**
	 * Returns whether the evaluation succeeded.
	 *
	 * @return {@code true} for success
	 */
	default boolean isSuccess() {
		return getClass() == Success.class;
	}

	record Fail() implements ToolResult {
		/**
		 * Singleton failed result.
		 */
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
