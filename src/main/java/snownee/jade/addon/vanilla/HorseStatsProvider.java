package snownee.jade.addon.vanilla;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.overlay.DisplayHelper;

public enum HorseStatsProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		AbstractHorse horse = (AbstractHorse) accessor.getEntity();
		IThemeHelper t = IThemeHelper.get();
		if (horse instanceof Llama llama) {
			tooltip.add(Component.translatable("jade.llamaStrength", t.info(llama.getStrength())));
			return;
		}
		if (horse instanceof Camel) {
			return;
		}
		double jumpStrength = horse.getCustomJump();
		double jumpHeight = -0.1817584952 * jumpStrength * jumpStrength * jumpStrength + 3.689713992 * jumpStrength * jumpStrength + 2.128599134 * jumpStrength - 0.343930367;
		// https://minecraft.fandom.com/wiki/Horse?so=search#Movement_speed
		double speed = horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * 42.16;
		tooltip.add(Component.translatable("jade.horseStat.jump", t.info(DisplayHelper.dfCommas.format(jumpHeight))));
		tooltip.add(Component.translatable("jade.horseStat.speed", t.info(DisplayHelper.dfCommas.format(speed))));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_HORSE_STATS;
	}
}
