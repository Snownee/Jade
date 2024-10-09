package snownee.jade;

import java.util.List;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;

import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.test.ExamplePlugin;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JsonConfig;

public class Jade {
	public static final String ID = "jade";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String SERVER_FILE = Jade.ID + "/server-plugin-overrides.json";
	private static final JsonConfig<WailaConfig.Root> config = new JsonConfig<>(
			Jade.ID + "/" + Jade.ID,
			WailaConfig.Root.CODEC,
			WailaConfig::fixData);
	private static List<JsonConfig<? extends WailaConfig>> configs = List.of();
	private static boolean frozen;

	private static JsonConfig<? extends WailaConfig> configHolder() {
		WailaConfig.Root root = rootConfig();
		if (root.isEnableProfiles() && root.profileIndex > 0 && root.profileIndex < configs.size()) {
			return configs.get(root.profileIndex);
		}
		return config;
	}

	/**
	 * addons: Use {@link IWailaConfig#get()}
	 */
	public static WailaConfig config() {
		return configHolder().get();
	}

	public static void saveConfig() {
		configHolder().save();
	}

	public static void invalidateConfig() {
		configHolder().invalidate();
	}

	public static WailaConfig.History history() {
		return rootConfig().history;
	}

	public static void resetConfig() {
		rootConfig().setEnableProfiles(false);
		int themesHash = history().themesHash;
		Preconditions.checkState(config.getFile().delete());
		invalidateConfig();
		history().themesHash = themesHash;
		saveConfig();
		//FIXME reapply server config
//		PluginConfig.INSTANCE.reload();
	}

	public static WailaConfig.Root rootConfig() {
		return config.get();
	}

	public static void loadComplete() {
		if (frozen) {
			return;
		}
		frozen = true;
		if (CommonProxy.isDevEnv()) {
			try {
				IWailaPlugin plugin = new ExamplePlugin();
				plugin.register(WailaCommonRegistration.instance());
				if (CommonProxy.isPhysicallyClient()) {
					plugin.registerClient(WailaClientRegistration.instance());
				}
			} catch (Throwable ignored) {
			}
		}

		WailaCommonRegistration.instance().priorities.sort(WailaClientRegistration.instance().getConfigKeys());
		WailaCommonRegistration.instance().loadComplete();
		if (CommonProxy.isPhysicallyClient()) {
			WailaClientRegistration.instance().loadComplete();

			Codec<WailaConfig> codec = WailaConfig.MAP_CODEC.codec();
			ImmutableList.Builder<JsonConfig<? extends WailaConfig>> list = ImmutableList.builderWithExpectedSize(4);
			list.add(config);
			for (int i = 1; i < 4; ++i) {
				list.add(new JsonConfig<>("%s/profiles/%s/%s".formatted(Jade.ID, i, Jade.ID), codec, WailaConfig::fixData));
			}
			configs = list.build();
			rootConfig().fixData();
			for (JsonConfig<? extends WailaConfig> config : configs) {
				config.save();
			}
			WailaConfig.init();
		}
	}

	public static List<JsonConfig<? extends WailaConfig>> configs() {
		return configs;
	}
}
