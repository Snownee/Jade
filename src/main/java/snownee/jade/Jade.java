package snownee.jade;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.test.ExamplePlugin;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JsonConfig;

public class Jade {
	public static final String MODID = "jade";
	public static final Logger LOGGER = LogUtils.getLogger();
	/**
	 * addons: Use {@link IWailaConfig#get()}
	 */
	public static final JsonConfig<WailaConfig> CONFIG = new JsonConfig<>(Jade.MODID + "/" + Jade.MODID, WailaConfig.class, null);
	public static int MAX_DISTANCE_SQR = 900;
	public static boolean FROZEN;

	public static void loadComplete() {
		if (FROZEN) {
			return;
		}
		FROZEN = true;
		if (CommonProxy.isDevEnv()) {
			try {
				IWailaPlugin plugin = new ExamplePlugin();
				plugin.register(WailaCommonRegistration.instance());
				if (CommonProxy.isPhysicallyClient()) {
					plugin.registerClient(WailaClientRegistration.instance());
				}
			} catch (Throwable e) {
				// NO-OP
			}
		}

		WailaCommonRegistration.instance().priorities.sort(PluginConfig.INSTANCE.getKeys());
		WailaCommonRegistration.instance().loadComplete();
		if (CommonProxy.isPhysicallyClient()) {
			WailaClientRegistration.instance().loadComplete();
			ConfigGeneral.init();
		}
		PluginConfig.INSTANCE.reload();
	}
}
