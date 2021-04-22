package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.util.text.StringTextComponent;
import snownee.jade.VanillaPlugin;

public class PaintingProvider implements IEntityComponentProvider {
	public static final PaintingProvider INSTANCE = new PaintingProvider();

	@Override
	public void append(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.PAINTING)) {
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
