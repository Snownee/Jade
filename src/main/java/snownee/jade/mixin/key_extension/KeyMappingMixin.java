package snownee.jade.mixin.key_extension;

import java.util.Collection;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import snownee.jade.key_extension.KeyExManager;
import snownee.jade.key_extension.KeyMappingEx;

@Mixin(value = KeyMapping.class, priority = 900)
public abstract class KeyMappingMixin implements KeyMappingEx {

	@Shadow
	@Final
	private static Map<String, KeyMapping> ALL;
	@Shadow
	protected InputConstants.Key key;

	@Shadow
	public abstract boolean isUnbound();

	@Unique
	private boolean active = true;

	@Override
	public boolean keyEx$isActive() {
		return active;
	}

	@Override
	public void keyEx$setActive(boolean active) {
		boolean changed = this.active != active;
		this.active = active;
		if (changed && !isUnbound()) {
			KeyExManager.markDirty();
		}
	}

	@Override
	public InputConstants.Key keyEx$key() {
		return key;
	}

	@WrapOperation(method = "setAll", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private static Collection<KeyMapping> keyEx$setAll(Map<String, KeyMapping> map, Operation<Collection<KeyMapping>> original) {
		return KeyExManager.activeKeys();
	}

	@WrapOperation(method = "resetMapping", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private static Collection<KeyMapping> keyEx$resetMapping(Map<String, KeyMapping> map, Operation<Collection<KeyMapping>> original) {
		return original.call(map).stream()
				.filter($ -> ((KeyMappingEx) $).keyEx$isActive())
				.toList();
	}

	@Inject(method = {"click", "set"}, at = @At("HEAD"), order = 800)
	private static void keyEx$checkDirty(CallbackInfo ci) {
		KeyExManager.checkDirty();
	}

	@Inject(method = "resetMapping", at = @At("HEAD"))
	private static void keyEx$resetMapping(CallbackInfo ci) {
		KeyExManager.resetMapping(ALL);
	}
}
