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
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import snownee.jade.key_extension.KeyExManager;
import snownee.jade.key_extension.KeyMappingEx;
import snownee.jade.util.ClientProxy;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements KeyMappingEx {

	@Shadow
	@Final
	private static Map<String, KeyMapping> ALL;
	@Shadow
	private InputConstants.Key key;

	@Shadow
	public abstract boolean isUnbound();

	@Unique
	private boolean active = true;
	@Unique
	private boolean noConflict;

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
	public boolean keyEx$isNoConflict() {
		return noConflict || KeyExManager.isGlobalNoConflict();
	}

	@Override
	public void keyEx$setNoConflict(boolean noConflict) {
		boolean changed = this.noConflict != noConflict;
		this.noConflict = noConflict;
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
				.filter($ -> ((KeyMappingEx) $).keyEx$isActive() &&
						(!((KeyMappingEx) $).keyEx$isNoConflict() || !ClientProxy.noBuiltInNoKeyConflict()))
				.toList();
	}

	@Inject(method = {"click", "set"}, at = @At("HEAD"))
	private static void keyEx$checkDirty(CallbackInfo ci) {
		KeyExManager.checkDirty();
	}

	@Inject(method = "click", at = @At("TAIL"))
	private static void keyEx$click(InputConstants.Key key, CallbackInfo ci, @Local KeyMapping keyMapping) {
		KeyExManager.click(key, keyMapping);
	}

	@Inject(method = "set", at = @At("TAIL"))
	private static void keyEx$set(InputConstants.Key key, boolean bl, CallbackInfo ci, @Local KeyMapping keyMapping) {
		KeyExManager.set(key, bl, keyMapping);
	}

	@Inject(method = "resetMapping", at = @At("HEAD"))
	private static void keyEx$resetMapping(CallbackInfo ci) {
		KeyExManager.resetMapping(ALL);
	}
}
