package snownee.jade.util;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.command.DumpHandlersCommand;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.overlay.DatapackBlockManager;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.overlay.WailaTickHandler;

public final class ClientPlatformProxy {

	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		return UsernameCache.getLastKnownUsername(uuid);
	}

	public static void initModNames(Map<String, String> map) {
		List<IModInfo> mods = ImmutableList.copyOf(ModList.get().getMods());
		for (IModInfo mod : mods) {
			String modid = mod.getModId();
			String name = mod.getDisplayName();
			if (Strings.isNullOrEmpty(name)) {
				StringUtils.capitalize(modid);
			}
			map.put(modid, name);
		}
	}

	public static void init() {
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onEntityJoin);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onEntityLeave);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, ClientPlatformProxy::onTooltip);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onClientTick);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onRenderTick);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onPlayerLeave);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onKeyPressed);
		ModLoadingContext.get().registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory((minecraft, screen) -> new HomeConfigScreen(screen)));

	}

	public static void onEntityJoin(EntityJoinWorldEvent event) {
		DatapackBlockManager.onEntityJoin(event.getEntity());
	}

	public static void onEntityLeave(EntityLeaveWorldEvent event) {
		DatapackBlockManager.onEntityLeave(event.getEntity());
	}

	public static void onTooltip(ItemTooltipEvent event) {
		JadeClient.onTooltip(event.getToolTip(), event.getItemStack());
	}

	public static void onRenderTick(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			OverlayRenderer.renderOverlay();
	}

	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			WailaTickHandler.instance().tickClient();
	}

	public static void onPlayerLeave(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		ObjectDataCenter.serverConnected = false;
	}

	public static void registerCommands(RegisterClientCommandsEvent event) {
		DumpHandlersCommand.register(event.getDispatcher());
	}

	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
		JadeClient.onKeyPressed(event.getAction());
	}

	public static KeyMapping registerKeyBinding(String desc, int defaultKey) {
		KeyMapping key = new KeyMapping("key.jade." + desc, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(defaultKey), Jade.NAME);
		ClientRegistry.registerKeyBinding(key);
		return key;
	}

	public static boolean shouldRegisterRecipeViewerKeys() {
		return ModList.get().isLoaded("jei");
	}

}
