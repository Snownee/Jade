package snownee.jade;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.test.ExamplePlugin;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JadeCodecs;
import snownee.jade.util.JsonConfig;

public class Jade {
	public static final String ID = "jade";
	public static final String PROTOCOL_VERSION = "7";
	public static final Logger LOGGER = LogUtils.getLogger();
	private static final JsonConfig<WailaConfig.Root> rootConfig = new JsonConfig<>(
			Jade.ID + "/" + Jade.ID,
			WailaConfig.Root.CODEC,
			WailaConfig::fixData);
	private static List<JsonConfig<? extends WailaConfig>> configs = List.of();

	private static JsonConfig<? extends WailaConfig> configHolder() {
		WailaConfig.Root root = rootConfig();
		if (root.isEnableProfiles() && root.profileIndex > 0 && root.profileIndex < configs.size()) {
			return configs.get(root.profileIndex);
		}
		return rootConfig;
	}

	/**
	 * addons: Use {@link IWailaConfig#get()}
	 */
	public static WailaConfig config() {
		return configHolder().get();
	}

	public static void saveConfig() {
		configHolder().save();
		if (config() != rootConfig()) {
			rootConfig.save();
		}
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
		Preconditions.checkState(rootConfig.getFile().delete());
		invalidateConfig();
		history().themesHash = themesHash;
		rootConfig.save();
	}

	public static WailaConfig.Root rootConfig() {
		return rootConfig.get();
	}

	public static void loadComplete() {
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

		Set<ResourceLocation> extraKeys;
		if (CommonProxy.isPhysicallyClient()) {
			extraKeys = WailaClientRegistration.instance().getConfigKeys();
		} else {
			extraKeys = Set.of();
		}
		WailaCommonRegistration.instance().priorities.sort(extraKeys);
		WailaCommonRegistration.instance().loadComplete();
		if (CommonProxy.isPhysicallyClient()) {
			WailaClientRegistration.instance().loadComplete();

			Codec<WailaConfig> codec = WailaConfig.MAP_CODEC.codec();
			ImmutableList.Builder<JsonConfig<? extends WailaConfig>> list = ImmutableList.builderWithExpectedSize(4);
			list.add(rootConfig);
			Supplier<WailaConfig> defaultFactory = () -> JadeCodecs.createFromEmptyMap(codec);
			for (int i = 1; i < 4; ++i) {
				Supplier<WailaConfig> factory = defaultFactory;
				if (i == 1) {
					factory = () -> {
						WailaConfig config = defaultFactory.get();
						config.setName("@jade.profile_preset.accessibility");
						config.accessibility().setEnableAccessibilityPlugin(true);
						config.overlay().setAnimation(false);
						config.overlay().setAlpha(1);
						config.plugin().set(JadeIds.CORE_BLOCK_FACE, true);
						config.plugin().set(JadeIds.CORE_MOD_NAME, false);
						return config;
					};
				} else if (i == 2) {
					factory = () -> {
						WailaConfig config = defaultFactory.get();
						config.setName("@jade.profile_preset.minimalism");
						config.general().setDisplayMode(IWailaConfig.DisplayMode.LITE);
						config.general().setBossBarOverlapMode(IWailaConfig.BossBarOverlapMode.HIDE_TOOLTIP);
						config.overlay().setAlpha(0);
						config.overlay().setSquare(true);
						config.overlay().setIconMode(IWailaConfig.IconMode.INLINE);
						config.plugin().set(JadeIds.MC_BREAKING_PROGRESS, false);
						config.plugin().set(JadeIds.MC_HARVEST_TOOL, false);
						return config;
					};
				}
				list.add(new JsonConfig<>("%s/profiles/%s/%s".formatted(Jade.ID, i, Jade.ID), codec, WailaConfig::fixData, factory));
			}
			configs = list.build();
			rootConfig().history.checkNewUser(CommonProxy.getConfigDirectory().getAbsolutePath().hashCode());
			rootConfig().fixData();
			WailaConfig.init();
			for (JsonConfig<? extends WailaConfig> config : configs) {
				config.save();
			}
			JadeClient.refreshKeyState();
		}
	}

	public static List<JsonConfig<? extends WailaConfig>> configs() {
		return configs;
	}

	public static void useProfile(int index) {
		rootConfig().setEnableProfiles(true);
		rootConfig().profileIndex = index;
		rootConfig.save();
	}

	public static void saveProfile(int index) {
		JsonConfig<? extends WailaConfig> dest = configs().get(index);
		configHolder().saveTo(dest.getFile());
		dest.invalidate();
	}

	public static void loadPlugins() {
		Set<String> erroneousClasses = Sets.newHashSet();
		CommonProxy.loadPlugins(erroneousClasses, Set.of());
		if (!erroneousClasses.isEmpty()) {
			LOGGER.info("Trying to load plugins again without erroneous plugins");
			CommonProxy.loadPlugins(erroneousClasses, erroneousClasses);
		}
		loadComplete();
	}
}
