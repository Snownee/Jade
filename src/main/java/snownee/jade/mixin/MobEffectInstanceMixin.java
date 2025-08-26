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
	private long jade$addTime;

	@Override
	public long jade$updateTime() {
		return jade$updateTime;
	}

	@Override
	public void jade$setUpdateTime(long time) {
		this.jade$updateTime = time;
	}

	@Override
	public long jade$addTime() {
		return jade$addTime;
	}

	@Override
	public void jade$setAddTime(long time) {
		this.jade$addTime = time;
	}

	@Inject(method = "onEffectAdded", at = @At("HEAD"))
	private void jade$onEffectAdded(LivingEntity entity, CallbackInfo ci) {
		long time = System.currentTimeMillis();
		jade$setAddTime(time);
		jade$setUpdateTime(time);
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
