//package snownee.jade;
//
//import mcp.mobius.waila.api.IRegistrar;
//import mcp.mobius.waila.api.IWailaPlugin;
//import mcp.mobius.waila.api.WailaPlugin;
//import net.minecraft.client.Minecraft;
//import net.minecraft.resources.IReloadableResourceManager;
//import net.minecraftforge.fml.loading.FMLEnvironment;
//import snownee.jade.addon.vanilla.HarvestToolProvider;
//
//@WailaPlugin
//public class JadeClientPlugin implements IWailaPlugin {
//
//    @Override
//    public void register(IRegistrar registrar) {
//        if (FMLEnvironment.dist.isClient()) {
//            ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(HarvestToolProvider.INSTANCE);
//        }
//    }
//
//}
