package snownee.jade;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import snownee.jade.addon.core.CorePlugin;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.TTSMode;
import snownee.jade.api.config.Theme;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.overlay.WailaTickHandler;
import snownee.jade.util.JsonConfig;
import snownee.jade.util.ModIdentification;
import snownee.jade.util.ThemeSerializer;

@Mod.EventBusSubscriber(modid = Waila.MODID, value = Dist.CLIENT)
public class WailaClient {

	/** addons: Use {@link mcp.mobius.waila.api.IWailaClientRegistration#getConfig} */
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

	public static KeyMapping openConfig;
	public static KeyMapping showOverlay;
	public static KeyMapping toggleLiquid;
	public static KeyMapping showDetails;
	public static KeyMapping narrate;

	public static void initClient() {
		ModLoadingContext.get().registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory((minecraft, screen) -> new HomeConfigScreen(screen)));

		WailaClient.openConfig = new KeyMapping("key.waila.config", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(320), Jade.NAME);
		WailaClient.showOverlay = new KeyMapping("key.waila.show_overlay", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(321), Jade.NAME);
		WailaClient.toggleLiquid = new KeyMapping("key.waila.toggle_liquid", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(322), Jade.NAME);
		WailaClient.narrate = new KeyMapping("key.waila.narrate", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(326), Jade.NAME);
		WailaClient.showDetails = new KeyMapping("key.waila.show_details", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(340), Jade.NAME);

		ClientRegistry.registerKeyBinding(WailaClient.openConfig);
		ClientRegistry.registerKeyBinding(WailaClient.showOverlay);
		ClientRegistry.registerKeyBinding(WailaClient.toggleLiquid);
		ClientRegistry.registerKeyBinding(WailaClient.narrate);
		ClientRegistry.registerKeyBinding(WailaClient.showDetails);

		((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(ModIdentification.INSTANCE);
	}

	@SubscribeEvent
	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
		if (event.getAction() != 1)
			return;

		if (openConfig.isDown()) {
			CONFIG.invalidate();
			Minecraft.getInstance().setScreen(new HomeConfigScreen(null));
		}

		if (showOverlay.isDown()) {
			DisplayMode mode = CONFIG.get().getGeneral().getDisplayMode();
			if (mode == IWailaConfig.DisplayMode.TOGGLE) {
				CONFIG.get().getGeneral().setDisplayTooltip(!CONFIG.get().getGeneral().shouldDisplayTooltip());
			}
		}

		if (toggleLiquid.isDown()) {
			CONFIG.get().getGeneral().setDisplayFluids(!CONFIG.get().getGeneral().shouldDisplayFluids());
		}

		if (narrate.isDown()) {
			if (CONFIG.get().getGeneral().getTTSMode() == TTSMode.TOGGLE) {
				CONFIG.get().getGeneral().toggleTTS();
			} else if (WailaTickHandler.instance().tooltipRenderer != null) {
				WailaTickHandler.narrate(WailaTickHandler.instance().tooltipRenderer.getTooltip(), false);
			}
		}
	}

	//public static boolean hasJEI = ModList.get().isLoaded("jei");
	public static boolean hideModName;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onTooltip(ItemTooltipEvent event) {
		appendModName(event);
		if (CONFIG.get().getGeneral().isDebug() && event.getItemStack().hasTag()) {
			event.getToolTip().add(NbtUtils.toPrettyComponent(event.getItemStack().getTag()));
		}
	}

	private static void appendModName(ItemTooltipEvent event) {
		if (hideModName || !PluginConfig.INSTANCE.get(CorePlugin.CONFIG_ITEM_MOD_NAME, false))
			return;
		//		if (hasJEI) {
		//			if (JEIClientConfig.modNameFormat.modNameFormat != "") {
		//				StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		//				int i = 0;
		//				for (StackTraceElement element : stackTrace) {
		//					if (++i > 12) {
		//						break;
		//					}
		//					if (element.getMethodName().equals("getIngredientTooltipSafe")) {
		//						return;
		//					}
		//				}
		//			}
		//		}
		String name = String.format(CONFIG.get().getFormatting().getModName(), ModIdentification.getModName(event.getItemStack()));
		event.getToolTip().add(new TextComponent(name));
	}

	@SubscribeEvent
	public static void onRenderTick(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			OverlayRenderer.renderOverlay();
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			WailaTickHandler.instance().tickClient();
	}

	@SubscribeEvent
	public static void onPlayerLeave(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		ObjectDataCenter.serverConnected = false;
	}
}
