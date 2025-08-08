package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.util.JadeMobEffectInstance;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements JadeMobEffectInstance {
	@Unique
	private long jade$updateTime;
	@Unique
	private long jade$addMs; // use millis here because I don't want the animation stopped when the game is paused

	@Override
	public long jade$updateTime() {
		return jade$updateTime;
	}

	@Override
	public void jade$setUpdateTime(long time) {
		this.jade$updateTime = time;
	}

	@Override
	public long jade$addMs() {
		return jade$addMs;
	}

	@Override
	public void jade$setAddMs(long ms) {
		this.jade$addMs = ms;
	}

	@Inject(method = "onEffectAdded", at = @At("HEAD"))
	private void jade$onEffectAdded(LivingEntity entity, CallbackInfo ci) {
		jade$setAddMs(System.currentTimeMillis());
		jade$setUpdateTime(entity.level().getGameTime());
	}

	@WrapMethod(method = "update")
	private boolean jade$update(MobEffectInstance that, Operation<Boolean> original) {
		boolean bl = original.call(that);
		long thatTime = ((JadeMobEffectInstance) that).jade$updateTime();
		if (bl && thatTime > jade$updateTime) {
			jade$updateTime = thatTime;
		}
		return bl;
	}
}
