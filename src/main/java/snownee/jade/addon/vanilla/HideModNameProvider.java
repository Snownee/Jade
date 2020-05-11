package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.util.text.ITextComponent;
import snownee.jade.JadePlugin;

public class HideModNameProvider implements IComponentProvider {

    public static final HideModNameProvider INSTANCE = new HideModNameProvider();

    @Override
    public void appendTail(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if (config.get(JadePlugin.HIDE_MOD_NAME)) {
            tooltip.clear();
        }
    }

}
