package snownee.jade.addon.vanilla;

import java.util.Collection;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

public enum StatusEffectsProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	public static Component getEffectName(MobEffectInstance mobEffectInstance) {
		MutableComponent mutableComponent = mobEffectInstance.getEffect().getDisplayName().copy();
		if (mobEffectInstance.getAmplifier() >= 1 && mobEffectInstance.getAmplifier() <= 9) {
			mutableComponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (mobEffectInstance.getAmplifier() + 1)));
		}
		return mutableComponent;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("StatusEffects")) {
			return;
		}
		IElementHelper helper = IElementHelper.get();
		ITooltip box = helper.tooltip();
		ListTag list = accessor.getServerData().getList("StatusEffects", Tag.TAG_COMPOUND);
		Component[] lines = new Component[list.size()];
		for (int i = 0; i < lines.length; i++) {
			CompoundTag compound = list.getCompound(i);
			MutableComponent name = Component.Serializer.fromJsonLenient(compound.getString("Name"));
			if (name == null) {
				continue;
			}
			String duration;
			if (compound.getBoolean("Infinite")) {
				duration = I18n.get("effect.duration.infinite");
			} else {
				duration = StringUtil.formatTickDuration(compound.getInt("Duration"));
			}
			MutableComponent s = Component.translatable("jade.potion", name, duration);
			IThemeHelper t = IThemeHelper.get();
			box.add(compound.getBoolean("Bad") ? t.danger(s) : t.success(s));
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
		ListTag list = new ListTag();
		for (MobEffectInstance effect : effects) {
			CompoundTag compound = new CompoundTag();
			compound.putString("Name", Component.Serializer.toJson(getEffectName(effect)));
			if (effect.isInfiniteDuration()) {
				compound.putBoolean("Infinite", true);
			} else {
				compound.putInt("Duration", effect.getDuration());
			}
			compound.putBoolean("Bad", effect.getEffect().getCategory() == MobEffectCategory.HARMFUL);
			list.add(compound);
		}
		tag.put("StatusEffects", list);
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_POTION_EFFECTS;
	}
}
