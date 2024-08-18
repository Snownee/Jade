package snownee.jade.util;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.EntityPickInteractionAware;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import snownee.jade.Jade;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.addon.universal.ItemCollector;
import snownee.jade.addon.universal.ItemIterator;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.command.JadeServerCommand;
import snownee.jade.compat.TechRebornEnergyCompat;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.mixin.AbstractHorseAccess;
import snownee.jade.network.ReceiveDataPacket;
import snownee.jade.network.RequestBlockPacket;
import snownee.jade.network.RequestEntityPacket;
import snownee.jade.network.ServerPingPacket;
import snownee.jade.network.ShowOverlayPacket;

public final class CommonProxy implements ModInitializer {

	public static boolean hasTechRebornEnergy = isModLoaded("team_reborn_energy");

	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		Optional<GameProfile> optional = SkullBlockEntity.fetchGameProfile(uuid).getNow(Optional.empty());
		if (optional.isPresent()) {
			return optional.get().getName();
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
			CustomModelData modelData = stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.DEFAULT);
			if (!CustomModelData.DEFAULT.equals(modelData)) {
				String key = "jade.customModelData.%s.namespace".formatted(modelData.value());
				if (I18n.exists(key)) {
					return I18n.get(key);
				}
			}
		}
		if (stack.getItem() instanceof EnchantedBookItem) {
			ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
			String modid = null;
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
			String modid = null;
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
			CustomData customData = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
			if (!customData.isEmpty()) {
				return customData.read(Painting.VARIANT_MAP_CODEC).result()
						.flatMap(Holder::unwrapKey)
						.map(ResourceKey::location)
						.map(ResourceLocation::getNamespace)
						.orElse(ResourceLocation.DEFAULT_NAMESPACE);
			}
		}
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
	}

	public static boolean isPhysicallyClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	public static ItemCollector<?> createItemCollector(Object target, Cache<Object, ItemCollector<?>> containerCache) {
		if (target instanceof AbstractHorseAccess) {
			return new ItemCollector<>(new ItemIterator.ContainerItemIterator(o -> {
				if (o instanceof AbstractHorseAccess horse) {
					return horse.getInventory();
				}
				return null;
			}, 2));
		}
		if (target instanceof BlockEntity be) {
			try {
				var storage = ItemStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, null);
				if (storage != null) {
					return containerCache.get(storage, () -> new ItemCollector<>(JadeFabricUtils.fromItemStorage(storage, 0)));
				}
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, null, null, null);
			}
		}
		if (target instanceof Container) {
			if (target instanceof ChestBlockEntity) {
				return new ItemCollector<>(new ItemIterator.ContainerItemIterator(o -> {
					if (o instanceof ChestBlockEntity be) {
						if (be.getBlockState().getBlock() instanceof ChestBlock chestBlock) {
							Container compound = ChestBlock.getContainer(
									chestBlock,
									be.getBlockState(),
									be.getLevel(),
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
		try {
			return ItemStorageProvider.containerCache.get(container, () -> new ItemCollector<>(new ItemIterator.ContainerItemIterator(0)))
					.update(container, accessor.getLevel().getGameTime());
		} catch (ExecutionException e) {
			return null;
		}
	}

	@Nullable
	public static List<ViewGroup<ItemStack>> storageGroup(Object storage, Accessor<?> accessor) {
		try {
			return ItemStorageProvider.containerCache.get(
					storage,
					() -> new ItemCollector<>(JadeFabricUtils.fromItemStorage((Storage<ItemVariant>) storage, 0))).update(
					storage,
					accessor.getLevel().getGameTime());
		} catch (ExecutionException e) {
			return null;
		}
	}

	public static List<ViewGroup<CompoundTag>> wrapFluidStorage(Accessor<?> accessor) {
		if (accessor.getTarget() instanceof BlockEntity be) {
			try {
				var storage = FluidStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, null);
				if (storage != null) {
					return JadeFabricUtils.fromFluidStorage(storage);
				}
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, null, null, null);
			}
		}
		return null;
	}

	public static List<ViewGroup<CompoundTag>> wrapEnergyStorage(Accessor<?> accessor) {
		if (hasTechRebornEnergy && accessor.getTarget() instanceof BlockEntity be) {
			try {
				var storage = TechRebornEnergyCompat.getSided().find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, null);
				if (storage != null && storage.getCapacity() > 0) {
					var group = new ViewGroup<>(List.of(EnergyView.of(storage.getAmount(), storage.getCapacity())));
					group.getExtraData().putString("Unit", "E");
					return List.of(group);
				}
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, null, null, null);
			}
		}
		return null;
	}

	public static boolean isDevEnv() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	public static float getEnchantPowerBonus(BlockState state, Level world, BlockPos pos) {
		if (WailaClientRegistration.instance().customEnchantPowers.containsKey(state.getBlock())) {
			return WailaClientRegistration.instance().customEnchantPowers.get(state.getBlock()).getEnchantPowerBonus(state, world, pos);
		}
		return state.is(Blocks.BOOKSHELF) ? 1 : 0;
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

	public static MutableComponent getProfessionName(VillagerProfession profession) {
		return Component.translatable(
				EntityType.VILLAGER.getDescriptionId() + "." + BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).getPath());
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
		Block block = state.getBlock();
		if (block instanceof BlockPickInteractionAware) {
			return ((BlockPickInteractionAware) block).getPickedStack(state, player.level(), hitResult.getBlockPos(), player, hitResult);
		}
		return block.getCloneItemStack(player.level(), hitResult.getBlockPos(), state);
	}

	public static ItemStack getEntityPickedResult(Entity entity, Player player, EntityHitResult hitResult) {
		if (entity instanceof EntityPickInteractionAware) {
			return ((EntityPickInteractionAware) entity).getPickedStack(player, hitResult);
		}
		ItemStack stack = entity.getPickResult();
		return stack == null ? ItemStack.EMPTY : stack;
	}

	private static void playerJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
		ServerPlayer player = handler.player;
		String configs = PluginConfig.INSTANCE.getServerConfigs();
		List<Block> shearableBlocks = HarvestToolProvider.INSTANCE.getShearableBlocks();
		if (!configs.isEmpty()) {
			Jade.LOGGER.debug("Syncing config to {} ({})", player.getGameProfile().getName(), player.getGameProfile().getId());
		}
		ServerPlayNetworking.send(player, new ServerPingPacket(configs, shearableBlocks));
	}

	public static boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	public static void loadComplete() {
		Set<Class<?>> classes = Sets.newHashSet();
		FabricLoader.getInstance().getEntrypointContainers(Jade.ID, IWailaPlugin.class).forEach(entrypoint -> {
			String className = null;
			try {
				ModMetadata metadata = entrypoint.getProvider().getMetadata();
				IWailaPlugin plugin = entrypoint.getEntrypoint();
				className = plugin.getClass().getName();
				Jade.LOGGER.info("Start loading plugin from %s: %s".formatted(metadata.getName(), className));
				WailaPlugin a = plugin.getClass().getDeclaredAnnotation(WailaPlugin.class);
				if (a != null && !Strings.isNullOrEmpty(a.value()) && !isModLoaded(a.value())) {
					return;
				}
				if (className.startsWith("snownee.jade.") && !metadata.getId().startsWith(Jade.ID)) {
					throw new IllegalStateException("Mod %s is not allowed to register built-in plugins. Please contact the mod author".formatted(
							metadata.getName()));
				}
				if (!classes.add(plugin.getClass())) {
					throw new IllegalStateException("Duplicate plugin class " + className);
				}
				Stopwatch stopwatch = null;
				if (CommonProxy.isDevEnv()) {
					stopwatch = Stopwatch.createStarted();
				}
				WailaCommonRegistration common = WailaCommonRegistration.instance();
				common.startSession();
				plugin.register(common);
				if (isPhysicallyClient()) {
					WailaClientRegistration client = WailaClientRegistration.instance();
					client.startSession();
					plugin.registerClient(client);
					if (stopwatch != null) {
						Jade.LOGGER.info("Bootstrapped plugin from %s in %s".formatted(className, stopwatch));
					}
					client.endSession();
				}
				common.endSession();
				if (stopwatch != null) {
					Jade.LOGGER.info("Loaded plugin from %s in %s".formatted(className, stopwatch.stop()));
				}
			} catch (Throwable e) {
				Jade.LOGGER.error("Error loading plugin at %s".formatted(className), e);
				Throwables.throwIfInstanceOf(e, IllegalStateException.class);
				if (entrypoint.getProvider().getMetadata().getId().equals(Jade.ID)) {
					throw e;
				}
			}
		});
		Jade.loadComplete();
	}

	public static Component getFluidName(JadeFluidObject fluidObject) {
		Fluid fluid = fluidObject.getType();
		DataComponentPatch components = fluidObject.getComponents();
		return FluidVariantAttributes.getName(FluidVariant.of(fluid, components));
	}

	public static int showOrHideFromServer(Collection<ServerPlayer> players, boolean show) {
		ShowOverlayPacket packet = new ShowOverlayPacket(show);
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, packet);
		}
		return players.size();
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

	public static void registerTagsUpdatedListener(BiConsumer<RegistryAccess, Boolean> listener) {
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

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(ReceiveDataPacket.TYPE, ReceiveDataPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestBlockPacket.TYPE, RequestBlockPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestEntityPacket.TYPE, RequestEntityPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ServerPingPacket.TYPE, ServerPingPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ShowOverlayPacket.TYPE, ShowOverlayPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(RequestEntityPacket.TYPE, (payload, context) -> {
			RequestEntityPacket.handle(payload, context::player);
		});
		ServerPlayNetworking.registerGlobalReceiver(RequestBlockPacket.TYPE, (payload, context) -> {
			RequestBlockPacket.handle(payload, context::player);
		});

		CommandRegistrationCallback.EVENT.register(CommonProxy::registerServerCommand);
		ServerPlayConnectionEvents.JOIN.register(CommonProxy::playerJoin);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (server.isDedicatedServer()) {
				loadComplete();
			}
		});
	}
}
