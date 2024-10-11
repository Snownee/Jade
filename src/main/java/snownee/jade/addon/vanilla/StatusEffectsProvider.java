package snownee.jade.addon.vanilla;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public enum StatusEffectsProvider implements IEntityComponentProvider, StreamServerDataProvider<EntityAccessor, List<MobEffectInstance>> {

	INSTANCE;

	private static final StreamCodec<RegistryFriendlyByteBuf, List<MobEffectInstance>> STREAM_CODEC = ByteBufCodecs.<RegistryFriendlyByteBuf, MobEffectInstance>list()
			.apply(MobEffectInstance.STREAM_CODEC);

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
		List<MobEffectInstance> effects = decodeFromData(accessor).orElse(List.of());
		if (effects.isEmpty()) {
			return;
		}
		IElementHelper helper = IElementHelper.get();
		ITooltip box = helper.tooltip();
		for (var effect : effects) {
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
	public boolean shouldRequestData(EntityAccessor accessor) {
		return accessor.getEntity() instanceof LivingEntity;
	}

	@Override
	@Nullable
	public List<MobEffectInstance> streamData(EntityAccessor accessor) {
		List<MobEffectInstance> effects = ((LivingEntity) accessor.getEntity()).getActiveEffects()
				.stream()
				.filter(MobEffectInstance::isVisible)
				.toList();
		return effects.isEmpty() ? null : effects;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, List<MobEffectInstance>> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_POTION_EFFECTS;
	}
}
