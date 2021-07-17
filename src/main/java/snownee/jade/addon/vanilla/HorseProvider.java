package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.jade.VanillaPlugin;

public class HorseProvider implements IEntityComponentProvider {
	public static final HorseProvider INSTANCE = new HorseProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.HORSE_STAT)) {
			return;
		}
		AbstractHorseEntity horse = (AbstractHorseEntity) accessor.getEntity();
		double jumpStrength = horse.getHorseJumpStrength();
		double jumpHeight = -0.1817584952 * jumpStrength * jumpStrength * jumpStrength + 3.689713992 * jumpStrength * jumpStrength + 2.128599134 * jumpStrength - 0.343930367;
		ModifiableAttributeInstance iattributeinstance = horse.getAttribute(Attributes.MOVEMENT_SPEED);
		tooltip.add(new TranslationTextComponent("jade.horseStat.jump", DisplayHelper.dfCommas.format(jumpHeight)));
		tooltip.add(new TranslationTextComponent("jade.horseStat.speed", DisplayHelper.dfCommas.format(iattributeinstance.getValue())));
	}
}
