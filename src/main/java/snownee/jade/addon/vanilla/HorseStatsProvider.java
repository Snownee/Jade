package snownee.jade.addon.vanilla;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.overlay.DisplayHelper;

public enum HorseStatsProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		AbstractHorse horse = (AbstractHorse) accessor.getEntity();
		if (horse instanceof AbstractChestedHorse) {
			if (horse instanceof Llama llama) {
				tooltip.add(new TranslatableComponent("jade.llamaStrength", llama.getStrength()));
			}
			return;
		}
		double jumpStrength = horse.getCustomJump();
		double jumpHeight = -0.1817584952 * jumpStrength * jumpStrength * jumpStrength + 3.689713992 * jumpStrength * jumpStrength + 2.128599134 * jumpStrength - 0.343930367;
		// https://minecraft.fandom.com/wiki/Horse?so=search#Movement_speed
		double speed = horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * 43.17;
		tooltip.add(new TranslatableComponent("jade.horseStat.jump", DisplayHelper.dfCommas.format(jumpHeight)));
		tooltip.add(new TranslatableComponent("jade.horseStat.speed", DisplayHelper.dfCommas.format(speed)));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_HORSE_STATS;
	}
}
