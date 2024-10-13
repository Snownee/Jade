package snownee.jade.mixin.conditional_key_mapping;

import java.util.Collection;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.KeyMapping;
import snownee.jade.conditional_key_mapping.ConditionalKeyMapping;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements ConditionalKeyMapping {
	@Shadow
	public abstract boolean isUnbound();

	@Unique
	private boolean enabled = true;

	@Override
	public boolean conditionalKeyMapping$isEnabled() {
		return enabled;
	}

	@Override
	public void conditionalKeyMapping$setEnabled(boolean enabled) {
		boolean changed = this.enabled != enabled;
		this.enabled = enabled;
		if (changed && !isUnbound()) {
			KeyMapping.resetMapping();
		}
	}

	@WrapOperation(
			method = {"setAll", "resetMapping"},
			at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private static Collection<KeyMapping> conditionalKeyMapping$filterDisabled(
			Map<String, KeyMapping> map,
			Operation<Collection<KeyMapping>> original) {
		return original.call(map).stream().filter($ -> ((ConditionalKeyMapping) $).conditionalKeyMapping$isEnabled()).toList();
	}
}
