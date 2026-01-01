package snownee.jade.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.addon.access.EntityVariantHelper;
import snownee.jade.api.Accessor;
import snownee.jade.api.AccessorClientHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EmptyAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.JadeIds;
import snownee.jade.api.callback.JadeAfterRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeBeforeTooltipCollectCallback;
import snownee.jade.api.callback.JadeItemModNameCallback;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.platform.CustomEnchantPower;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ProgressView;
import snownee.jade.gui.PluginsConfigScreen;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.impl.config.entry.BooleanConfigEntry;
import snownee.jade.impl.config.entry.ConfigEntry;
import snownee.jade.impl.config.entry.EnumConfigEntry;
import snownee.jade.impl.config.entry.FloatConfigEntry;
import snownee.jade.impl.config.entry.IntConfigEntry;
import snownee.jade.impl.config.entry.StringConfigEntry;
import snownee.jade.impl.lookup.HierarchyLookup;
import snownee.jade.overlay.DatapackBlockManager;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.ModIdentification;

public class WailaClientRegistration implements IWailaClientRegistration {

	private static WailaClientRegistration INSTANCE = new WailaClientRegistration();

	public final HierarchyLookup<IComponentProvider<BlockAccessor>> blockIconProviders;
	public final HierarchyLookup<IComponentProvider<BlockAccessor>> blockComponentProviders;

	public final HierarchyLookup<IComponentProvider<EntityAccessor>> entityIconProviders;
	public final HierarchyLookup<IComponentProvider<EntityAccessor>> entityComponentProviders;

	public final CallbackContainer<JadeAfterRenderCallback> afterRenderCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeBeforeRenderCallback> beforeRenderCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeRayTraceCallback> rayTraceCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeTooltipCollectedCallback> tooltipCollectedCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeItemModNameCallback> itemModNameCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeBeforeTooltipCollectCallback> beforeTooltipCollectCallback = new CallbackContainer<>();

	public final Map<Identifier, ConfigEntry<?>> configEntries = Maps.newHashMap();
	public final Multimap<Identifier, Component> configCategoryOverrides = ArrayListMultimap.create();

	public final Map<Block, CustomEnchantPower> customEnchantPowers = Maps.newHashMap();
	public final Map<Identifier, IClientExtensionProvider<ItemStack, ItemView>> itemStorageProviders = Maps.newHashMap();
	public final Map<Identifier, IClientExtensionProvider<FluidView.Data, FluidView>> fluidStorageProviders = Maps.newHashMap();
	public final Map<Identifier, IClientExtensionProvider<EnergyView.Data, EnergyView>> energyStorageProviders = Maps.newHashMap();
	public final Map<Identifier, IClientExtensionProvider<ProgressView.Data, ProgressView>> progressProviders = Maps.newHashMap();

	public final Set<Identifier> clientFeatures = Sets.newHashSet();

	public final Map<Class<Accessor<?>>, AccessorClientHandler<Accessor<?>>> accessorHandlers = Maps.newIdentityHashMap();

	WailaClientRegistration() {
		blockIconProviders = new HierarchyLookup<>(Block.class);
		blockComponentProviders = new HierarchyLookup<>(Block.class);

		entityIconProviders = new HierarchyLookup<>(Entity.class);
		entityComponentProviders = new HierarchyLookup<>(Entity.class);
	}

	public static WailaClientRegistration instance() {
		return INSTANCE;
	}

	public static void reset() {
		INSTANCE = new WailaClientRegistration();
	}

	@Override
	public void registerBlockIcon(IComponentProvider<BlockAccessor> provider, Class<? extends Block> blockClass) {
		blockIconProviders.register(blockClass, provider);
		tryAddConfig(provider);
	}

	@Override
	public void registerBlockComponent(IComponentProvider<BlockAccessor> provider, Class<? extends Block> blockClass) {
		blockComponentProviders.register(blockClass, provider);
		tryAddConfig(provider);
	}

	@Override
	public void registerEntityIcon(IComponentProvider<EntityAccessor> provider, Class<? extends Entity> entityClass) {
		entityIconProviders.register(entityClass, provider);
		tryAddConfig(provider);
	}

	@Override
	public void registerEntityComponent(IComponentProvider<EntityAccessor> provider, Class<? extends Entity> entityClass) {
		entityComponentProviders.register(entityClass, provider);
		tryAddConfig(provider);
	}

	public List<IComponentProvider<BlockAccessor>> getBlockProviders(
			Block block,
			Predicate<IComponentProvider<? extends Accessor<?>>> filter) {
		return blockComponentProviders.get(block).stream().filter(filter).toList();
	}

	public List<IComponentProvider<BlockAccessor>> getBlockIconProviders(
			Block block,
			Predicate<IComponentProvider<? extends Accessor<?>>> filter) {
		return blockIconProviders.get(block).stream().filter(filter).toList();
	}

	public List<IComponentProvider<EntityAccessor>> getEntityProviders(
			Entity entity, Predicate<IComponentProvider<? extends Accessor<?>>> filter) {
		return entityComponentProviders.get(entity).stream().filter(filter).toList();
	}

	public List<IComponentProvider<EntityAccessor>> getEntityIconProviders(
			Entity entity, Predicate<IComponentProvider<? extends Accessor<?>>> filter) {
		return entityIconProviders.get(entity).stream().filter(filter).toList();
	}

	public void addConfig(ConfigEntry<?> entry) {
		Objects.requireNonNull(entry);
		Preconditions.checkArgument(StringUtils.countMatches(entry.id().getPath(), '.') <= 1);
		Preconditions.checkArgument(!hasConfig(entry.id()), "Duplicate config key: %s", entry.id());
		Preconditions.checkArgument(
				entry.isValidValue(entry.defaultValue()),
				"Default value of config %s does not pass value check",
				entry.id());
		configEntries.put(entry.id(), entry);
	}

	@Override
	public void addConfig(Identifier key, boolean defaultValue) {
		addConfig(new BooleanConfigEntry(key, defaultValue));
	}

	@Override
	public <T extends Enum<T>> void addConfig(Identifier key, T defaultValue) {
		Objects.requireNonNull(defaultValue);
		addConfig(new EnumConfigEntry<>(key, defaultValue));
	}

	@Override
	public void addConfig(Identifier key, String defaultValue, Predicate<String> validator) {
		Objects.requireNonNull(defaultValue);
		Objects.requireNonNull(validator);
		addConfig(new StringConfigEntry(key, defaultValue, validator));
	}

	@Override
	public void addConfig(Identifier key, int defaultValue, int min, int max, boolean slider) {
		addConfig(new IntConfigEntry(key, defaultValue, min, max, slider));
	}

	@Override
	public void addConfig(Identifier key, float defaultValue, float min, float max, boolean slider) {
		addConfig(new FloatConfigEntry(key, defaultValue, min, max, slider));
	}

	@Override
	public void addConfigListener(Identifier key, Consumer<Identifier> listener) {
		Objects.requireNonNull(listener);
		Preconditions.checkArgument(hasConfig(key), "Unknown config key: %s", key);
		Objects.requireNonNull(getConfigEntry(key)).addListener(listener);
	}

	@Override
	public void setConfigCategoryOverride(Identifier key, Component override) {
		Preconditions.checkArgument(!JadeIds.isAccess(key), "Cannot override option from access category");
		Preconditions.checkArgument(IPluginConfig.isPrimaryKey(key), "Only primary config key can be overridden");
		Preconditions.checkArgument(hasConfig(key), "Unknown config key: %s", key);
		configCategoryOverrides.put(key, override);
	}

	@Override
	public void setConfigCategoryOverride(Identifier key, List<Component> overrides) {
		for (Component override : overrides) {
			setConfigCategoryOverride(key, override);
		}
	}

	private void tryAddConfig(IToggleableProvider provider) {
		if (!provider.isRequired() && !hasConfig(provider.getUid())) {
			addConfig(provider.getUid(), provider.enabledByDefault());
		}
	}

	@Override
	public Set<Identifier> getConfigKeys(String namespace) {
		return getConfigKeys().stream().filter(id -> id.getNamespace().equals(namespace)).collect(Collectors.toSet());
	}

	@Override
	public Set<Identifier> getConfigKeys() {
		return configEntries.keySet();
	}

	@Override
	public boolean hasConfig(Identifier key) {
		return getConfigKeys().contains(key);
	}

	@Nullable
	public ConfigEntry<?> getConfigEntry(Identifier key) {
		return configEntries.get(key);
	}

	public List<Category> getConfigListView(boolean enableAccessibilityPlugins) {
		Multimap<String, ConfigEntry<?>> categoryMap = ArrayListMultimap.create();
		configCategoryOverrides.forEach((key, component) -> {
			categoryMap.put(component.getString(), Objects.requireNonNull(getConfigEntry(key)));
		});
		configEntries.forEach((key, entry) -> {
			if (configCategoryOverrides.containsKey(key)) {
				return;
			}
			if (!enableAccessibilityPlugins && JadeIds.isAccess(key)) {
				return;
			}
			if (!IPluginConfig.isPrimaryKey(key)) {
				Identifier primaryKey = IPluginConfig.getPrimaryKey(key);
				Collection<Component> components = configCategoryOverrides.get(primaryKey);
				if (!components.isEmpty()) {
					for (Component component : components) {
						categoryMap.put(component.getString(), entry);
					}
					return;
				}
			}
			String namespace = key.getNamespace();
			Optional<String> modName = ModIdentification.getModFullName(namespace);
			if (!Jade.ID.equals(namespace) && modName.isPresent()) {
				categoryMap.put(modName.get(), entry);
			} else {
				categoryMap.put(I18n.get(OptionsList.Entry.makeKey("plugin_" + namespace)), entry);
			}
		});

		return categoryMap.asMap().entrySet().stream()
				.map(e -> new Category(
						Component.literal(e.getKey()), e.getValue().stream()
						.sorted(Comparator.comparingInt($ -> WailaCommonRegistration.instance().priorities.getSortedList()
								.indexOf($.id())))
						.toList()
				))
				.sorted(Comparator.comparingInt(specialOrder()).thenComparing($ -> $.title().getString()))
				.toList();
	}

	private static ToIntFunction<Category> specialOrder() {
		String core = I18n.get(OptionsList.Entry.makeKey("plugin_" + Jade.ID));
		String debug = I18n.get(OptionsList.Entry.makeKey("plugin_" + Jade.ID + ".debug"));
		// core is always the first, debug is always the last
		return category -> {
			String title = category.title().getString();
			if (core.equals(title)) {
				return -1;
			}
			if (debug.equals(title)) {
				return 1;
			}
			return 0;
		};
	}

	public void setServerConfig(Map<Identifier, Object> config) {
		for (ConfigEntry<?> entry : configEntries.values()) {
			entry.setSyncedValue(null);
		}
		config.forEach((key, value) -> {
			//noinspection rawtypes
			ConfigEntry entry = getConfigEntry(key);
			if (entry != null) {
				try {
					value = entry.convertValue(value);
					if (entry.isValidValue(value)) {
						//noinspection unchecked
						entry.setSyncedValue(value);
					}
				} catch (Exception ignored) {
				}
			}
		});
		Jade.config().fixData();
	}

	public record Category(MutableComponent title, List<ConfigEntry<?>> entries) {
	}

	public void loadComplete() {
		var priorities = WailaCommonRegistration.instance().priorities;
		blockComponentProviders.loadComplete(priorities);
		blockIconProviders.loadComplete(priorities);
		entityComponentProviders.loadComplete(priorities);
		entityIconProviders.loadComplete(priorities);
		Stream.of(
				afterRenderCallback,
				beforeRenderCallback,
				rayTraceCallback,
				tooltipCollectedCallback,
				itemModNameCallback,
				beforeTooltipCollectCallback).forEach(CallbackContainer::sort);
	}

	@Override
	public void addAfterRenderCallback(int priority, JadeAfterRenderCallback callback) {
		Objects.requireNonNull(callback);
		afterRenderCallback.add(priority, callback);
	}

	@Override
	public void addBeforeRenderCallback(int priority, JadeBeforeRenderCallback callback) {
		Objects.requireNonNull(callback);
		beforeRenderCallback.add(priority, callback);
	}

	@Override
	public void addRayTraceCallback(int priority, JadeRayTraceCallback callback) {
		Objects.requireNonNull(callback);
		rayTraceCallback.add(priority, callback);
	}

	@Override
	public void addTooltipCollectedCallback(int priority, JadeTooltipCollectedCallback callback) {
		Objects.requireNonNull(callback);
		tooltipCollectedCallback.add(priority, callback);
	}

	@Override
	public void addItemModNameCallback(int priority, JadeItemModNameCallback callback) {
		Objects.requireNonNull(callback);
		itemModNameCallback.add(priority, callback);
	}

	@Override
	public void addBeforeTooltipCollectCallback(int priority, JadeBeforeTooltipCollectCallback callback) {
		Objects.requireNonNull(callback);
		beforeTooltipCollectCallback.add(priority, callback);
	}

	@Override
	public EmptyAccessor.Builder emptyAccessor() {
		Minecraft mc = Minecraft.getInstance();
		return new EmptyAccessorImpl.Builder()
				.level(Objects.requireNonNull(mc.level))
				.player(Objects.requireNonNull(mc.player))
				.serverConnected(isServerConnected())
				.serverData(getServerData())
				.showDetails(isShowDetailsPressed());
	}

	@Override
	public BlockAccessor.Builder blockAccessor() {
		Minecraft mc = Minecraft.getInstance();
		return new BlockAccessorImpl.Builder()
				.level(Objects.requireNonNull(mc.level))
				.player(Objects.requireNonNull(mc.player))
				.serverConnected(isServerConnected())
				.serverData(getServerData())
				.showDetails(isShowDetailsPressed());
	}

	@Override
	public EntityAccessor.Builder entityAccessor() {
		Minecraft mc = Minecraft.getInstance();
		return new EntityAccessorImpl.Builder()
				.level(Objects.requireNonNull(mc.level))
				.player(Objects.requireNonNull(mc.player))
				.serverConnected(isServerConnected())
				.serverData(getServerData())
				.showDetails(isShowDetailsPressed());
	}

	@Override
	public void registerCustomEnchantPower(Block block, CustomEnchantPower customEnchantPower) {
		customEnchantPowers.put(block, customEnchantPower);
	}

	@Override
	public Screen createPluginConfigScreen(@Nullable Screen parent, @Nullable Component jumpToCategory) {
		Function<OptionsList, OptionsList.@Nullable Entry> jumpTo = null;
		if (jumpToCategory != null) {
			String title = jumpToCategory.getString();
			jumpTo = options -> {
				for (OptionsList.Entry entry : options.children()) {
					if (entry instanceof OptionsList.Title e && e.title().getString().equals(title)) {
						return entry;
					}
				}
				return null;
			};
		}
		return PluginsConfigScreen.createPluginConfigScreen(parent, jumpTo, false);
	}

	@Override
	public void registerItemStorageClient(IClientExtensionProvider<ItemStack, ItemView> provider) {
		Objects.requireNonNull(provider.getUid());
		itemStorageProviders.put(provider.getUid(), provider);
	}

	@Override
	public void registerFluidStorageClient(IClientExtensionProvider<FluidView.Data, FluidView> provider) {
		Objects.requireNonNull(provider.getUid());
		fluidStorageProviders.put(provider.getUid(), provider);
	}

	@Override
	public void registerEnergyStorageClient(IClientExtensionProvider<EnergyView.Data, EnergyView> provider) {
		Objects.requireNonNull(provider.getUid());
		energyStorageProviders.put(provider.getUid(), provider);
	}

	@Override
	public void registerProgressClient(IClientExtensionProvider<ProgressView.Data, ProgressView> provider) {
		Objects.requireNonNull(provider.getUid());
		progressProviders.put(provider.getUid(), provider);
	}

	@Override
	public boolean isServerConnected() {
		return ObjectDataCenter.serverConnected;
	}

	@Override
	public boolean isShowDetailsPressed() {
		return ClientProxy.isShowDetailsPressed();
	}

	@Override
	public @Nullable CompoundTag getServerData() {
		return JadeClient.tickHandler().getData();
	}

	@Override
	public void setServerData(CompoundTag tag) {
		JadeClient.tickHandler().setData(tag);
	}

	@Override
	public ItemStack getBlockCamouflage(LevelAccessor level, BlockPos pos) {
		return DatapackBlockManager.getFakeBlock(level, pos);
	}

	@Override
	public void markAsClientFeature(Identifier uid) {
		clientFeatures.add(uid);
	}

	@Override
	public void markAsServerFeature(Identifier uid) {
		clientFeatures.remove(uid);
	}

	@Override
	public boolean isClientFeature(Identifier uid) {
		return clientFeatures.contains(uid);
	}

	@Override
	@SuppressWarnings({"unchecked", "NullableProblems"})
	public <T extends Accessor<?>> void registerAccessorHandler(Class<T> clazz, AccessorClientHandler<T> handler) {
		accessorHandlers.put((Class<Accessor<?>>) clazz, (AccessorClientHandler<Accessor<?>>) handler);
	}

	@Override
	public AccessorClientHandler<Accessor<?>> getAccessorHandler(Class<? extends Accessor<?>> clazz) {
		return Objects.requireNonNull(accessorHandlers.get(clazz), () -> "No accessor handler for " + clazz);
	}

	@Override
	public void addEntityVariantMapping(EntityType<?> entityType, @Nullable DataComponentType<?> variantType) {
		EntityVariantHelper.addVariantMapping(entityType, variantType);
	}

	@Override
	public void addVariantType(DataComponentType<?> type, boolean isVariant) {
		EntityVariantHelper.addVariantType(type, isVariant);
	}

	@Override
	public void reloadIgnoreLists() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			WailaCommonRegistration.instance().reloadOperations(connection.registryAccess());
		}
	}

	@Override
	public boolean maybeLowVisionUser() {
		return ClientProxy.metadata.hasAccessibilityMod() || IWailaConfig.get().accessibility().shouldEnableTextToSpeech();
	}
}
