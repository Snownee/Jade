package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.jade.JadePlugin;

public class ChestedHorseProvider implements IEntityComponentProvider {
    public static final ChestedHorseProvider INSTANCE = new ChestedHorseProvider();

    @Override
    public void append(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        if (!config.get(JadePlugin.HORSE_INVENTORY)) {
            return;
        }
        AbstractChestedHorseEntity horse = (AbstractChestedHorseEntity) accessor.getEntity();
        if (horse instanceof LlamaEntity) {
            tooltip.add(new TranslationTextComponent("jade.llamaStrength", ((LlamaEntity) horse).getStrength()));
        }
    }
}
