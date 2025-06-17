package snownee.jade.mixin.key_extension;

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import snownee.jade.key_extension.KeyMappingEx;

@Mixin(KeyBindsList.class)
public class KeyBindsListMixin {
	@WrapOperation(
			method = "<init>",
			at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;"))
	private KeyMapping[] keyEx$filterDisabled(Options options, Operation<KeyMapping[]> original) {
		return Stream.of(original.call(options)).filter($ -> ((KeyMappingEx) $).keyEx$isActive()).toArray(KeyMapping[]::new);
	}

	@Mixin(KeyBindsList.KeyEntry.class)
	public static class KeyEntryMixin {
		@Shadow
		@Final
		private KeyMapping key;
		@Shadow
		private boolean hasCollision;
		@Unique
		private boolean softCollision;

		@WrapOperation(
				method = "render",
				at = @At(
						value = "FIELD",
						target = "Lnet/minecraft/client/gui/screens/options/controls/KeyBindsList$KeyEntry;hasCollision:Z"))
		private boolean keyEx$render(KeyBindsList.KeyEntry instance, Operation<Boolean> original) {
			return !softCollision && original.call(instance);
		}

		@Inject(
				method = "refreshEntry",
				at = @At(
						value = "FIELD",
						ordinal = 3,
						target = "Lnet/minecraft/client/gui/screens/options/controls/KeyBindsList$KeyEntry;hasCollision:Z")
		)
		private void keyEx$refreshEntry(CallbackInfo ci) {
			softCollision = false;
			if (!hasCollision || key.isUnbound()) {
				return;
			}
			softCollision = true;
			if (((KeyMappingEx) key).keyEx$isNoConflict()) {
				return;
			}
			for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
				if (keyMapping != key && key.same(keyMapping) && !((KeyMappingEx) keyMapping).keyEx$isNoConflict()) {
					softCollision = false;
					break;
				}
			}
		}

		@WrapOperation(
				method = "refreshEntry",
				at = @At(
						value = "INVOKE",
						ordinal = 1,
						target = "Lnet/minecraft/client/gui/components/Button;setMessage(Lnet/minecraft/network/chat/Component;)V"))
		private void keyEx$setMessage(Button button, Component message, Operation<Void> original) {
			if (softCollision) {
				original.call(button, button.getMessage().copy().withStyle(ChatFormatting.YELLOW));
				return;
			}
			original.call(button, message);
		}
	}
}
