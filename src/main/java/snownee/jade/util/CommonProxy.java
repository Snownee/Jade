package snownee.jade.util;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.EntityPickInteractionAware;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import snownee.jade.Jade;
import snownee.jade.addon.universal.ItemCollector;
import snownee.jade.addon.universal.ItemIterator;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.command.JadeServerCommand;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.impl.EntityAccessorImpl;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.mixin.AbstractHorseAccess;

public final class CommonProxy implements ModInitializer {

	public static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("c:bosses"));
	public static boolean hasTechRebornEnergy = isModLoaded("team_reborn_energy");

	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		return UsernameCache.getLastKnownUsername(uuid);
	}

	public static File getConfigDirectory() {
		return FabricLoader.getInstance().getConfigDir().toFile();
	}

	public static boolean isShears(ItemStack tool) {
		return tool.getItem() instanceof ShearsItem;
	}

	public static boolean isShearable(BlockState state) {
		return state.is(FabricMineableTags.SHEARS_MINEABLE);
	}

	public static boolean isCorrectToolForDrops(BlockState state, Player player) {
		return player.hasCorrectToolForDrops(state);
	}

	public static String getModIdFromItem(ItemStack stack) {
		if (stack.getTag() != null && stack.getTag().contains("id")) {
			String s = stack.getTag().getString("id");
			if (s.contains(":")) {
				ResourceLocation id = ResourceLocation.tryParse(s);
				if (id != null) {
					return id.getNamespace();
				}
			}
		}
		if (stack.getItem() instanceof EnchantedBookItem) {
			ListTag listTag = EnchantedBookItem.getEnchantments(stack);
			String modid = null;
			for (int i = 0; i < listTag.size(); i++) {
				ResourceLocation enchantmentId = EnchantmentHelper.getEnchantmentId(listTag.getCompound(i));
				if (enchantmentId != null) {
					String namespace = enchantmentId.getNamespace();
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
		if (stack.getItem() instanceof PotionItem || stack.getItem() instanceof TippedArrowItem) {
			Potion potion = PotionUtils.getPotion(stack);
			return BuiltInRegistries.POTION.getKey(potion).getNamespace();
		}
		if (stack.is(Items.PAINTING)) {
			CompoundTag compoundTag = stack.getTag();
			if (compoundTag != null && compoundTag.contains("EntityTag", 10)) {
				CompoundTag compoundTag2 = compoundTag.getCompound("EntityTag");
				return Painting.loadVariant(compoundTag2)
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

	@SuppressWarnings("UnstableApiUsage")
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
				WailaExceptionHandler.handleErr(e, null, null);
			}
		}
		if (target instanceof Container) {
			if (target instanceof ChestBlockEntity) {
				return new ItemCollector<>(new ItemIterator.ContainerItemIterator(o -> {
					if (o instanceof ChestBlockEntity be) {
						if (be.getBlockState().getBlock() instanceof ChestBlock chestBlock) {
							Container compound = ChestBlock.getContainer(chestBlock, be.getBlockState(), be.getLevel(), be.getBlockPos(), false);
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
			return ItemStorageProvider.INSTANCE.containerCache.get(container, () -> new ItemCollector<>(new ItemIterator.ContainerItemIterator(0))).update(container, accessor.getLevel().getGameTime());
		} catch (ExecutionException e) {
			return null;
		}
	}

	@Nullable
	@SuppressWarnings("UnstableApiUsage")
	public static List<ViewGroup<ItemStack>> storageGroup(Object storage, Accessor<?> accessor) {
		try {
			return ItemStorageProvider.INSTANCE.containerCache.get(storage, () -> new ItemCollector<>(JadeFabricUtils.fromItemStorage((Storage<ItemVariant>) storage, 0))).update(storage, accessor.getLevel().getGameTime());
		} catch (ExecutionException e) {
			return null;
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	public static List<ViewGroup<CompoundTag>> wrapFluidStorage(Accessor<?> accessor, Object target) {
		if (target instanceof BlockEntity be) {
			try {
				var storage = FluidStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, null);
				if (storage != null) {
					return JadeFabricUtils.fromFluidStorage(storage);
				}
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, null, null);
			}
		}
		return null;
	}

	public static List<ViewGroup<CompoundTag>> wrapEnergyStorage(Accessor<?> accessor, Object target) {
		if (hasTechRebornEnergy && target instanceof BlockEntity be) {
			try {
				var storage = TechRebornEnergyCompat.getSided().find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, null);
				if (storage != null && storage.getCapacity() > 0) {
					var group = new ViewGroup<>(List.of(EnergyView.of(storage.getAmount(), storage.getCapacity())));
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

	public static ResourceLocation getId(PaintingVariant motive) {
		return BuiltInRegistries.PAINTING_VARIANT.getKey(motive);
	}

	public static String getPlatformIdentifier() {
		return "fabric";
	}

	public static MutableComponent getProfressionName(VillagerProfession profession) {
		return Component.translatable(EntityType.VILLAGER.getDescriptionId() + "." + BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).getPath());
	}

	private static void registerServerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
		JadeServerCommand.register(dispatcher);
	}

	public static boolean isBoss(Entity entity) {
		EntityType<?> entityType = entity.getType();
		return entityType.is(BOSSES) || entityType == EntityType.ENDER_DRAGON || entityType == EntityType.WITHER;
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
		if (!configs.isEmpty()) {
			Jade.LOGGER.debug("Syncing config to {} ({})", player.getGameProfile().getName(), player.getGameProfile().getId());
		}
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUtf(configs);
		ServerPlayNetworking.send(player, Identifiers.PACKET_SERVER_PING, buf);
		if (server.isDedicatedServer() && !(player instanceof FakePlayer)) {
			UsernameCache.setUsername(player.getUUID(), player.getGameProfile().getName());
		}
	}

	public static boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	public static void loadComplete() {
		Set<Class<?>> classes = Sets.newHashSet();
		FabricLoader.getInstance().getEntrypointContainers(Jade.MODID, IWailaPlugin.class).forEach(entrypoint -> {
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			Jade.LOGGER.info("Start loading plugin from {}", metadata.getName());
			String className = null;
			try {
				IWailaPlugin plugin = entrypoint.getEntrypoint();
				WailaPlugin a = plugin.getClass().getDeclaredAnnotation(WailaPlugin.class);
				if (a != null && !Strings.isNullOrEmpty(a.value()) && !isModLoaded(a.value())) {
					return;
				}
				className = plugin.getClass().getName();
				if (className.startsWith("snownee.jade.") && !metadata.getId().startsWith(Jade.MODID)) {
					throw new IllegalStateException("Mod " + metadata.getName() + " is not allowed to register built-in plugins. Please contact the mod author");
				}
				if (!classes.add(plugin.getClass())) {
					throw new IllegalStateException("Duplicate plugin class " + className);
				}
				plugin.register(WailaCommonRegistration.instance());
				if (isPhysicallyClient()) {
					plugin.registerClient(WailaClientRegistration.instance());
				}
			} catch (Throwable e) {
				Jade.LOGGER.error("Error loading plugin at {}", className, e);
				Throwables.throwIfInstanceOf(e, IllegalStateException.class);
			}
		});
		Jade.loadComplete();
	}

	@SuppressWarnings("UnstableApiUsage")
	public static Component getFluidName(JadeFluidObject fluidObject) {
		Fluid fluid = fluidObject.getType();
		CompoundTag nbt = fluidObject.getTag();
		return FluidVariantAttributes.getName(FluidVariant.of(fluid, nbt));
	}

	public static int showOrHideFromServer(Collection<ServerPlayer> players, boolean show) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(show);
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, Identifiers.PACKET_SHOW_OVERLAY, buf);
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

	@Override
	public void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(Identifiers.PACKET_REQUEST_ENTITY, (server, player, handler, buf, responseSender) -> {
			EntityAccessorImpl.SyncData data = new EntityAccessorImpl.SyncData(buf);
			EntityAccessorImpl.handleRequest(data, player, server::execute, tag -> {
				FriendlyByteBuf buf1 = PacketByteBufs.create().writeNbt(tag);
				responseSender.sendPacket(Identifiers.PACKET_RECEIVE_DATA, buf1);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(Identifiers.PACKET_REQUEST_TILE, (server, player, handler, buf, responseSender) -> {
			BlockAccessorImpl.SyncData data = new BlockAccessorImpl.SyncData(buf);
			BlockAccessorImpl.handleRequest(data, player, server::execute, tag -> {
				FriendlyByteBuf buf1 = PacketByteBufs.create().writeNbt(tag);
				responseSender.sendPacket(Identifiers.PACKET_RECEIVE_DATA, buf1);
			});
		});

		CommandRegistrationCallback.EVENT.register(CommonProxy::registerServerCommand);
		ServerPlayConnectionEvents.JOIN.register(CommonProxy::playerJoin);
		UsernameCache.load();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (server.isDedicatedServer()) {
				loadComplete();
			}
		});
	}
}
