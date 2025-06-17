package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.GuiGraphics;
import snownee.jade.util.JadeGuiGraphics;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements JadeGuiGraphics {
	@Unique
	private boolean jade$ignoreScissorTest;

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
}
