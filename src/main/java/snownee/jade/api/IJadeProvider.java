package snownee.jade.api;

import net.minecraft.resources.ResourceLocation;

public interface IJadeProvider {

	ResourceLocation getUid();

	default int getDefaultPriority() {
		return TooltipPosition.BODY;
	}

}
