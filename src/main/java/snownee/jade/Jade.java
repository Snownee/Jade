package snownee.jade;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Jade.MODID, name = Jade.NAME, version = "@VERSION_INJECT@")
public class Jade
{
    public static final String MODID = "jade";
    public static final String NAME = "Jade";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }
}
