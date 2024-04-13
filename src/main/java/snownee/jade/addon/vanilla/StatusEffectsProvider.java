package snownee.jade.addon.vanilla;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.ServerDataUtil;

public enum StatusEffectsProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	private static final MapCodec<List<MobEffectInstance>> EFFECTS_CODEC = MobEffectInstance.CODEC.listOf().fieldOf("mob_effects");

	public static Component getEffectName(MobEffectInstance mobEffectInstance) {
		MutableComponent mutableComponent = mobEffectInstance.getEffect().value().getDisplayName().copy();
		if (mobEffectInstance.getAmplifier() >= 1 && mobEffectInstance.getAmplifier() <= 9) {
			mutableComponent.append(CommonComponents.SPACE).append(Component.translatable(
					"enchantment.level." + (mobEffectInstance.getAmplifier() + 1)));
		}
		return mutableComponent;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Optional<List<MobEffectInstance>> result = ServerDataUtil.read(accessor.getServerData(), EFFECTS_CODEC);
		if (result.isEmpty() || result.get().isEmpty()) {
			return;
		}
		IElementHelper helper = IElementHelper.get();
		ITooltip box = helper.tooltip();
		for (var effect : result.get()) {
			Component name = getEffectName(effect);
			String duration;
			if (effect.isInfiniteDuration()) {
				duration = I18n.get("effect.duration.infinite");
			} else {
				duration = StringUtil.formatTickDuration(effect.getDuration(), accessor.tickRate());
			}
			MutableComponent s = Component.translatable("jade.potion", name, duration);
			IThemeHelper t = IThemeHelper.get();
			box.add(effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL ? t.danger(s) : t.success(s));
		}
		tooltip.add(helper.box(box, BoxStyle.getNestedBox()));
	}

	@Override
	public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
		LivingEntity living = (LivingEntity) accessor.getEntity();
		Collection<MobEffectInstance> effects = living.getActiveEffects();
		if (effects.isEmpty()) {
			return;
		}
		ServerDataUtil.write(accessor.getServerData(), EFFECTS_CODEC, List.copyOf(effects));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_POTION_EFFECTS;
	}
}
