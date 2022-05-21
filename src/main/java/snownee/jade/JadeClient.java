package snownee.jade;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.TTSMode;
import snownee.jade.command.DumpHandlersCommand;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.overlay.WailaTickHandler;
import snownee.jade.util.ModIdentification;

@EventBusSubscriber(Dist.CLIENT)
public final class JadeClient {

	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		ModLoadingContext.get().registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory((minecraft, screen) -> new HomeConfigScreen(screen)));
	}

	public static KeyMapping openConfig;
	public static KeyMapping showOverlay;
	public static KeyMapping toggleLiquid;
	public static KeyMapping showDetails;
	public static KeyMapping narrate;

	public static void initClient() {
		ModLoadingContext.get().registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory((minecraft, screen) -> new HomeConfigScreen(screen)));

		openConfig = new KeyMapping("key.jade.config", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(320), Jade.NAME);
		showOverlay = new KeyMapping("key.jade.show_overlay", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(321), Jade.NAME);
		toggleLiquid = new KeyMapping("key.jade.toggle_liquid", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(322), Jade.NAME);
		narrate = new KeyMapping("key.jade.narrate", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(326), Jade.NAME);
		showDetails = new KeyMapping("key.jade.show_details", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(340), Jade.NAME);

		ClientRegistry.registerKeyBinding(openConfig);
		ClientRegistry.registerKeyBinding(showOverlay);
		ClientRegistry.registerKeyBinding(toggleLiquid);
		ClientRegistry.registerKeyBinding(narrate);
		ClientRegistry.registerKeyBinding(showDetails);

		((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(ModIdentification.INSTANCE);
	}

	@SubscribeEvent
	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
		if (event.getAction() != 1)
			return;

		if (openConfig.isDown()) {
			Jade.CONFIG.invalidate();
			Minecraft.getInstance().setScreen(new HomeConfigScreen(null));
		}

		if (showOverlay.isDown()) {
			DisplayMode mode = Jade.CONFIG.get().getGeneral().getDisplayMode();
			if (mode == IWailaConfig.DisplayMode.TOGGLE) {
				Jade.CONFIG.get().getGeneral().setDisplayTooltip(!Jade.CONFIG.get().getGeneral().shouldDisplayTooltip());
			}
		}

		if (toggleLiquid.isDown()) {
			Jade.CONFIG.get().getGeneral().setDisplayFluids(!Jade.CONFIG.get().getGeneral().shouldDisplayFluids());
		}

		if (narrate.isDown()) {
			if (Jade.CONFIG.get().getGeneral().getTTSMode() == TTSMode.TOGGLE) {
				Jade.CONFIG.get().getGeneral().toggleTTS();
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
		if (Jade.CONFIG.get().getGeneral().isDebug() && event.getItemStack().hasTag()) {
			event.getToolTip().add(NbtUtils.toPrettyComponent(event.getItemStack().getTag()));
		}
	}

	private static void appendModName(ItemTooltipEvent event) {
		if (hideModName || !Jade.CONFIG.get().getGeneral().showItemModNameTooltip())
			return;
		String name = String.format(Jade.CONFIG.get().getFormatting().getModName(), ModIdentification.getModName(event.getItemStack()));
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

	@SubscribeEvent
	public static void registerCommands(RegisterClientCommandsEvent event) {
		DumpHandlersCommand.register(event.getDispatcher());
	}
}
