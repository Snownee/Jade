package snownee.jade.util;

import org.spongepowered.asm.mixin.Unique;

public interface JadeGuiGraphics {
	@Unique
	void jade$setIgnoreScissorTest(boolean ignore);

	boolean jade$isIgnoreScissorTest();
}
