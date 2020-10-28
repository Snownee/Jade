package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.util.text.ITextComponent;
import snownee.jade.JadePlugin;

public class HarvestToolProvider implements IComponentProvider {

    public static final HarvestToolProvider INSTANCE = new HarvestToolProvider();

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if (!config.get(JadePlugin.HARVEST_TOOL)) {
            return;
        }
        // TODO
    }

}
