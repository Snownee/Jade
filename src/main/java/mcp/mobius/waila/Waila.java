package mcp.mobius.waila;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.GsonBuilder;

import mcp.mobius.waila.addons.core.PluginCore;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import mcp.mobius.waila.api.impl.config.WailaConfig;
import mcp.mobius.waila.command.CommandDumpHandlers;
import mcp.mobius.waila.network.MessageReceiveData;
import mcp.mobius.waila.network.MessageRequestEntity;
import mcp.mobius.waila.network.MessageRequestTile;
import mcp.mobius.waila.network.MessageServerPing;
import mcp.mobius.waila.utils.JsonConfig;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod(Waila.MODID)
public class Waila {

	public static final String MODID = "waila";
	public static final String NAME = "Waila";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(MODID, "networking")).clientAcceptedVersions(s -> true).serverAcceptedVersions(s -> true).networkProtocolVersion(() -> "1.0.0").simpleChannel();
	public static final JsonConfig<WailaConfig> CONFIG = new JsonConfig<>(MODID + "/" + MODID, WailaConfig.class).withGson(new GsonBuilder().setPrettyPrinting().registerTypeAdapter(WailaConfig.ConfigOverlay.ConfigOverlayColor.class, new WailaConfig.ConfigOverlay.ConfigOverlayColor.Adapter()).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create());

	public Waila() {
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(this::playerJoin);
	}

	@SubscribeEvent
	public void setup(FMLCommonSetupEvent event) {
		NETWORK.registerMessage(0, MessageReceiveData.class, MessageReceiveData::write, MessageReceiveData::read, MessageReceiveData.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NETWORK.registerMessage(1, MessageServerPing.class, MessageServerPing::write, MessageServerPing::read, MessageServerPing.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NETWORK.registerMessage(2, MessageRequestEntity.class, MessageRequestEntity::write, MessageRequestEntity::read, MessageRequestEntity.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		NETWORK.registerMessage(3, MessageRequestTile.class, MessageRequestTile::write, MessageRequestTile::read, MessageRequestTile.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_SERVER));
	}

	@SubscribeEvent
	public void setupClient(FMLClientSetupEvent event) {
		WailaClient.initClient();
	}

	@SubscribeEvent
	public void loadComplete(FMLLoadCompleteEvent event) {
		new PluginCore().register(WailaRegistrar.INSTANCE);
		ModList.get().getAllScanData().forEach(scan -> {
			scan.getAnnotations().forEach(a -> {
				if (a.getAnnotationType().getClassName().equals(WailaPlugin.class.getName())) {
					String required = (String) a.getAnnotationData().getOrDefault("value", "");
					if (required.isEmpty() || ModList.get().isLoaded(required)) {
						try {
							Class<?> clazz = Class.forName(a.getMemberName());
							if (IWailaPlugin.class.isAssignableFrom(clazz)) {
								IWailaPlugin plugin = (IWailaPlugin) clazz.newInstance();
								plugin.register(WailaRegistrar.INSTANCE);
								LOGGER.info("Registered plugin at {}", a.getMemberName());
							}
						} catch (Throwable e) {
							LOGGER.error("Error loading plugin at {}", a.getMemberName(), e);
						}
					}
				}
			});
		});

		PluginConfig.INSTANCE.reload();
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent event) {
		CommandDumpHandlers.register(event.getDispatcher());
	}

	@SubscribeEvent
	public void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		LOGGER.info("Syncing config to {} ({})", event.getPlayer().getGameProfile().getName(), event.getPlayer().getGameProfile().getId());
		NETWORK.sendTo(new MessageServerPing(PluginConfig.INSTANCE), ((ServerPlayerEntity) event.getPlayer()).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
	}
}
