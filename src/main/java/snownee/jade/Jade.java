package snownee.jade;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.Theme;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.impl.EntityAccessorImpl;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.test.ExamplePlugin;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JsonConfig;
import snownee.jade.util.ThemeSerializer;
import snownee.jade.util.UsernameCache;
import snownee.jade.util.WailaExceptionHandler;

public class Jade implements ModInitializer {
	public static final String MODID = "jade";
	public static final String NAME = "Jade";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	public static int MAX_DISTANCE_SQR = 900;

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
				if (CommonProxy.isPhysicallyClient()) {
					plugin.registerClient(WailaClientRegistration.INSTANCE);
				}
			} catch (Throwable e) {
				LOGGER.error("Error loading plugin at {}", className, e);
			}
		});
		if (CommonProxy.isDevEnv()) {
			try {
				IWailaPlugin plugin = new ExamplePlugin();
				plugin.register(WailaCommonRegistration.INSTANCE);
				if (CommonProxy.isPhysicallyClient()) {
					plugin.registerClient(WailaClientRegistration.INSTANCE);
				}
			} catch (Throwable e) {
			}
		}

		WailaCommonRegistration.INSTANCE.priorities.sort(PluginConfig.INSTANCE.getKeys());
		WailaCommonRegistration.INSTANCE.loadComplete();
		if (CommonProxy.isPhysicallyClient()) {
			WailaClientRegistration.INSTANCE.loadComplete();
			ConfigGeneral.init();
		}
		PluginConfig.INSTANCE.reload();
	}

	@Override
	public void onInitialize() {
		CommonProxy.init();

		ServerPlayNetworking.registerGlobalReceiver(Identifiers.PACKET_REQUEST_ENTITY, (server, player, handler, buf, responseSender) -> {
			EntityAccessor accessor;
			try {
				accessor = EntityAccessorImpl.fromNetwork(buf, player);
			} catch (Exception e) {
				WailaExceptionHandler.handleErr(e, null, null);
				return;
			}
			server.execute(() -> {
				Entity entity = accessor.getEntity();
				if (player.distanceToSqr(entity) > MAX_DISTANCE_SQR)
					return;
				List<IServerDataProvider<EntityAccessor>> providers = WailaCommonRegistration.INSTANCE.getEntityNBTProviders(entity);
				if (providers.isEmpty())
					return;

				CompoundTag tag = accessor.getServerData();
				for (IServerDataProvider<EntityAccessor> provider : providers) {
					try {
						provider.appendServerData(tag, accessor);
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
			BlockAccessor accessor;
			try {
				accessor = BlockAccessorImpl.fromNetwork(buf, player);
			} catch (Exception e) {
				WailaExceptionHandler.handleErr(e, null, null);
				return;
			}
			server.execute(() -> {
				BlockPos pos = accessor.getPosition();
				ServerLevel world = player.serverLevel();
				if (pos.distSqr(player.blockPosition()) > MAX_DISTANCE_SQR || !world.isLoaded(pos))
					return;

				BlockEntity tile = accessor.getBlockEntity();
				if (tile == null)
					return;

				List<IServerDataProvider<BlockAccessor>> providers = WailaCommonRegistration.INSTANCE.getBlockNBTProviders(tile);
				if (providers.isEmpty())
					return;

				CompoundTag tag = accessor.getServerData();
				for (IServerDataProvider<BlockAccessor> provider : providers) {
					try {
						provider.appendServerData(tag, accessor);
					} catch (Exception e) {
						WailaExceptionHandler.handleErr(e, provider, null);
					}
				}

				tag.putInt("x", pos.getX());
				tag.putInt("y", pos.getY());
				tag.putInt("z", pos.getZ());
				tag.putString("id", CommonProxy.getId(tile.getType()).toString());

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

	private void playerJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
		ServerPlayer player = handler.player;
		LOGGER.info("Syncing config to {} ({})", player.getGameProfile().getName(), player.getGameProfile().getId());
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUtf(Strings.nullToEmpty(PluginConfig.INSTANCE.getServerConfigs()));
		ServerPlayNetworking.send(player, Identifiers.PACKET_SERVER_PING, buf);

		if (server.isDedicatedServer())
			UsernameCache.setUsername(player.getUUID(), player.getGameProfile().getName());
	}

	/**
	 * addons: Use {@link IWailaConfig#get()}
	 */
	/* off */
	public static final JsonConfig<WailaConfig> CONFIG =
			new JsonConfig<>(Jade.MODID + "/" + Jade.MODID, WailaConfig.class, OverlayRenderer::updateTheme).withGson(
					new GsonBuilder()
							.setPrettyPrinting()
							.enableComplexMapKeySerialization()
							.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
							.registerTypeAdapter(Theme.class, new ThemeSerializer())
							.setLenient()
							.create()
			);
	/* on */


}
