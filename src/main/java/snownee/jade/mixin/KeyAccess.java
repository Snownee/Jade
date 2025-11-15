package snownee.jade.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.network.chat.Component;

// We can't access-transform classes in Blaze3D, so we use Mixin here.
@Mixin(InputConstants.Key.class)
public interface KeyAccess {

	@Accessor
	@Mutable
	void setDisplayName(Supplier<Component> displayName);

}
