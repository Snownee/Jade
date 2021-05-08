package mcp.mobius.waila;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.GsonBuilder;

import mcp.mobius.waila.addons.core.CorePlugin;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.command.DumpHandlersCommand;
import mcp.mobius.waila.impl.WailaRegistrar;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.network.ReceiveDataPacket;
import mcp.mobius.waila.network.RequestEntityPacket;
import mcp.mobius.waila.network.RequestTilePacket;
import mcp.mobius.waila.network.ServerPingPacket;
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
import snownee.jade.Jade;

@Mod(Waila.MODID)
public class Waila {

	public static final String MODID = "waila";
	public static final String NAME = "Waila";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(MODID, "networking")).clientAcceptedVersions(s -> true).serverAcceptedVersions(s -> true).networkProtocolVersion(() -> "1.0.0").simpleChannel();
	/** addons: Use {@link mcp.mobius.waila.api.IRegistrar#getConfig} */
	/* off */
	public static final JsonConfig<WailaConfig> CONFIG = 
			new JsonConfig<>(Jade.MODID + "/" + Jade.MODID, WailaConfig.class).withGson(
					new GsonBuilder()
					.setPrettyPrinting()
					.enableComplexMapKeySerialization()
					.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
					.create()
			);
	/* on */

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
		NETWORK.registerMessage(0, ReceiveDataPacket.class, ReceiveDataPacket::write, ReceiveDataPacket::read, ReceiveDataPacket.Handler::onMessage);
		NETWORK.registerMessage(1, ServerPingPacket.class, ServerPingPacket::write, ServerPingPacket::read, ServerPingPacket.Handler::onMessage);
		NETWORK.registerMessage(2, RequestEntityPacket.class, RequestEntityPacket::write, RequestEntityPacket::read, RequestEntityPacket.Handler::onMessage);
		NETWORK.registerMessage(3, RequestTilePacket.class, RequestTilePacket::write, RequestTilePacket::read, RequestTilePacket.Handler::onMessage);
	}

	@SubscribeEvent
	public void setupClient(FMLClientSetupEvent event) {
		WailaClient.initClient();
	}

	@SubscribeEvent
	public void loadComplete(FMLLoadCompleteEvent event) {
		new CorePlugin().register(WailaRegistrar.INSTANCE);
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
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
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
		DumpHandlersCommand.register(event.getDispatcher());
	}

	@SubscribeEvent
	public void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		LOGGER.info("Syncing config to {} ({})", event.getPlayer().getGameProfile().getName(), event.getPlayer().getGameProfile().getId());
		NETWORK.sendTo(new ServerPingPacket(PluginConfig.INSTANCE), ((ServerPlayerEntity) event.getPlayer()).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
	}
}
