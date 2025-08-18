package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import snownee.jade.util.JadeGuiGraphics;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements JadeGuiGraphics {
//	@Shadow
//	@Final
//	private Minecraft minecraft;
	@Unique
	private boolean jade$ignoreScissorTest;
//	@Unique
//	private ItemStack jade$itemTooltipContext = ItemStack.EMPTY;

	@Inject(method = "containsPointInScissor", at = @At("HEAD"), cancellable = true)
	private void jade$containsPointInScissor(int x, int y, CallbackInfoReturnable<Boolean> cir) {
		if (jade$ignoreScissorTest) {
			cir.setReturnValue(true);
		}
	}

	@Override
	public void jade$setIgnoreScissorTest(boolean ignore) {
		this.jade$ignoreScissorTest = ignore;
	}

	@Override
	public boolean jade$isIgnoreScissorTest() {
		return jade$ignoreScissorTest;
	}

/*	@WrapMethod(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V")
	private void jade$captureItemContext(Font font, ItemStack itemStack, int i, int j, Operation<Void> original) {
		jade$itemTooltipContext = itemStack;
		original.call(font, itemStack, i, j);
		jade$itemTooltipContext = ItemStack.EMPTY;
	}

	@Inject(method = "setTooltipForNextFrameInternal", at = @At("HEAD"))
	private void jade$appendModName(
			Font font,
			List<ClientTooltipComponent> list,
			int i,
			int j,
			ClientTooltipPositioner clientTooltipPositioner,
			@Nullable ResourceLocation resourceLocation,
			boolean bl,
			CallbackInfo ci) {
		ItemStack itemStack = jade$itemTooltipContext;
		if (itemStack.isEmpty() && minecraft.screen instanceof AbstractContainerScreen<?> screen && screen.hoveredSlot != null &&
				screen.hoveredSlot.hasItem()) {
			itemStack = screen.hoveredSlot.getItem();
		}
		try {
			JadeClient.appendModName(list, itemStack);
		} catch (Exception ignored) {
		}
	}*/
}
