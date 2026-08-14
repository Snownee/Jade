package snownee.jade;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import snownee.jade.addon.core.ModNameProvider;
import snownee.jade.addon.harvest.LootTableMineableCollector;
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
	public static final String PROTOCOL_VERSION = "9";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final Set<String> DISABLED_PLUGINS = Sets.newHashSet();
	private static @Nullable GameRule<Integer> MAX_POSITION_DEVIATION;
	private static final Supplier<JsonConfig<WailaConfig.Root>> rootConfig = Suppliers.memoize(() -> new JsonConfig<>(
			ID + "/" + ID,
			WailaConfig.Root.CODEC,
			WailaConfig::fixData));
	private static List<JsonConfig<? extends WailaConfig>> configs = List.of();

	private static JsonConfig<? extends WailaConfig> configHolder() {
		WailaConfig.Root root = rootConfig();
		if (root.isEnableProfiles() && root.profileIndex > 0 && root.profileIndex < configs.size()) {
			return configs.get(root.profileIndex);
		}
		return rootConfig.get();
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
			rootConfig.get().save();
		}
	}

	public static void invalidateConfig() {
		configHolder().invalidate();
	}

	public static WailaConfig.History history() {
		return rootConfig().history;
	}

	public static void resetConfig() {
		int themesHash = history().themesHash;
		Preconditions.checkState(configHolder().getFile().delete());
		invalidateConfig();
		history().themesHash = themesHash;
		configHolder().save();
	}

	public static WailaConfig.Root rootConfig() {
		return rootConfig.get().get();
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

		Set<Identifier> extraKeys;
		if (CommonProxy.isPhysicallyClient()) {
			extraKeys = WailaClientRegistration.instance().getConfigKeys();
		} else {
			extraKeys = Set.of();
		}
		WailaCommonRegistration.instance().priorities.sort(extraKeys);
		WailaCommonRegistration.instance().loadComplete();
		CommonProxy.registerTagsUpdatedListener((provider, bl) -> WailaCommonRegistration.instance().reloadOperations(provider));
		if (CommonProxy.isPhysicallyClient()) {
			WailaClientRegistration.instance().loadComplete();

			Codec<WailaConfig> codec = WailaConfig.MAP_CODEC.codec();
			ImmutableList.Builder<JsonConfig<? extends WailaConfig>> list = ImmutableList.builderWithExpectedSize(4);
			list.add(rootConfig.get());
			Supplier<WailaConfig> defaultFactory = () -> JadeCodecs.createFromEmptyMap(codec);
			for (int i = 1; i < 4; ++i) {
				Supplier<WailaConfig> factory = getProfilePreset(defaultFactory, i);
				list.add(new JsonConfig<>("%s/profiles/%s/%s".formatted(ID, i, ID), codec, WailaConfig::fixData, factory));
			}
			configs = list.build();
			rootConfig().history.checkNewUser(CommonProxy.getConfigDirectory().getAbsolutePath().hashCode());
			rootConfig().fixData();
			WailaConfig.init();
			for (JsonConfig<? extends WailaConfig> config : configs) {
				config.save();
			}
			JadeClient.refreshKeyState();
		} else {
			CommonProxy.registerTagsUpdatedListener(LootTableMineableCollector::onTagsUpdated);
		}
	}

	private static Supplier<WailaConfig> getProfilePreset(Supplier<WailaConfig> defaultFactory, int i) {
		Supplier<WailaConfig> factory = defaultFactory;
		if (i == 1) {
			factory = () -> {
				WailaConfig config = defaultFactory.get();
				config.setName("@jade.profile_preset.accessibility");
				config.accessibility().setEnableAccessibilityPlugin(true);
				config.overlay().setAnimation(false);
				config.overlay().setAlpha(1);
				config.plugin().set(JadeIds.CORE_BLOCK_FACE, true);
				config.plugin().set(JadeIds.CORE_MOD_NAME, ModNameProvider.Mode.OFF);
				return config;
			};
		} else if (i == 2) {
			factory = () -> {
				WailaConfig config = defaultFactory.get();
				config.setName("@jade.profile_preset.minimalism");
				config.general().setDisplayMode(IWailaConfig.DisplayMode.LITE);
				config.general().setBossBarOverlapMode(IWailaConfig.BossBarOverlapMode.HIDE_TOOLTIP);
				config.overlay().setAlpha(0);
				config.overlay().activeTheme = JadeIds.JADE("dark/slim");
				config.overlay().setIconMode(IWailaConfig.IconMode.INLINE);
				config.plugin().set(JadeIds.MC_BREAKING_PROGRESS, false);
				config.plugin().set(JadeIds.MC_HARVEST_TOOL, false);
				config.plugin().set(JadeIds.MC_ITEM_TOOLTIP, false);
				return config;
			};
		}
		return factory;
	}

	public static List<JsonConfig<? extends WailaConfig>> configs() {
		return configs;
	}

	public static void useProfile(int index) {
		rootConfig().setEnableProfiles(true);
		rootConfig().profileIndex = index;
		rootConfig.get().save();
		JadeClient.refreshKeyState();
	}

	public static void saveProfile(int index) {
		JsonConfig<? extends WailaConfig> dest = configs().get(index);
		configHolder().saveTo(dest.getFile());
		dest.invalidate();
	}

	public static void loadPlugins() {
		List<CommonProxy.Entrypoint> entrypoints = CommonProxy.loadEntrypoints();
		Set<String> disabledClasses = DISABLED_PLUGINS;
		JsonConfig<List<String>> config = new JsonConfig<>(
				ID + "/disabled_plugins",
				ExtraCodecs.NON_EMPTY_STRING.listOf().optionalFieldOf("values", List.of()).codec(),
				null,
				List::of);
		if (config.getFile().exists() && !config.get().isEmpty()) {
			disabledClasses = Sets.newHashSet(disabledClasses);
			disabledClasses.addAll(config.get());
		}
		Set<String> erroneousClasses = Sets.newHashSet();
		loadPlugins(entrypoints, disabledClasses, erroneousClasses, Set.of());
		if (!erroneousClasses.isEmpty()) {
			LOGGER.info("Trying to load plugins again without erroneous plugins");
			loadPlugins(entrypoints, disabledClasses, erroneousClasses, erroneousClasses);
		}
		loadComplete();
	}

	private static void loadPlugins(
			List<CommonProxy.Entrypoint> entrypoints,
			Set<String> disabledClasses,
			Set<String> erroneousClasses,
			Set<String> excludedClasses) {
		WailaCommonRegistration.reset();
		if (CommonProxy.isPhysicallyClient()) {
			WailaClientRegistration.reset();
		}
		Set<String> classes = Sets.newHashSet();
		Stopwatch stopwatch = null;
		if (CommonProxy.isDevEnv()) {
			stopwatch = Stopwatch.createUnstarted();
		}
		for (CommonProxy.Entrypoint entrypoint : entrypoints) {
			String className = entrypoint.className();
			try {
				if (disabledClasses.contains(className)) {
					LOGGER.info("Skipping disabled plugin: %s".formatted(className));
					continue;
				}
				if (excludedClasses.contains(className)) {
					continue;
				}
				if (className.startsWith("snownee.jade.") && !entrypoint.modId().equals(ID)) {
					entrypoint.throwError("Built-in plugin registered by non-Jade mod");
				}
				String requiredMod = entrypoint.requiredMod();
				if (!requiredMod.isEmpty() && !CommonProxy.isModLoaded(requiredMod)) {
					continue;
				}
				if (!classes.add(className)) {
					entrypoint.throwError("Duplicate plugin class");
				}
				IWailaPlugin plugin = entrypoint.newInstance();
				LOGGER.info("Start loading plugin from %s: %s".formatted(entrypoint.modName(), className));
				if (stopwatch != null) {
					stopwatch.reset().start();
				}
				WailaCommonRegistration common = WailaCommonRegistration.instance();
				plugin.register(common);
				if (CommonProxy.isPhysicallyClient()) {
					WailaClientRegistration client = WailaClientRegistration.instance();
					plugin.registerClient(client);
				}
				if (stopwatch != null) {
					LOGGER.info("%s loaded: %s".formatted(className, stopwatch.stop()));
				}
			} catch (Throwable e) {
				LOGGER.error("Failed to load plugin from %s: %s".formatted(entrypoint.modName(), className), e);
				if (CommonProxy.isDevEnv() || entrypoint.modId().equals(ID)) {
					throw e;
				}
				erroneousClasses.add(className);
			}
		}
	}

	public static void registerGameRules() {
		MAX_POSITION_DEVIATION = GameRules.registerInteger("jade:max_position_deviation", GameRuleCategory.MISC, 21, 0, 1000);
	}

	public static boolean isOutOfReach(ServerPlayer player, BlockPos pos, double baseReach) {
		ServerLevel level = player.level();
		if (level.getServer().isSingleplayerOwner(player.nameAndId())) {
			return false;
		}
		double maxDistance = Mth.square(baseReach + level.getGameRules().get(Objects.requireNonNull(MAX_POSITION_DEVIATION)));
		return pos.distSqr(player.blockPosition()) > maxDistance;
	}
}
