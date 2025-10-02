package snownee.jade.util;

import java.io.File;
import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.decoration.PaintingVariant;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.i18n.MavenVersionTranslator;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
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
import snownee.jade.network.ShowOverlayPacket;

@Mod(Jade.ID)
public final class CommonProxy {

	public static File getConfigDirectory() {
		return FMLPaths.CONFIGDIR.get().toFile();
	}

	public static boolean isCorrectToolForDrops(BlockState state, Player player, Level level, BlockPos pos) {
		return EventHooks.doPlayerHarvestCheck(player, state, level, pos);
	}

	public static String getModIdFromItem(ItemStack stack) {
		HolderLookup.Provider registries = RegistryAccess.EMPTY;
		if (isPhysicallyClient() && Minecraft.getInstance().level != null) {
			registries = Minecraft.getInstance().level.registryAccess();
		}
		String modid = stack.getItem().getCreatorModId(registries, stack);
		if (!ResourceLocation.DEFAULT_NAMESPACE.equals(modid)) {
			return modid;
		}
		if (stack.has(DataComponents.STORED_ENCHANTMENTS)) {
			ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
			modid = null;
			for (Holder<Enchantment> enchantmentHolder : enchantments.keySet()) {
				ResourceLocation id = enchantmentHolder.unwrapKey().map(ResourceKey::location).orElse(null);
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
				ResourceLocation id = effect.getEffect().unwrapKey().map(ResourceKey::location).orElse(null);
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
						.map(ResourceKey::location)
						.map(ResourceLocation::getNamespace)
						.orElse(ResourceLocation.DEFAULT_NAMESPACE);
			}
		}
		return ResourceLocation.DEFAULT_NAMESPACE;
	}

	public static boolean isPhysicallyClient() {
		return FMLEnvironment.getDist().isClient();
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
			Function<Accessor<?>, Container> containerFinder) {
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
			Function<Accessor<?>, Object> storageFinder) {
		try {
			//noinspection unchecked
			return ItemStorageProvider.containerCache.get(
					storage,
					() -> new ItemCollector<>(JadeForgeUtils.fromItemHandler(
							(ResourceHandler<ItemResource>) storage,
							0,
							(Function<Accessor<?>, @Nullable ResourceHandler<ItemResource>>) (Object) storageFinder))).update(
					accessor
			);
		} catch (Exception e) {
			return null;
		}
	}

	@Nullable
	public static ResourceHandler<ItemResource> findItemHandler(Accessor<?> accessor) {
		if (accessor instanceof BlockAccessor blockAccessor) {
			return accessor.getLevel().getCapability(
					Capabilities.Item.BLOCK,
					blockAccessor.getPosition(),
					blockAccessor.getBlockState(),
					blockAccessor.getBlockEntity(),
					null);
		} else if (accessor instanceof EntityAccessor entityAccessor) {
			return entityAccessor.getEntity().getCapability(Capabilities.Item.ENTITY);
		}
		return null;
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
		ResourceHandler<FluidResource> fluidHandler = getDefaultStorage(accessor, Capabilities.Fluid.BLOCK, Capabilities.Fluid.ENTITY);
		if (fluidHandler != null) {
			return JadeForgeUtils.fromFluidHandler(fluidHandler);
		}
		return null;
	}

	@Nullable
	public static List<ViewGroup<EnergyView.Data>> wrapEnergyStorage(Accessor<?> accessor) {
		EnergyHandler energyStorage = getDefaultStorage(accessor, Capabilities.Energy.BLOCK, Capabilities.Energy.ENTITY);
		if (energyStorage != null) {
			var group = new ViewGroup<>(List.of(new EnergyView.Data(energyStorage.getAmountAsLong(), energyStorage.getCapacityAsLong())));
			group.getExtraData().putString("Unit", "FE");
			return List.of(group);
		}
		return null;
	}

	public static boolean isDevEnv() {
		return !FMLEnvironment.isProduction();
	}

	public static ResourceLocation getId(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block);
	}

	public static ResourceLocation getId(EntityType<?> entityType) {
		return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
	}

	public static ResourceLocation getId(BlockEntityType<?> blockEntityType) {
		return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType);
	}

	public static String getPlatformIdentifier() {
		return "neoforge";
	}

	private static void registerServerCommand(RegisterCommandsEvent event) {
		JadeServerCommand.register(event.getDispatcher());
	}

	public static boolean isBoss(Entity entity) {
		EntityType<?> entityType = entity.getType();
		return entityType.is(Tags.EntityTypes.BOSSES) || entityType == EntityType.ENDER_DRAGON || entityType == EntityType.WITHER;
	}

	public static ItemStack getBlockPickedResult(BlockState state, Player player, BlockHitResult hitResult) {
		return state.getCloneItemStack(player.level(), hitResult.getBlockPos(), true);
	}

	public static ItemStack getEntityPickedResult(Entity entity, Player player, EntityHitResult hitResult) {
		return MoreObjects.firstNonNull(entity.getPickResult(), ItemStack.EMPTY);
	}

	public static boolean isModLoaded(String modid) {
		try {
			ModList modList = ModList.get();
			if (modList == null) {
				return FMLLoader.getCurrent().getLoadingModList().getModFileById(modid) != null;
			}
			return modList.isLoaded(modid);
		} catch (Throwable e) {
			return false;
		}
	}

	public static Optional<String> getModVersion(String modid) {
		return ModList.get().getModContainerById(modid).map($ -> MavenVersionTranslator.artifactVersionToString($.getModInfo()
				.getVersion()));
	}

	public static void loadComplete() {
		Jade.loadPlugins();
	}

	public static List<Entrypoint> loadEntrypoints() {
		List<Entrypoint> entrypoints = Lists.newArrayList();
		for (ModContainer container : ModList.get().getSortedMods()) {
			IModFileInfo owningFile = container.getModInfo().getOwningFile();
			if (owningFile == null) {
				continue;
			}
			owningFile.getFile()
					.getScanResult()
					.getAnnotatedBy(WailaPlugin.class, ElementType.TYPE)
					.map($ -> new Entrypoint(container, $))
					.forEach(entrypoints::add);
		}
		return entrypoints;
	}

	public static Component getFluidName(JadeFluidObject fluid) {
		return toFluidStack(fluid).getHoverName();
	}

	public static FluidStack toFluidStack(JadeFluidObject fluid) {
		int id = BuiltInRegistries.FLUID.getId(fluid.getType().unwrapKey().orElseThrow());
		Optional<Holder.Reference<Fluid>> holder = BuiltInRegistries.FLUID.get(id);
		if (holder.isEmpty()) {
			return FluidStack.EMPTY;
		} else {
			long amount = fluid.getAmount();
			if (amount > Integer.MAX_VALUE) {
				amount = Integer.MAX_VALUE;
			}
			return new FluidStack(holder.get(), (int) amount, fluid.getComponents());
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

	public static Entity getPartEntity(Entity parent, int index) {
		if (parent == null) {
			return null;
		}
		if (index < 0) {
			return parent;
		}
		PartEntity<?>[] parts = parent.getParts();
		if (parts == null || index >= parts.length) {
			return parent;
		}
		return parts[index];
	}

	public static <T> T getDefaultStorage(
			Accessor<?> accessor,
			BlockCapability<T, ?> blockCapability,
			EntityCapability<T, ?> entityCapability) {
		if (accessor instanceof BlockAccessor blockAccessor) {
			return accessor.getLevel().getCapability(
					blockCapability,
					blockAccessor.getPosition(),
					blockAccessor.getBlockState(),
					blockAccessor.getBlockEntity(),
					null);
		} else if (accessor instanceof EntityAccessor entityAccessor) {
			return entityAccessor.getEntity().getCapability(entityCapability, null);
		}
		return null;
	}

	public static <T> boolean hasDefaultStorage(
			Accessor<?> accessor,
			BlockCapability<T, ?> blockCapability,
			EntityCapability<T, ?> entityCapability) {
		if (accessor instanceof BlockAccessor || accessor instanceof EntityAccessor) {
			return getDefaultStorage(accessor, blockCapability, entityCapability) != null;
		}
		return true;
	}

	public static boolean hasDefaultItemStorage(Accessor<?> accessor) {
		if (accessor.getTarget() == null && accessor instanceof BlockAccessor blockAccessor &&
				blockAccessor.getBlock() instanceof WorldlyContainerHolder) {
			return true;
		}
		return hasDefaultStorage(accessor, Capabilities.Item.BLOCK, Capabilities.Item.ENTITY);
	}

	public static boolean hasDefaultFluidStorage(Accessor<?> accessor) {
		return hasDefaultStorage(accessor, Capabilities.Fluid.BLOCK, Capabilities.Fluid.ENTITY);
	}

	public static boolean hasDefaultEnergyStorage(Accessor<?> accessor) {
		return hasDefaultStorage(accessor, Capabilities.Energy.BLOCK, Capabilities.Energy.ENTITY);
	}

	public static long bucketVolume() {
		return FluidType.BUCKET_VOLUME;
	}

	public static long blockVolume() {
		return FluidType.BUCKET_VOLUME;
	}

	public static void registerTagsUpdatedListener(BiConsumer<HolderLookup.Provider, Boolean> listener) {
		NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> listener.accept(
				event.getLookupProvider(),
				event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED));
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
		}
		return false;
	}

	public static <T> Map.Entry<ResourceLocation, List<ViewGroup<T>>> getServerExtensionData(
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

	public CommonProxy(IEventBus modBus) {
		modBus.addListener(FMLLoadCompleteEvent.class, event -> event.enqueueWork(CommonProxy::loadComplete));
		modBus.addListener(
				RegisterPayloadHandlersEvent.class, event -> {
					event.registrar(Jade.ID)
							.versioned(Jade.PROTOCOL_VERSION)
							.optional()
							.playToClient(
									ReceiveDataPacket.TYPE,
									ReceiveDataPacket.CODEC,
									(payload, context) -> ReceiveDataPacket.handle(payload, context::enqueueWork))
							.playToServer(
									RequestEntityPacket.TYPE,
									RequestEntityPacket.CODEC,
									(payload, context) -> RequestEntityPacket.handle(payload, () -> (ServerPlayer) context.player()))
							.playToServer(
									RequestBlockPacket.TYPE,
									RequestBlockPacket.CODEC,
									(payload, context) -> RequestBlockPacket.handle(payload, () -> (ServerPlayer) context.player()))
							.playToServer(
									ClientHandshakePacket.TYPE,
									ClientHandshakePacket.CODEC,
									(payload, context) -> ClientHandshakePacket.handle(payload, () -> (ServerPlayer) context.player()))
							.playToClient(
									ServerHandshakePacket.TYPE,
									ServerHandshakePacket.CODEC,
									(payload, context) -> ServerHandshakePacket.handle(payload, context::enqueueWork))
							.playToClient(
									ShowOverlayPacket.TYPE,
									ShowOverlayPacket.CODEC,
									(payload, context) -> ShowOverlayPacket.handle(payload, context::enqueueWork));
				});
		NeoForge.EVENT_BUS.addListener(CommonProxy::registerServerCommand);
		if (isPhysicallyClient()) {
			ClientProxy.init(modBus);
		}
	}

	public static String defaultEnergyUnit() {
		return "E";
	}

	public static void sendPacket(ServerPlayer player, CustomPacketPayload payload) {
		player.connection.send(payload);
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
	public static Either<String, Component> getTranslatableName(Object object) {
		if (object instanceof TranslatableEnum translatableEnum) {
			return Either.right(translatableEnum.getTranslatedName());
		}
		return null;
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