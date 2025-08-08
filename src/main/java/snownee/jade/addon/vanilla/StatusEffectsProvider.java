package snownee.jade.addon.vanilla;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
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
import snownee.jade.api.ui.JadeUI;
import snownee.jade.util.JadeMobEffectInstance;

public class StatusEffectsProvider implements StreamServerDataProvider<EntityAccessor, List<StatusEffectsProvider.Effect>> {
	public static final StatusEffectsProvider INSTANCE = new StatusEffectsProvider();

	private static final StreamCodec<RegistryFriendlyByteBuf, List<Effect>> STREAM_CODEC = ByteBufCodecs.<RegistryFriendlyByteBuf, Effect>list()
			.apply(Effect.STREAM_CODEC);

	public static MutableComponent getEffectName(MobEffectInstance mobEffectInstance) {
		MutableComponent mutableComponent = mobEffectInstance.getEffect().value().getDisplayName().copy();
		if (mobEffectInstance.getAmplifier() >= 1 && mobEffectInstance.getAmplifier() <= 9) {
			mutableComponent.append(CommonComponents.SPACE).append(Component.translatable(
					"enchantment.level." + (mobEffectInstance.getAmplifier() + 1)));
		}
		return mutableComponent;
	}

	@Override
	public boolean shouldRequestData(EntityAccessor accessor) {
		return accessor.getEntity() instanceof LivingEntity;
	}

	@Override
	@Nullable
	public List<Effect> streamData(EntityAccessor accessor) {
		List<Effect> effects = ((LivingEntity) accessor.getEntity()).getActiveEffects()
				.stream()
				.filter(MobEffectInstance::isVisible)
				.map(Effect::new)
				.toList();
		return effects.isEmpty() ? null : effects;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, List<Effect>> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_POTION_EFFECTS;
	}

	public static class Client implements IEntityComponentProvider {
		public static final Client INSTANCE = new Client();
		public static final Component INFINITE = Component.translatable("effect.duration.infinite");

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			List<Effect> effects = StatusEffectsProvider.INSTANCE.decodeFromData(accessor).orElse(List.of());
			if (effects.isEmpty()) {
				return;
			}
			ITooltip box = JadeUI.tooltip();
			IThemeHelper t = IThemeHelper.get();
			long current = System.currentTimeMillis();
			effects = effects.stream().filter($ -> {
				long ms = current - $.addTime() - 20;
				return ms > 0;
			}).sorted().limit(config.getInt(JadeIds.MC_POTION_EFFECTS_LIMIT)).toList();
			float scale = effects.size() > 2 ? 0.75F : 1F;
			for (var data : effects) {
				long ms = current - data.addTime() - 20;
				float alpha = 1F;
				if (ms < 480) {
					alpha = ms / 480F;
				}
				MobEffectInstance effect = data.effect();
				Component name = getEffectName(effect);
				String duration;
				if (effect.isInfiniteDuration()) {
					duration = INFINITE.getString();
				} else {
					duration = StringUtil.formatTickDuration(effect.getDuration(), accessor.tickRate());
				}
				MutableComponent s = Component.translatable("jade.potion", name, duration);
				s = switch (effect.getEffect().value().getCategory()) {
					case BENEFICIAL -> t.success(s);
					case HARMFUL -> t.danger(s);
					case NEUTRAL -> t.info(s);
				};
				box.add(JadeUI.text(s).scale(scale).alpha(alpha));
			}
			tooltip.add(JadeUI.box(box, BoxStyle.nestedBox()).flexGrow(1));
		}

		@Override
		public ResourceLocation getUid() {
			return JadeIds.MC_POTION_EFFECTS;
		}
	}

	public record Effect(MobEffectInstance effect, long updateTime, long addTime) implements Comparable<Effect> {
		public static final StreamCodec<RegistryFriendlyByteBuf, Effect> STREAM_CODEC = StreamCodec.composite(
				MobEffectInstance.STREAM_CODEC,
				Effect::effect,
				ByteBufCodecs.LONG,
				Effect::updateTime,
				ByteBufCodecs.LONG,
				Effect::addTime,
				Effect::new);

		public Effect(MobEffectInstance effect) {
			this(effect, ((JadeMobEffectInstance) effect).jade$updateTime(), ((JadeMobEffectInstance) effect).jade$addMs());
		}

		@Override
		public int compareTo(Effect o) {
			int compared = Long.compare(updateTime, o.updateTime);
			if (compared != 0) {
				return -compared;
			}
			return effect.compareTo(o.effect);
		}
	}
}
