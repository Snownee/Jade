package mcp.mobius.waila;

import com.mojang.blaze3d.platform.InputConstants;

import mcp.mobius.waila.addons.core.CorePlugin;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.config.WailaConfig.DisplayMode;
import mcp.mobius.waila.gui.HomeConfigScreen;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.overlay.OverlayRenderer;
import mcp.mobius.waila.overlay.WailaTickHandler;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
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
import net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import snownee.jade.Jade;

@Mod.EventBusSubscriber(modid = Waila.MODID, value = Dist.CLIENT)
public class WailaClient {

	public static KeyMapping openConfig;
	public static KeyMapping showOverlay;
	public static KeyMapping toggleLiquid;
	public static KeyMapping showDetails;

	public static void initClient() {
		ModLoadingContext.get().registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory((minecraft, screen) -> new HomeConfigScreen(screen)));

		WailaClient.openConfig = new KeyMapping("key.waila.config", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(320), Jade.NAME);
		WailaClient.showOverlay = new KeyMapping("key.waila.show_overlay", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(321), Jade.NAME);
		WailaClient.toggleLiquid = new KeyMapping("key.waila.toggle_liquid", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(322), Jade.NAME);
		WailaClient.showDetails = new KeyMapping("key.waila.show_details", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(340), Jade.NAME);

		ClientRegistry.registerKeyBinding(WailaClient.openConfig);
		ClientRegistry.registerKeyBinding(WailaClient.showOverlay);
		ClientRegistry.registerKeyBinding(WailaClient.toggleLiquid);
		ClientRegistry.registerKeyBinding(WailaClient.showDetails);
	}

	@SubscribeEvent
	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
		if (openConfig == null || showOverlay == null || toggleLiquid == null)
			return;
		if (event.getAction() != 1)
			return;

		if (openConfig.isDown()) {
			Waila.CONFIG.invalidate();
			Minecraft.getInstance().setScreen(new HomeConfigScreen(null));
		}

		if (showOverlay.isDown()) {
			DisplayMode mode = Waila.CONFIG.get().getGeneral().getDisplayMode();
			if (mode == WailaConfig.DisplayMode.TOGGLE) {
				Waila.CONFIG.get().getGeneral().setDisplayTooltip(!Waila.CONFIG.get().getGeneral().shouldDisplayTooltip());
			}
		}

		if (toggleLiquid.isDown()) {
			Waila.CONFIG.get().getGeneral().setDisplayFluids(!Waila.CONFIG.get().getGeneral().shouldDisplayFluids());
		}
	}

	public static boolean hideModName;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onTooltip(ItemTooltipEvent event) {
		if (!hideModName && PluginConfig.INSTANCE.get(CorePlugin.CONFIG_ITEM_MOD_NAME, false)) {
			String name = String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModName(event.getItemStack()));
			event.getToolTip().add(new TextComponent(name));
		}
		if (Waila.CONFIG.get().getGeneral().isDebug() && event.getItemStack().hasTag()) {
			event.getToolTip().add(NbtUtils.toPrettyComponent(event.getItemStack().getTag()));
		}
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
