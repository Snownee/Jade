package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;

public class PaintingProvider implements IEntityComponentProvider {
	public static final PaintingProvider INSTANCE = new PaintingProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.PAINTING)) {
			return;
		}
		Painting painting = (Painting) accessor.getEntity();
		if (painting.motive == null) {
			return;
		}
		String name = painting.motive.getRegistryName().getPath().replace('_', ' ');
		tooltip.add(new TextComponent(name));
	}
}
