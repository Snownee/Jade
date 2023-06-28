package snownee.jade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.GsonBuilder;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.Theme;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.test.ExamplePlugin;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JsonConfig;
import snownee.jade.util.ThemeSerializer;

public class Jade {
	public static final String MODID = "jade";
	public static final Logger LOGGER = LogManager.getLogger("Jade");
	public static int MAX_DISTANCE_SQR = 900;

	public static void loadComplete() {
		if (CommonProxy.isDevEnv()) {
			try {
				IWailaPlugin plugin = new ExamplePlugin();
				plugin.register(WailaCommonRegistration.INSTANCE);
				if (CommonProxy.isPhysicallyClient()) {
					plugin.registerClient(WailaClientRegistration.INSTANCE);
				}
			} catch (Throwable e) {
				// NO-OP
			}
		}

		WailaCommonRegistration.INSTANCE.priorities.sort(PluginConfig.INSTANCE.getKeys());
		WailaCommonRegistration.INSTANCE.loadComplete();
		if (CommonProxy.isPhysicallyClient()) {
			WailaClientRegistration.INSTANCE.loadComplete();
			ConfigGeneral.init();
		}
		PluginConfig.INSTANCE.reload();
	}


	/**
	 * addons: Use {@link IWailaConfig#get()}
	 */
	/* off */
	public static final JsonConfig<WailaConfig> CONFIG =
			new JsonConfig<>(Jade.MODID + "/" + Jade.MODID, WailaConfig.class, OverlayRenderer::updateTheme).withGson(
					new GsonBuilder()
							.setPrettyPrinting()
							.enableComplexMapKeySerialization()
							.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
							.registerTypeAdapter(Theme.class, new ThemeSerializer())
							.setLenient()
							.create()
			);
	/* on */


}
