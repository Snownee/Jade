package snownee.jade.addon.vanilla;

import java.util.Collection;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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

public enum PotionEffectsProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	public static String getPotionDurationString(int duration) {
		if (duration >= 32767) {
			return "**:**";
		} else {
			int i = Mth.floor(duration);
			return ticksToElapsedTime(i);
		}
	}

	public static String ticksToElapsedTime(int ticks) {
		int i = ticks / 20;
		int j = i / 60;
		i = i % 60;
		return i < 10 ? j + ":0" + i : j + ":" + i;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("Potions")) {
			return;
		}
		IElementHelper helper = IElementHelper.get();
		ITooltip box = helper.tooltip();
		ListTag list = accessor.getServerData().getList("Potions", Tag.TAG_COMPOUND);
		Component[] lines = new Component[list.size()];
		for (int i = 0; i < lines.length; i++) {
			CompoundTag compound = list.getCompound(i);
			int duration = compound.getInt("Duration");
			MutableComponent name = Component.translatable(compound.getString("Name"));
			String amplifierKey = "potion.potency." + compound.getInt("Amplifier");
			Component amplifier;
			if (I18n.exists(amplifierKey)) {
				amplifier = Component.translatable(amplifierKey);
			} else {
				amplifier = Component.literal(Integer.toString(compound.getInt("Amplifier")));
			}
			MutableComponent s = Component.translatable("jade.potion", name, amplifier, getPotionDurationString(duration));
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
			compound.putString("Name", effect.getDescriptionId());
			compound.putInt("Amplifier", effect.getAmplifier());
			int duration = Math.min(32767, effect.getDuration());
			compound.putInt("Duration", duration);
			compound.putBoolean("Bad", effect.getEffect().getCategory() == MobEffectCategory.HARMFUL);
			list.add(compound);
		}
		tag.put("Potions", list);
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_POTION_EFFECTS;
	}
}
