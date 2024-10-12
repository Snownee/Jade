package snownee.jade.mixin;

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import snownee.jade.Jade;

@Mixin(KeyBindsList.class)
public class KeyBindsListMixin {
	@WrapOperation(
			method = "<init>",
			at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;"))
	private KeyMapping[] init(Options options, Operation<KeyMapping[]> original) {
		KeyMapping[] keyMappings = original.call(options);
		if (!Jade.rootConfig().isEnableProfiles()) {
			return Stream.of(keyMappings).filter($ -> !$.getName().startsWith("key.jade.profile.")).toArray(KeyMapping[]::new);
		}
		return keyMappings;
	}
}
