package snownee.jade;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.GsonBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.Theme;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.network.ReceiveDataPacket;
import snownee.jade.network.RequestEntityPacket;
import snownee.jade.network.RequestTilePacket;
import snownee.jade.network.ServerPingPacket;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.util.JsonConfig;
import snownee.jade.util.PlatformProxy;
import snownee.jade.util.ThemeSerializer;

@Mod(Jade.MODID)
public class Jade {
	public static final String MODID = "jade";
	public static final String NAME = "Jade";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(MODID, "networking")).clientAcceptedVersions(s -> true).serverAcceptedVersions(s -> true).networkProtocolVersion(() -> "1").simpleChannel();

	public static final Vec2 SMALL_ITEM_SIZE = new Vec2(10, 10);
	public static final Vec2 SMALL_ITEM_OFFSET = Vec2.NEG_UNIT_Y;

	/** addons: Use {@link mcp.IWailaCommonRegistration.waila.api.IWailaClientRegistration#getConfig} */
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
					.create()
			);
	/* on */

	public static IElement smallItem(IElementHelper elements, ItemStack stack) {
		return elements.item(stack, 0.5F).size(SMALL_ITEM_SIZE).translate(SMALL_ITEM_OFFSET).message(null);
	}

	public Jade() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, JadeCommonConfig.spec, "jade-addons.toml");
		FMLJavaModLoadingContext.get().getModEventBus().register(JadeCommonConfig.class);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		if (PlatformProxy.isPhysicallyClient()) {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
		}
		MinecraftForge.EVENT_BUS.addListener(this::playerJoin);
		PlatformProxy.init();
	}

	private void setup(FMLCommonSetupEvent event) {
		JadeCommonConfig.refresh();
		NETWORK.registerMessage(0, ReceiveDataPacket.class, ReceiveDataPacket::write, ReceiveDataPacket::read, ReceiveDataPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NETWORK.registerMessage(1, ServerPingPacket.class, ServerPingPacket::write, ServerPingPacket::read, ServerPingPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NETWORK.registerMessage(2, RequestEntityPacket.class, RequestEntityPacket::write, RequestEntityPacket::read, RequestEntityPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		NETWORK.registerMessage(3, RequestTilePacket.class, RequestTilePacket::write, RequestTilePacket::read, RequestTilePacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_SERVER));
	}

	private void setupClient(FMLClientSetupEvent event) {
		JadeClient.initClient();
	}

	private void loadComplete(FMLLoadCompleteEvent event) {
		/* off */
		List<String> classNames = ModList.get().getAllScanData()
				.stream()
				.flatMap($ -> $.getAnnotations().stream())
				.filter($ -> {
					if ($.annotationType().getClassName().equals(WailaPlugin.class.getName())) {
						String required = (String) $.annotationData().getOrDefault("value", "");
						if (required.isEmpty() || ModList.get().isLoaded(required)) {
							return true;
						}
					}
					return false;
				})
				.map(AnnotationData::memberName)
				.collect(Collectors.toList());
		/* on */

		for (String className : classNames) {
			LOGGER.info("Start loading plugin at {}", className);
			try {
				Class<?> clazz = Class.forName(className);
				if (IWailaPlugin.class.isAssignableFrom(clazz)) {
					IWailaPlugin plugin = (IWailaPlugin) clazz.getDeclaredConstructor().newInstance();
					plugin.register(WailaCommonRegistration.INSTANCE);
					if (PlatformProxy.isPhysicallyClient()) {
						plugin.registerClient(WailaClientRegistration.INSTANCE);
					}
				}
			} catch (Throwable e) {
				LOGGER.error("Error loading plugin at {}", className, e);
			}
		}

		WailaCommonRegistration.INSTANCE.loadComplete();
		if (PlatformProxy.isPhysicallyClient()) {
			WailaClientRegistration.INSTANCE.loadComplete();
		}
		PluginConfig.INSTANCE.reload();
	}

	private void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		LOGGER.info("Syncing config to {} ({})", event.getEntity().getGameProfile().getName(), event.getEntity().getGameProfile().getId());
		NETWORK.sendTo(new ServerPingPacket(PluginConfig.INSTANCE), ((ServerPlayer) event.getEntity()).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}

}
