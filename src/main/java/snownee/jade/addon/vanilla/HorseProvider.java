package snownee.jade.addon.vanilla;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.overlay.DisplayHelper;

public class HorseProvider implements IEntityComponentProvider {
	public static final HorseProvider INSTANCE = new HorseProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.HORSE_STAT)) {
			return;
		}
		AbstractHorse horse = (AbstractHorse) accessor.getEntity();
		double jumpStrength = horse.getAttributeValue(Attributes.JUMP_STRENGTH);
		double jumpHeight = -0.1817584952 * jumpStrength * jumpStrength * jumpStrength + 3.689713992 * jumpStrength * jumpStrength + 2.128599134 * jumpStrength - 0.343930367;
		// https://minecraft.fandom.com/wiki/Horse?so=search#Movement_speed
		double speed = horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * 43.17;
		tooltip.add(new TranslatableComponent("jade.horseStat.jump", DisplayHelper.dfCommas.format(jumpHeight)));
		tooltip.add(new TranslatableComponent("jade.horseStat.speed", DisplayHelper.dfCommas.format(speed)));
	}
}
