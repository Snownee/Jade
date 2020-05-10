package snownee.jade;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Jade.MODID)
public class Jade {
    public static final String MODID = "jade";

    public Jade() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, JadeCommonConfig.spec);
        FMLJavaModLoadingContext.get().getModEventBus().register(JadeCommonConfig.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
    }

    private void init(FMLCommonSetupEvent event) {
        JadeCommonConfig.refresh();
    }
}
