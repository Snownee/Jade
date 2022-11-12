package mcp.mobius.waila;

import com.mojang.blaze3d.platform.InputConstants;

import mcp.mobius.waila.addons.core.CorePlugin;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.config.WailaConfig.ConfigGeneral;
import mcp.mobius.waila.api.config.WailaConfig.ConfigGeneral.TTSMode;
import mcp.mobius.waila.api.config.WailaConfig.DisplayMode;
import mcp.mobius.waila.compat.JEICompat;
import mcp.mobius.waila.gui.HomeConfigScreen;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.overlay.OverlayRenderer;
import mcp.mobius.waila.overlay.WailaTickHandler;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import snownee.jade.Jade;

@Mod.EventBusSubscriber(modid = Waila.MODID, value = Dist.CLIENT)
public class WailaClient {

	private static boolean hasJEI = ModList.get().isLoaded("jei");
	public static boolean hasFastScroll = ModList.get().isLoaded("fastscroll");

	public static KeyMapping openConfig;
	public static KeyMapping showOverlay;
	public static KeyMapping toggleLiquid;
	public static KeyMapping showDetails;
	public static KeyMapping narrate;
	public static KeyMapping showRecipes;
	public static KeyMapping showUses;

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

		if (hasJEI) {
			showRecipes = new KeyMapping("key.waila.show_recipes", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(323), Jade.NAME);
			showUses = new KeyMapping("key.waila.show_uses", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(324), Jade.NAME);
			ClientRegistry.registerKeyBinding(showRecipes);
			ClientRegistry.registerKeyBinding(showUses);
		}

		((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(ModIdentification.INSTANCE);
	}

	@SubscribeEvent
	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
		if (event.getAction() != 1)
			return;

		if (openConfig.isDown()) {
			Waila.CONFIG.invalidate();
			Minecraft.getInstance().setScreen(new HomeConfigScreen(null));
		}

		ConfigGeneral general = Waila.CONFIG.get().getGeneral();
		if (showOverlay.isDown()) {
			DisplayMode mode = general.getDisplayMode();
			if (mode == WailaConfig.DisplayMode.TOGGLE) {
				general.setDisplayTooltip(!general.shouldDisplayTooltip());
				if (!general.shouldDisplayTooltip() && general.hintOverlayToggle) {
					SystemToast.add(Minecraft.getInstance().getToasts(), SystemToastIds.TUTORIAL_HINT, new TranslatableComponent("toast.jade.toggle_hint.1"), new TranslatableComponent("toast.jade.toggle_hint.2", showOverlay.getTranslatedKeyMessage()));
					general.hintOverlayToggle = false;
				}
				Waila.CONFIG.save();
			}
		}

		if (toggleLiquid.isDown()) {
			general.setDisplayFluids(!general.shouldDisplayFluids());
			Waila.CONFIG.save();
		}

		if (narrate.isDown()) {
			if (general.getTTSMode() == TTSMode.TOGGLE) {
				general.toggleTTS();
			} else if (WailaTickHandler.instance().tooltipRenderer != null) {
				WailaTickHandler.narrate(WailaTickHandler.instance().tooltipRenderer.getTooltip(), false);
			}
		}

		if (hasJEI) {
			JEICompat.onKeyPressed(event);
		}
	}

	public static boolean hideModName;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onTooltip(ItemTooltipEvent event) {
		appendModName(event);
		if (Waila.CONFIG.get().getGeneral().isDebug() && event.getItemStack().hasTag()) {
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
		String name = String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModName(event.getItemStack()));
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
