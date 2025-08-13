package snownee.jade.util;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Either;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
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
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ResolvableProfile;
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
import snownee.jade.Jade;
import snownee.jade.addon.universal.ItemCollector;
import snownee.jade.addon.universal.ItemIterator;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.TraceableException;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.command.JadeServerCommand;
import snownee.jade.compat.TechRebornEnergyCompat;
import snownee.jade.impl.lookup.WrappedHierarchyLookup;
import snownee.jade.mixin.AbstractHorseAccess;
import snownee.jade.network.ClientHandshakePacket;
import snownee.jade.network.ReceiveDataPacket;
import snownee.jade.network.RequestBlockPacket;
import snownee.jade.network.RequestEntityPacket;
import snownee.jade.network.ServerHandshakePacket;
import snownee.jade.network.ShowOverlayPacket;

public final class CommonProxy implements ModInitializer {

	public static boolean hasTechRebornEnergy = isModLoaded("team_reborn_energy");

	@Nullable
	public static String getLastKnownUsername(@Nullable UUID uuid) {
		if (uuid == null) {
			return null;
		}
		ResolvableProfile gameProfile = new ResolvableProfile(new GameProfile(uuid, "")).pollResolve();
		if (gameProfile != null) {
			return gameProfile.gameProfile().getName();
		}
		if (isPhysicallyClient()) {
			return UsernameCache.getLastKnownUsername(uuid);
		}
		return null;
	}

	public static File getConfigDirectory() {
		return FabricLoader.getInstance().getConfigDir().toFile();
	}

	public static boolean isCorrectToolForDrops(BlockState state, Player player, Level level, BlockPos pos) {
		return player.hasCorrectToolForDrops(state);
	}

	public static String getModIdFromItem(ItemStack stack) {
		if (isPhysicallyClient()) {
			CustomModelData modelData = stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY);
			if (!CustomModelData.EMPTY.equals(modelData)) {
				for (String string : modelData.strings()) {
					if (string.startsWith("namespace:")) {
						return string.substring(10);
					}
				}
			}
		}
		String modid = stack.getItem().getCreatorNamespace(stack);
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
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
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
					return containerCache.get(storage, () -> new ItemCollector<>(JadeFabricUtils.fromItemStorage(storage, 0)));
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
											false);
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
					() -> new ItemCollector<>(JadeFabricUtils.fromItemStorage(
							(Storage<ItemVariant>) storage,
							0,
							(Function<Accessor<?>, @Nullable Storage<ItemVariant>>) (Object) storageFinder))).update(
					accessor
			);
		} catch (Exception e) {
			return null;
		}
	}

	@Nullable
	public static Storage<ItemVariant> findItemHandler(Accessor<?> accessor) {
		if (accessor instanceof BlockAccessor blockAccessor) {
			return ItemStorage.SIDED.find(
					blockAccessor.getLevel(),
					blockAccessor.getPosition(),
					blockAccessor.getBlockState(),
					blockAccessor.getBlockEntity(),
					null);
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

	public static List<ViewGroup<FluidView.Data>> wrapFluidStorage(Accessor<?> accessor) {
		if (accessor instanceof BlockAccessor blockAccessor) {
			try {
				var storage = FluidStorage.SIDED.find(
						accessor.getLevel(),
						blockAccessor.getPosition(),
						blockAccessor.getBlockState(),
						blockAccessor.getBlockEntity(),
						null);
				if (storage != null) {
					return JadeFabricUtils.fromFluidStorage(storage);
				}
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, null, null);
			}
		}
		return null;
	}

	public static List<ViewGroup<EnergyView.Data>> wrapEnergyStorage(Accessor<?> accessor) {
		if (hasTechRebornEnergy && accessor instanceof BlockAccessor blockAccessor) {
			try {
				var storage = TechRebornEnergyCompat.getSided().find(
						accessor.getLevel(),
						blockAccessor.getPosition(),
						blockAccessor.getBlockState(),
						blockAccessor.getBlockEntity(),
						null);
				if (storage != null && storage.getCapacity() > 0) {
					var group = new ViewGroup<>(List.of(new EnergyView.Data(storage.getAmount(), storage.getCapacity())));
					group.getExtraData().putString("Unit", "E");
					return List.of(group);
				}
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, null, null);
			}
		}
		return null;
	}

	public static boolean isDevEnv() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
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
		return "fabric";
	}

	private static void registerServerCommand(
			CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
		JadeServerCommand.register(dispatcher);
	}

	public static boolean isBoss(Entity entity) {
		EntityType<?> entityType = entity.getType();
		return entityType.is(ConventionalEntityTypeTags.BOSSES) || entityType == EntityType.ENDER_DRAGON || entityType == EntityType.WITHER;
	}

	public static ItemStack getBlockPickedResult(BlockState state, Player player, BlockHitResult hitResult) {
		return state.getCloneItemStack(player.level(), hitResult.getBlockPos(), true);
	}

	public static ItemStack getEntityPickedResult(Entity entity, Player player, EntityHitResult hitResult) {
		ItemStack stack = entity.getPickResult();
		return stack == null ? ItemStack.EMPTY : stack;
	}

	public static boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	public static Optional<String> getModVersion(String modid) {
		return FabricLoader.getInstance()
				.getModContainer(modid)
				.map(ModContainer::getMetadata)
				.map(ModMetadata::getVersion)
				.map(Version::getFriendlyString);
	}

	public static void loadComplete() {
		Jade.loadPlugins();
	}

	public static List<Entrypoint> loadEntrypoints() {
		return FabricLoader.getInstance().getEntrypointContainers(Jade.ID, IWailaPlugin.class).stream().map(Entrypoint::new).toList();
	}

	public static Component getFluidName(JadeFluidObject fluidObject) {
		Fluid fluid = fluidObject.getType();
		DataComponentPatch components = fluidObject.getComponents();
		return FluidVariantAttributes.getName(FluidVariant.of(fluid, components));
	}

	public static boolean isMultipartEntity(Entity target) {
		return target instanceof EnderDragon;
	}

	public static Entity wrapPartEntityParent(Entity target) {
		if (target instanceof EnderDragonPart part) {
			return part.parentMob;
		}
		return target;
	}

	public static int getPartEntityIndex(Entity entity) {
		if (!(entity instanceof EnderDragonPart part)) {
			return -1;
		}
		if (!(wrapPartEntityParent(entity) instanceof EnderDragon parent)) {
			return -1;
		}
		EnderDragonPart[] parts = parent.getSubEntities();
		return List.of(parts).indexOf(part);
	}

	public static Entity getPartEntity(Entity parent, int index) {
		if (parent == null) {
			return null;
		}
		if (index < 0) {
			return parent;
		}
		if (parent instanceof EnderDragon dragon) {
			EnderDragonPart[] parts = dragon.getSubEntities();
			if (index < parts.length) {
				return parts[index];
			}
		}
		return parent;
	}

	public static boolean hasDefaultItemStorage(Accessor<?> accessor) {
		if (accessor instanceof BlockAccessor blockAccessor) {
			if (blockAccessor.getBlockEntity() == null) {
				return blockAccessor.getBlock() instanceof WorldlyContainerHolder;
			}
			return ItemStorage.SIDED.find(
					accessor.getLevel(),
					blockAccessor.getPosition(),
					blockAccessor.getBlockState(),
					blockAccessor.getBlockEntity(),
					null) != null;
		}
		return true;
	}

	public static boolean hasDefaultFluidStorage(Accessor<?> accessor) {
		if (accessor instanceof BlockAccessor blockAccessor) {
			return FluidStorage.SIDED.find(
					accessor.getLevel(),
					blockAccessor.getPosition(),
					blockAccessor.getBlockState(),
					blockAccessor.getBlockEntity(),
					null) != null;
		}
		return true;
	}

	public static boolean hasDefaultEnergyStorage(Accessor<?> accessor) {
		if (hasTechRebornEnergy && accessor instanceof BlockAccessor blockAccessor) {
			return TechRebornEnergyCompat.getSided().find(
					accessor.getLevel(),
					blockAccessor.getPosition(),
					blockAccessor.getBlockState(),
					blockAccessor.getBlockEntity(),
					null) != null;
		}
		return true;
	}

	public static long bucketVolume() {
		return FluidConstants.BUCKET;
	}

	public static long blockVolume() {
		return FluidConstants.BLOCK;
	}

	public static void registerTagsUpdatedListener(BiConsumer<HolderLookup.Provider, Boolean> listener) {
		CommonLifecycleEvents.TAGS_LOADED.register(listener::accept);
	}

	public static boolean isCorrectConditions(List<LootItemCondition> conditions, ItemStack toolItem) {
		if (conditions.size() != 1) {
			return false;
		}
		LootItemCondition condition = conditions.getFirst();
		if (condition instanceof MatchTool matchTool) {
			ItemPredicate itemPredicate = matchTool.predicate().orElse(null);
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

	public static String defaultEnergyUnit() {
		return "E";
	}

	public static void sendPacket(ServerPlayer player, CustomPacketPayload payload) {
		ServerPlayNetworking.send(player, payload);
	}

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
		return null;
	}

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(ReceiveDataPacket.TYPE, ReceiveDataPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestBlockPacket.TYPE, RequestBlockPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestEntityPacket.TYPE, RequestEntityPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(ClientHandshakePacket.TYPE, ClientHandshakePacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ServerHandshakePacket.TYPE, ServerHandshakePacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ShowOverlayPacket.TYPE, ShowOverlayPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(
				RequestEntityPacket.TYPE, (payload, context) -> {
					RequestEntityPacket.handle(payload, context::player);
				});
		ServerPlayNetworking.registerGlobalReceiver(
				RequestBlockPacket.TYPE, (payload, context) -> {
					RequestBlockPacket.handle(payload, context::player);
				});
		ServerPlayNetworking.registerGlobalReceiver(
				ClientHandshakePacket.TYPE, (payload, context) -> {
					ClientHandshakePacket.handle(payload, context::player);
				});

		CommandRegistrationCallback.EVENT.register(CommonProxy::registerServerCommand);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (server.isDedicatedServer()) {
				loadComplete();
			}
		});
	}

	public record Entrypoint(EntrypointContainer<IWailaPlugin> container) {
		public String className() {
			return container.getDefinition();
		}

		public String modId() {
			return container.getProvider().getMetadata().getId();
		}

		public String modName() {
			return container.getProvider().getMetadata().getName();
		}

		public String requiredMod() {
			try {
				Class<?> clazz = Class.forName(className());
				WailaPlugin a = clazz.getDeclaredAnnotation(WailaPlugin.class);
				return a == null ? "" : a.value();
			} catch (ClassNotFoundException e) {
				return "";
			}
		}

		public IWailaPlugin newInstance() {
			try {
				return container.getEntrypoint();
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
