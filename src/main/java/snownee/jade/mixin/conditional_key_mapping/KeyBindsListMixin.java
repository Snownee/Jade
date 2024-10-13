package snownee.jade.mixin.conditional_key_mapping;

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import snownee.jade.conditional_key_mapping.ConditionalKeyMapping;

@Mixin(KeyBindsList.class)
public class KeyBindsListMixin {
	@WrapOperation(
			method = "<init>",
			at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;"))
	private KeyMapping[] conditionalKeyMapping$filterDisabled(Options options, Operation<KeyMapping[]> original) {
		return Stream.of(original.call(options))
				.filter($ -> ((ConditionalKeyMapping) $).conditionalKeyMapping$isEnabled())
				.toArray(KeyMapping[]::new);
	}
}
