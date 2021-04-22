package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.jade.VanillaPlugin;

public class ChestedHorseProvider implements IEntityComponentProvider {
	public static final ChestedHorseProvider INSTANCE = new ChestedHorseProvider();

	@Override
	public void append(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.HORSE_INVENTORY)) {
			return;
		}
		AbstractChestedHorseEntity horse = (AbstractChestedHorseEntity) accessor.getEntity();
		if (horse instanceof LlamaEntity) {
			tooltip.add(new TranslationTextComponent("jade.llamaStrength", ((LlamaEntity) horse).getStrength()));
		}
	}
}
