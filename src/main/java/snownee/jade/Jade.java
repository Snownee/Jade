package snownee.jade;

import org.apache.commons.lang3.tuple.Pair;

import mcp.mobius.waila.gui.HomeConfigScreen;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;

@Mod(Jade.MODID)
public class Jade {
	public static final String MODID = "jade";
	public static final String NAME = "Jade";
	public static final Vector2f VERTICAL_OFFSET = new Vector2f(0, 2);

	public Jade() {
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, JadeCommonConfig.spec, "jade-addons.toml");
		FMLJavaModLoadingContext.get().getModEventBus().register(JadeCommonConfig.class);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
		if (FMLEnvironment.dist.isClient()) {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
		}
	}

	private void init(FMLCommonSetupEvent event) {
		JadeCommonConfig.refresh();
	}

	@OnlyIn(Dist.CLIENT)
	private void clientInit(FMLClientSetupEvent event) {
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> ((minecraft, screen) -> new HomeConfigScreen(screen)));
	}
}
