package snownee.jade;

import com.google.common.collect.Sets;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import snownee.jade.providers.ItemHandlerProvider;

@EventBusSubscriber
@Config(modid = Jade.MODID)
public class ModConfig
{
    @Config.RangeInt(min = 0)
    public static int inventorySneakShowAmount = 54;
    @Config.RangeInt(min = 0)
    public static int inventoryNormalShowAmount = 0;
    @Config.RangeInt(min = 1)
    public static int inventoryShowItemPreLine = 9;
    public static String[] inventoryShowBlacklist = new String[] { "refinedstorage:disk_drive" };

    @SubscribeEvent
    public static void onConfigReload(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(Jade.MODID))
        {
            ConfigManager.sync(Jade.MODID, Config.Type.INSTANCE);
            ItemHandlerProvider.INVENTORY_IGNORE = Sets.newHashSet(inventoryShowBlacklist);
        }
    }
}
