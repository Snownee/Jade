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
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.test.ExamplePlugin;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JsonConfig;

public class Jade {
	public static final String ID = "jade";
	public static final Logger LOGGER = LogUtils.getLogger();
	private static final JsonConfig<WailaConfig.Root> config = new JsonConfig<>(Jade.ID + "/" + Jade.ID, WailaConfig.Root.CODEC, null);
	private static List<JsonConfig<? extends WailaConfig>> configs = List.of();
	private static boolean frozen;

	/**
	 * addons: Use {@link IWailaConfig#get()}
	 */
	public static WailaConfig config() {
		WailaConfig.Root root = config.get();
		if (root.isEnableProfiles() && root.profileIndex > 0 && root.profileIndex < configs.size()) {
			return configs.get(root.profileIndex).get();
		}
		return root;
	}

	public static WailaConfig.History history() {
		return config.get().history;
	}

	public static void saveConfig() {
		config.save();
	}

	public static void invalidateConfig() {
		config.invalidate();
	}

	public static void resetConfig() {
		int themesHash = history().themesHash;
		Preconditions.checkState(config.getFile().delete());
		Preconditions.checkState(PluginConfig.INSTANCE.getFile().delete());
		invalidateConfig();
		history().themesHash = themesHash;
		saveConfig();
		PluginConfig.INSTANCE.reload();
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

		WailaCommonRegistration.instance().priorities.sort(PluginConfig.INSTANCE.getKeys());
		WailaCommonRegistration.instance().loadComplete();
		if (CommonProxy.isPhysicallyClient()) {
			WailaClientRegistration.instance().loadComplete();

			Codec<WailaConfig> codec = WailaConfig.MAP_CODEC.codec();
			ImmutableList.Builder<JsonConfig<? extends WailaConfig>> list = ImmutableList.builderWithExpectedSize(4);
			list.add(config);
			for (int i = 1; i < 4; ++i) {
				list.add(new JsonConfig<>("%s/profiles/%s/%s".formatted(Jade.ID, i, Jade.ID), codec, null));
			}
			configs = list.build();

			WailaConfig.init();
		}
		PluginConfig.INSTANCE.reload();
	}
}
