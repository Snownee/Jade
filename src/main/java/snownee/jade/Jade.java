package snownee.jade;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.Theme;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.test.ExamplePlugin;
import snownee.jade.util.JsonConfig;
import snownee.jade.util.PlatformProxy;
import snownee.jade.util.ThemeSerializer;
import snownee.jade.util.UsernameCache;
import snownee.jade.util.WailaExceptionHandler;

public class Jade implements ModInitializer {
	public static final String MODID = "jade";
	public static final String NAME = "Jade";
	public static final Logger LOGGER = LogManager.getLogger(NAME);

	@ScheduledForRemoval(inVersion = "1.20")
	public static final Vec2 SMALL_ITEM_SIZE = new Vec2(10, 10);
	@ScheduledForRemoval(inVersion = "1.20")
	public static final Vec2 SMALL_ITEM_OFFSET = Vec2.NEG_UNIT_Y;

	/**
	 * addons: Use {@link snownee.jade.api.IWailaClientRegistration#getConfig}
	 */
	/* off */
	public static final JsonConfig<WailaConfig> CONFIG =
			new JsonConfig<>(Jade.MODID + "/" + Jade.MODID, WailaConfig.class, () -> {
				OverlayRenderer.updateTheme();
			}).withGson(
					new GsonBuilder()
							.setPrettyPrinting()
							.enableComplexMapKeySerialization()
							.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
							.registerTypeAdapter(Theme.class, new ThemeSerializer())
							.setLenient()
							.create()
			);
	/* on */

	@ScheduledForRemoval(inVersion = "1.20")
	public static IElement smallItem(IElementHelper elements, ItemStack stack) {
		return elements.smallItem(stack);
	}

	public static int MAX_DISTANCE_SQR = 900;

	@Override
	public void onInitialize() {
		PlatformProxy.init();

		ServerPlayNetworking.registerGlobalReceiver(Identifiers.PACKET_REQUEST_ENTITY, (server, player, handler, buf, responseSender) -> {
			Level world = player.level;
			Entity entity = world.getEntity(buf.readVarInt());
			boolean showDetails = buf.readBoolean();
			if (entity == null || player.distanceToSqr(entity) > MAX_DISTANCE_SQR)
				return;
			server.execute(() -> {
				List<IServerDataProvider<Entity>> providers = WailaCommonRegistration.INSTANCE.getEntityNBTProviders(entity);
				if (providers.isEmpty())
					return;

				CompoundTag tag = new CompoundTag();
				for (IServerDataProvider<Entity> provider : providers) {
					try {
						provider.appendServerData(tag, player, world, entity, showDetails);
					} catch (Exception e) {
						WailaExceptionHandler.handleErr(e, provider, null);
					}
				}

				tag.putInt("WailaEntityID", entity.getId());

				FriendlyByteBuf re = PacketByteBufs.create();
				re.writeNbt(tag);
				responseSender.sendPacket(Identifiers.PACKET_RECEIVE_DATA, re);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(Identifiers.PACKET_REQUEST_TILE, (server, player, handler, buf, responseSender) -> {
			BlockPos pos = buf.readBlockPos();
			boolean showDetails = buf.readBoolean();
			Level world = player.level;
			if (pos.distSqr(player.blockPosition()) > MAX_DISTANCE_SQR || !world.isLoaded(pos))
				return;
			server.execute(() -> {
				BlockEntity tile = world.getBlockEntity(pos);
				if (tile == null)
					return;

				List<IServerDataProvider<BlockEntity>> providers = WailaCommonRegistration.INSTANCE.getBlockNBTProviders(tile);
				if (providers.isEmpty())
					return;

				CompoundTag tag = new CompoundTag();
				for (IServerDataProvider<BlockEntity> provider : providers) {
					try {
						provider.appendServerData(tag, player, world, tile, showDetails);
					} catch (Exception e) {
						WailaExceptionHandler.handleErr(e, provider, null);
					}
				}

				tag.putInt("x", pos.getX());
				tag.putInt("y", pos.getY());
				tag.putInt("z", pos.getZ());
				tag.putString("id", PlatformProxy.getId(tile.getType()).toString());

				FriendlyByteBuf re = PacketByteBufs.create();
				re.writeNbt(tag);
				responseSender.sendPacket(Identifiers.PACKET_RECEIVE_DATA, re);
			});
		});

		ServerPlayConnectionEvents.JOIN.register(this::playerJoin);
		UsernameCache.load();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (server.isDedicatedServer()) {
				loadComplete();
			}
		});
	}

	public static void loadComplete() {
		FabricLoader.getInstance().getEntrypointContainers(MODID, IWailaPlugin.class).forEach(entrypoint -> {
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			LOGGER.info("Start loading plugin from {}", metadata.getName());
			String className = null;
			try {
				IWailaPlugin plugin = entrypoint.getEntrypoint();
				WailaPlugin a = plugin.getClass().getDeclaredAnnotation(WailaPlugin.class);
				if (a != null && !Strings.isNullOrEmpty(a.value()) && !FabricLoader.getInstance().isModLoaded(a.value()))
					return;
				className = plugin.getClass().getName();
				plugin.register(WailaCommonRegistration.INSTANCE);
				if (PlatformProxy.isPhysicallyClient()) {
					plugin.registerClient(WailaClientRegistration.INSTANCE);
				}
			} catch (Throwable e) {
				LOGGER.error("Error loading plugin at {}", className, e);
			}
		});
		if (PlatformProxy.isDevEnv()) {
			try {
				IWailaPlugin plugin = new ExamplePlugin();
				plugin.register(WailaCommonRegistration.INSTANCE);
				if (PlatformProxy.isPhysicallyClient()) {
					plugin.registerClient(WailaClientRegistration.INSTANCE);
				}
			} catch (Throwable e) {
			}
		}

		WailaCommonRegistration.INSTANCE.priorities.sort(PluginConfig.INSTANCE.getKeys());
		WailaCommonRegistration.INSTANCE.loadComplete();
		if (PlatformProxy.isPhysicallyClient()) {
			WailaClientRegistration.INSTANCE.loadComplete();
			ConfigGeneral.init();
		}
		PluginConfig.INSTANCE.reload();
	}

	private void playerJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
		ServerPlayer player = handler.player;
		LOGGER.info("Syncing config to {} ({})", player.getGameProfile().getName(), player.getGameProfile().getId());
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUtf(Strings.nullToEmpty(PluginConfig.INSTANCE.getServerConfigs()));
		ServerPlayNetworking.send(player, Identifiers.PACKET_SERVER_PING, buf);

		if (server.isDedicatedServer())
			UsernameCache.setUsername(player.getUUID(), player.getGameProfile().getName());
	}

}
