package snownee.jade.util;

import java.io.File;
import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;

import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.NetworkProtocol;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.objectweb.asm.Type;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.loot.CanToolPerformAction;
import net.minecraftforge.event.TagsUpdatedEvent;
import snownee.jade.Jade;
import snownee.jade.addon.universal.ItemCollector;
import snownee.jade.addon.universal.ItemIterator;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.TraceableException;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.callback.CallbackContainer;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.command.JadeServerCommand;
import snownee.jade.impl.lookup.WrappedHierarchyLookup;
import snownee.jade.mixin.AbstractHorseAccess;
import snownee.jade.network.ClientHandshakePacket;
import snownee.jade.network.ReceiveDataPacket;
import snownee.jade.network.RequestBlockPacket;
import snownee.jade.network.RequestEntityPacket;
import snownee.jade.network.ServerHandshakePacket;
import snownee.jade.network.ServerPayloadContext;
import snownee.jade.network.ShowOverlayPacket;

@Mod(Jade.ID)
public final class CommonProxy {

	private static final CallbackContainer<BiConsumer<HolderLookup.Provider, Boolean>> componentsBoundListeners = new CallbackContainer<>();

	public static File getConfigDirectory() {
		return FMLPaths.CONFIGDIR.get().toFile();
	}

	public static boolean isCorrectToolForDrops(BlockState state, Player player, Level level, BlockPos pos) {
		// Forge's hook takes the vanilla result rather than the level+pos, and the correct-tool check
		// is what vanilla would have answered.
		return ForgeEventFactory.doPlayerHarvestCheck(player, state, ForgeHooks.isCorrectToolForDrops(state, player));
	}

	public static String getModIdFromItem(ItemStack stack) {
		HolderLookup.Provider registries = RegistryAccess.EMPTY;
		if (isPhysicallyClient() && Minecraft.getInstance().level != null) {
			registries = Minecraft.getInstance().level.registryAccess();
		}
		// Forge's IForgeItem takes only the stack; NeoForge threads a registry provider through.
		String modid = stack.getItem().getCreatorModId(stack);
		if (modid != null && !Identifier.DEFAULT_NAMESPACE.equals(modid)) {
			return modid;
		}
		if (stack.has(DataComponents.STORED_ENCHANTMENTS)) {
			ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
			modid = null;
			for (Holder<Enchantment> enchantmentHolder : enchantments.keySet()) {
				Identifier id = enchantmentHolder.unwrapKey().map(ResourceKey::identifier).orElse(null);
				if (id != null) {
					String namespace = id.getNamespace();
					if (modid == null) {
						modid = namespace;
					} else if (!modid.equals(namespace)) {
						modid = null;
						break;
					}
				}
			}
			if (modid != null) {
				return modid;
			}
		}
		PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		if (potionContents.hasEffects()) {
			modid = null;
			for (MobEffectInstance effect : potionContents.getAllEffects()) {
				Identifier id = effect.getEffect().unwrapKey().map(ResourceKey::identifier).orElse(null);
				if (id != null) {
					String namespace = id.getNamespace();
					if (modid == null) {
						modid = namespace;
					} else if (!modid.equals(namespace)) {
						modid = null;
						break;
					}
				}
			}
			if (modid != null) {
				return modid;
			}
		}
		if (stack.is(Items.PAINTING)) {
			Holder<PaintingVariant> holder = stack.get(DataComponents.PAINTING_VARIANT);
			if (holder != null) {
				return holder.unwrapKey()
						.map(ResourceKey::identifier)
						.map(Identifier::getNamespace)
						.orElse(Identifier.DEFAULT_NAMESPACE);
			}
		}
		return Identifier.DEFAULT_NAMESPACE;
	}

	public static boolean isPhysicallyClient() {
		return FMLLoader.getDist().isClient();
	}

	public static ItemCollector<?> createItemCollector(Accessor<?> accessor, Cache<Object, ItemCollector<?>> containerCache) {
		Object target = accessor.getTarget();
		if (target instanceof Player) {
			return ItemCollector.EMPTY;
		}
		if (target instanceof AbstractHorseAccess) {
			return new ItemCollector<>(new ItemIterator.ContainerItemIterator(
					o -> {
						if (o instanceof AbstractHorseAccess horse) {
							return horse.getInventory();
						}
						return null;
					}, 2));
		}
		if (!(target instanceof ChestBlockEntity)) {
			try {
				var storage = findItemHandler(accessor);
				if (storage != null) {
					return containerCache.get(storage, () -> new ItemCollector<>(JadeForgeUtils.fromItemHandler(storage, 0)));
				}
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, null, null);
			}
		}
		final Container container = findContainer(accessor);
		if (container != null) {
			if (container instanceof ChestBlockEntity) {
				return new ItemCollector<>(new ItemIterator.ContainerItemIterator(
						a -> {
							if (a.getTarget() instanceof ChestBlockEntity be) {
								if (be.getBlockState().getBlock() instanceof ChestBlock chestBlock) {
									Container compound = ChestBlock.getContainer(
											chestBlock,
											be.getBlockState(),
											Objects.requireNonNull(be.getLevel()),
											be.getBlockPos(),
											true);
									if (compound != null) {
										return compound;
									}
								}
								return be;
							}
							return null;
						}, 0));
			}
			return new ItemCollector<>(new ItemIterator.ContainerItemIterator(0));
		}
		return ItemCollector.EMPTY;
	}

	@Nullable
	public static List<ViewGroup<ItemStack>> containerGroup(Container container, Accessor<?> accessor) {
		return containerGroup(container, accessor, CommonProxy::findContainer);
	}

	@Nullable
	public static List<ViewGroup<ItemStack>> containerGroup(
			Container container,
			Accessor<?> accessor,
			Function<Accessor<?>, @Nullable Container> containerFinder) {
		try {
			return ItemStorageProvider.containerCache.get(
							container,
							() -> new ItemCollector<>(new ItemIterator.ContainerItemIterator(containerFinder, 0)))
					.update(accessor);
		} catch (Exception e) {
			return null;
		}
	}

	@Nullable
	public static List<ViewGroup<ItemStack>> storageGroup(Object storage, Accessor<?> accessor) {
		return storageGroup(storage, accessor, CommonProxy::findItemHandler);
	}

	@Nullable
	public static List<ViewGroup<ItemStack>> storageGroup(
			Object storage,
			Accessor<?> accessor,
			Function<Accessor<?>, @Nullable Object> storageFinder) {
		try {
			//noinspection unchecked
			return ItemStorageProvider.containerCache.get(
					storage,
					() -> new ItemCollector<>(JadeForgeUtils.fromItemHandler(
							(IItemHandler) storage,
							0,
							(Function<Accessor<?>, @Nullable IItemHandler>) (Object) storageFinder))).update(
					accessor
			);
		} catch (Exception e) {
			return null;
		}
	}

	@Nullable
	public static IItemHandler findItemHandler(Accessor<?> accessor) {
		return getDefaultStorage(accessor, ForgeCapabilities.ITEM_HANDLER);
	}

	@Nullable
	public static Container findContainer(Accessor<?> accessor) {
		Object target = accessor.getTarget();
		if (target == null && accessor instanceof BlockAccessor blockAccessor &&
				blockAccessor.getBlock() instanceof WorldlyContainerHolder holder) {
			return holder.getContainer(blockAccessor.getBlockState(), accessor.getLevel(), blockAccessor.getPosition());
		} else if (target instanceof Container container) {
			return container;
		}
		return null;
	}

	@Nullable
	public static List<ViewGroup<FluidView.Data>> wrapFluidStorage(Accessor<?> accessor) {
		IFluidHandler fluidHandler = getDefaultStorage(accessor, ForgeCapabilities.FLUID_HANDLER);
		if (fluidHandler != null) {
			return JadeForgeUtils.fromFluidHandler(fluidHandler);
		}
		return null;
	}

	@Nullable
	public static List<ViewGroup<EnergyView.Data>> wrapEnergyStorage(Accessor<?> accessor) {
		IEnergyStorage energyStorage = getDefaultStorage(accessor, ForgeCapabilities.ENERGY);
		if (energyStorage != null) {
			var group = new ViewGroup<>(List.of(new EnergyView.Data(energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored())));
			group.getExtraData().putString("Unit", "FE");
			return List.of(group);
		}
		return null;
	}

	public static boolean isDevEnv() {
		return !FMLLoader.isProduction();
	}

	public static Identifier getId(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block);
	}

	public static Identifier getId(EntityType<?> entityType) {
		return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
	}

	public static Identifier getId(BlockEntityType<?> blockEntityType) {
		return Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType));
	}

	public static String getPlatformIdentifier() {
		return "neoforge";
	}

	private static void registerServerCommand(RegisterCommandsEvent event) {
		JadeServerCommand.register(event.getDispatcher());
	}

	public static boolean isBoss(Entity entity) {
		EntityType<?> entityType = entity.getType();
		return entity.is(Tags.EntityTypes.BOSSES) || entityType == EntityTypes.ENDER_DRAGON || entityType == EntityTypes.WITHER;
	}

	public static ItemStack getBlockPickedResult(BlockState state, Player player, BlockHitResult hitResult) {
		// NeoForge adds a Player-aware overload; Forge only has vanilla's (LevelReader, BlockPos,
		// boolean), so blocks that would customise the picked stack per player fall back to the
		// standard result.
		return state.getCloneItemStack(player.level(), hitResult.getBlockPos(), true);
	}

	public static ItemStack getEntityPickedResult(Entity entity, Player player, EntityHitResult hitResult) {
		return MoreObjects.firstNonNull(entity.getPickResult(), ItemStack.EMPTY);
	}

	public static boolean isModLoaded(String modid) {
		try {
			return ModList.isLoaded(modid);
		} catch (Throwable e) {
			// ModList is not built yet during early loading; the loading list still answers.
			try {
				return LoadingModList.getModFileById(modid) != null;
			} catch (Throwable e2) {
				return false;
			}
		}
	}

	public static Optional<String> getModVersion(String modid) {
		// NeoForge routed this through MavenVersionTranslator; ArtifactVersion.toString() gives the
		// same string on Forge.
		return ModList.getModContainerById(modid).map($ -> $.getModInfo().getVersion().toString());
	}

	public static void loadComplete() {
		Jade.loadPlugins();
	}

	public static List<Entrypoint> loadEntrypoints() {
		List<Entrypoint> entrypoints = Lists.newArrayList();
		Set<IModFileInfo> modFiles = Sets.newIdentityHashSet();
		Set<String> classNames = Sets.newHashSet();
		for (ModContainer container : ModList.getLoadedMods()) {
			IModFileInfo owningFile = container.getModInfo().getOwningFile();
			if (owningFile == null || !modFiles.add(owningFile)) {
				continue;
			}
			owningFile.getFile()
					.getScanResult()
					.getAnnotations()
					.stream()
					.filter($ -> $.targetType() == ElementType.TYPE &&
							$.annotationType().equals(Type.getType(WailaPlugin.class)))
					.map($ -> new Entrypoint(container, $))
					.forEach(entrypoint -> {
						if (classNames.add(entrypoint.className())) {
							entrypoints.add(entrypoint);
						}
					});
		}
		return entrypoints;
	}

	public static Component getFluidName(JadeFluidObject fluid) {
		return toFluidStack(fluid).getDisplayName();
	}

	public static FluidStack toFluidStack(JadeFluidObject fluid) {
		if (fluid.isEmpty()) {
			return FluidStack.EMPTY;
		} else {
			long amount = fluid.getAmount();
			if (amount > Integer.MAX_VALUE) {
				amount = Integer.MAX_VALUE;
			}
			// Forge's FluidStack takes the fluid itself and carries variant data as NBT, so the
			// component patch is dropped here (see JadeForgeUtils for the same trade-off).
			return new FluidStack(fluid.typeHolder().value(), (int) amount);
		}
	}

	public static boolean isMultipartEntity(Entity target) {
		return target.isMultipartEntity();
	}

	public static Entity wrapPartEntityParent(Entity target) {
		if (target instanceof PartEntity<?> part) {
			return part.getParent();
		}
		return target;
	}

	@SuppressWarnings("NullableProblems")
	public static int getPartEntityIndex(Entity entity) {
		if (!(entity instanceof PartEntity<?> part)) {
			return -1;
		}
		Entity parent = wrapPartEntityParent(entity);
		PartEntity<?>[] parts = parent.getParts();
		//noinspection ConstantValue
		if (parts == null) {
			return -1;
		}
		return List.of(parts).indexOf(part);
	}

	@Nullable
	public static Entity getPartEntity(@Nullable Entity parent, int index) {
		if (parent == null) {
			return null;
		}
		if (index < 0) {
			return parent;
		}
		@Nullable PartEntity<?>[] parts = parent.getParts();
		//noinspection ConstantValue
		if (parts == null || index >= parts.length) {
			return parent;
		}
		return parts[index];
	}

	/**
	 * NeoForge resolves capabilities from the level, which lets a mod attach one to a block that has
	 * no block entity. Forge 26.2 resolves them from the capability provider itself, so a block
	 * without a block entity cannot expose an inventory, tank or energy buffer here — such blocks
	 * simply show nothing rather than showing wrong data.
	 */
	@Nullable
	public static <T> T getDefaultStorage(Accessor<?> accessor, Capability<T> capability) {
		if (accessor instanceof BlockAccessor blockAccessor) {
			BlockEntity blockEntity = blockAccessor.getBlockEntity();
			if (blockEntity == null) {
				return null;
			}
			return blockEntity.getCapability(capability, null).orElse(null);
		} else if (accessor instanceof EntityAccessor entityAccessor) {
			return entityAccessor.getEntity().getCapability(capability, null).orElse(null);
		}
		return null;
	}

	public static <T> boolean hasDefaultStorage(Accessor<?> accessor, Capability<T> capability) {
		if (accessor instanceof BlockAccessor || accessor instanceof EntityAccessor) {
			return getDefaultStorage(accessor, capability) != null;
		}
		return true;
	}

	public static boolean hasDefaultItemStorage(Accessor<?> accessor) {
		if (accessor.getTarget() == null && accessor instanceof BlockAccessor blockAccessor &&
				blockAccessor.getBlock() instanceof WorldlyContainerHolder) {
			return true;
		}
		return hasDefaultStorage(accessor, ForgeCapabilities.ITEM_HANDLER);
	}

	public static boolean hasDefaultFluidStorage(Accessor<?> accessor) {
		return hasDefaultStorage(accessor, ForgeCapabilities.FLUID_HANDLER);
	}

	public static boolean hasDefaultEnergyStorage(Accessor<?> accessor) {
		return hasDefaultStorage(accessor, ForgeCapabilities.ENERGY);
	}

	public static long bucketVolume() {
		return FluidType.BUCKET_VOLUME;
	}

	public static long blockVolume() {
		return FluidType.BUCKET_VOLUME;
	}

	public static void registerTagsUpdatedListener(BiConsumer<HolderLookup.Provider, Boolean> listener) {
		TagsUpdatedEvent.BUS.addListener(event -> {
			if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
				listener.accept(event.getRegistryAccess(), true);
			}
		});
		componentsBoundListeners.add(listener);
	}

	public static void callComponentsBoundListeners(HolderLookup.Provider provider) {
		componentsBoundListeners.call(listener -> listener.accept(provider, false));
	}

	public static boolean isCorrectConditions(List<LootItemCondition> conditions, ItemStack toolItem) {
		if (conditions.size() != 1) {
			return false;
		}
		LootItemCondition condition = conditions.getFirst();
		if (condition instanceof MatchTool(Optional<ItemPredicate> predicate)) {
			ItemPredicate itemPredicate = predicate.orElse(null);
			return itemPredicate != null && itemPredicate.test(toolItem);
		} else if (condition instanceof AnyOfCondition anyOfCondition) {
			for (LootItemCondition child : anyOfCondition.terms) {
				if (isCorrectConditions(List.of(child), toolItem)) {
					return true;
				}
			}
		} else if (condition instanceof CanToolPerformAction canToolPerformAction) {
			return canToolPerformAction.action() == ToolActions.SHEARS_DIG;
		}
		return false;
	}

	public static <T> Map.@Nullable Entry<Identifier, List<ViewGroup<T>>> getServerExtensionData(
			Accessor<?> accessor,
			WrappedHierarchyLookup<IServerExtensionProvider<T>> lookup) {
		for (var provider : lookup.wrappedGet(accessor)) {
			List<ViewGroup<T>> groups;
			try {
				groups = provider.getGroups(accessor);
			} catch (Exception e) {
				WailaExceptionHandler.handleErr(e, provider, null);
				continue;
			}
			if (groups != null) {
				return Map.entry(provider.getUid(), groups);
			}
		}
		return null;
	}

	// The payload codecs live in this channel, not in vanilla's custom-payload registry, so every
	// send has to go through it. Wrapping a payload in a vanilla Clientbound/ServerboundCustomPayload
	// packet and pushing it straight onto the connection makes the encoder fail.
	static Channel<CustomPacketPayload> CHANNEL;

	public CommonProxy(FMLJavaModLoadingContext loadingContext) {
		// named loadingContext: the packet handler lambdas below already bind a 'context'
		BusGroup modBusGroup = loadingContext.getModBusGroup();
		FMLLoadCompleteEvent.getBus(modBusGroup).addListener(event -> event.enqueueWork(CommonProxy::loadComplete));
		// NeoForge registers payloads through RegisterPayloadHandlersEvent and a fluent registrar.
		// Forge 26.2 builds a named channel instead: the payload types keep their own ids, but the
		// channel itself needs one, hence "jade:main".
		CHANNEL = ChannelBuilder.named(Identifier.fromNamespaceAndPath(Jade.ID, "main"))
				.networkProtocolVersion(Integer.parseInt(Jade.PROTOCOL_VERSION))
				.optional()
				.payloadChannel()
				.protocol(NetworkProtocol.PLAY)
				.clientbound()
				.addMain(
						ReceiveDataPacket.TYPE,
						ReceiveDataPacket.CODEC.<RegistryFriendlyByteBuf>mapStream(buf -> buf),
						(payload, context) -> ReceiveDataPacket.handle(payload, context::enqueueWork))
				.addMain(
						ServerHandshakePacket.TYPE,
						ServerHandshakePacket.CODEC,
						(payload, context) -> ServerHandshakePacket.handle(payload, context::enqueueWork))
				.addMain(
						ShowOverlayPacket.TYPE,
						ShowOverlayPacket.CODEC,
						(payload, context) -> ShowOverlayPacket.handle(payload, context::enqueueWork))
				.serverbound()
				.addMain(
						RequestEntityPacket.TYPE,
						RequestEntityPacket.CODEC,
						(payload, context) -> RequestEntityPacket.handle(payload, context::getSender))
				.addMain(
						RequestBlockPacket.TYPE,
						RequestBlockPacket.CODEC,
						(payload, context) -> RequestBlockPacket.handle(payload, context::getSender))
				.addMain(
						ClientHandshakePacket.TYPE,
						ClientHandshakePacket.CODEC,
						(payload, context) -> ClientHandshakePacket.handle(payload, context::getSender))
				.build();
		RegisterEvent.getBus(modBusGroup).addListener(event -> {
			if (event.getRegistryKey() == Registries.GAME_RULE) {
				Jade.registerGameRules();
			}
		});
		RegisterCommandsEvent.BUS.addListener(CommonProxy::registerServerCommand);
		if (isPhysicallyClient()) {
			ClientProxy.init(modBusGroup);
		}
	}

	public static String defaultEnergyUnit() {
		return "E";
	}

	public static void sendPacket(ServerPlayer player, CustomPacketPayload payload) {
		CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
	}

	@SuppressWarnings("deprecation")
	public static TriState isShearable(Entity entity) {
		if (entity instanceof Shearable shearable) {
			if (entity instanceof Sheep || entity instanceof MushroomCow) {
				if (((Animal) entity).isBaby()) {
					return TriState.FALSE;
				}
			}
			return shearable.readyForShearing() ? TriState.TRUE : TriState.FALSE;
		}
		return TriState.DEFAULT;
	}

	@Nullable
	public static Either<String, Component> getTranslatableName(@Nullable Object object) {
		// TranslatableEnum is a NeoForge interface with no Forge counterpart. Enum values that
		// relied on it fall back to their raw name in the config screen.
		return null;
	}

	public static void runWithContext(ServerPayloadContext context, Runnable runnable) {
		context.player().level().getServer().execute(runnable);
	}

	public record Entrypoint(ModContainer container, ModFileScanData.AnnotationData annotationData) {
		public String className() {
			return annotationData.memberName();
		}

		public String modId() {
			return container.getModId();
		}

		public String modName() {
			return container.getModInfo().getDisplayName();
		}

		public String requiredMod() {
			return annotationData.annotationData().getOrDefault("value", "").toString();
		}

		public IWailaPlugin newInstance() {
			try {
				return (IWailaPlugin) Class.forName(className()).getDeclaredConstructor().newInstance();
			} catch (Throwable e) {
				throwError("Failed to instantiate plugin class");
				throw new AssertionError();
			}
		}

		public void throwError(String message, @Nullable Throwable cause) {
			message = "Error in plugin class %s from %s: %s".formatted(className(), modName(), message);
			if (cause == null) {
				cause = new IllegalStateException(message);
			} else {
				cause = new IllegalStateException(message, cause);
			}
			throw new TraceableException(cause, modId());
		}

		public void throwError(String message) {
			throwError(message, null);
		}
	}
}