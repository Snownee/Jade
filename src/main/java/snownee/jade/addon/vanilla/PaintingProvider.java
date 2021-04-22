package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import snownee.jade.JadePlugin;

public class PaintingProvider implements IEntityComponentProvider {
	public static final PaintingProvider INSTANCE = new PaintingProvider();

	@Override
	public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.PAINTING)) {
			return;
		}
		PaintingEntity painting = (PaintingEntity) accessor.getEntity();
		if (painting.art == null) {
			return;
		}
		String name = painting.art.getRegistryName().getPath().replace('_', ' ');
		tooltip.add(new StringTextComponent(name));
	}

}
