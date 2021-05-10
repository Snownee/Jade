package snownee.jade;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.jade.addon.vanilla.HarvestToolProvider;
import snownee.jade.client.renderer.BoxTooltipRenderer;
import snownee.jade.client.renderer.StringTooltipRenderer;
import snownee.jade.client.renderer.SubStringTooltipRenderer;

@WailaPlugin
public class JadeClientPlugin implements IWailaPlugin {

	@Override
	public void register(IRegistrar registrar) {
		if (FMLEnvironment.dist.isClient()) {
			((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(HarvestToolProvider.INSTANCE);
			registrar.registerTooltipRenderer(Renderables.BORDER, new BoxTooltipRenderer());
			registrar.registerTooltipRenderer(Renderables.OFFSET_TEXT, new StringTooltipRenderer());
			registrar.registerTooltipRenderer(Renderables.SUB, new SubStringTooltipRenderer());
			HarvestToolProvider.init();
		}
	}

}
