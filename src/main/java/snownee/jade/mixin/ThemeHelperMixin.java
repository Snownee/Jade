package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.impl.theme.ThemeHelper;

@Mixin(ThemeHelper.class)
public abstract class ThemeHelperMixin implements IdentifiableResourceReloadListener {

	@Override
	public ResourceLocation getFabricId() {
		return ThemeHelper.ID;
	}

}
