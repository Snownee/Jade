package snownee.jade.mixin.key_extension;

import java.util.stream.Stream;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import snownee.jade.key_extension.KeyMappingEx;

@Mixin(KeyBindsList.class)
public class KeyBindsListMixin {
	@WrapOperation(
			method = "<init>",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;",
					opcode = Opcodes.GETFIELD))
	private KeyMapping[] keyEx$filterDisabled(Options options, Operation<KeyMapping[]> original) {
		return Stream.of(original.call(options)).filter($ -> ((KeyMappingEx) $).keyEx$isActive()).toArray(KeyMapping[]::new);
	}
}
