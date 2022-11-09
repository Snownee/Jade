package snownee.jade.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.api.ui.IElement;
import snownee.jade.command.DumpHandlersCommand;
import snownee.jade.compat.JEICompat;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.ui.FluidStackElement;
import snownee.jade.network.RequestEntityPacket;
import snownee.jade.network.RequestTilePacket;
import snownee.jade.overlay.DatapackBlockManager;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.overlay.WailaTickHandler;

public final class ClientPlatformProxy {

	public static boolean hasJEI = isModLoaded("jei");
	public static boolean hasREI = isModLoaded("roughlyenoughitems");

	private static boolean isModLoaded(String modid) {
		try {
			return ModList.get().isLoaded(modid);
		} catch (Throwable e) {
			return false;
		}
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
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onGui);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, ClientPlatformProxy::onDrawBossBar);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientPlatformProxy::onKeyMappingEvent);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientPlatformProxy::onRegisterReloadListener);
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((minecraft, screen) -> new HomeConfigScreen(screen)));
	}

	private static void onEntityJoin(EntityJoinLevelEvent event) {
		DatapackBlockManager.onEntityJoin(event.getEntity());
	}

	private static void onEntityLeave(EntityLeaveLevelEvent event) {
		DatapackBlockManager.onEntityLeave(event.getEntity());
	}

	private static void onTooltip(ItemTooltipEvent event) {
		JadeClient.onTooltip(event.getToolTip(), event.getItemStack());
	}

	private static void onRenderTick(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			OverlayRenderer.renderOverlay478757(new PoseStack());
			bossbarShown = false;
		}
	}

	private static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			WailaTickHandler.instance().tickClient();
	}

	private static void onPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
		ObjectDataCenter.serverConnected = false;
	}

	public static void registerCommands(RegisterClientCommandsEvent event) {
		DumpHandlersCommand.register(event.getDispatcher());
	}

	private static void onKeyPressed(InputEvent.Key event) {
		JadeClient.onKeyPressed(event.getAction());
		if (JadeClient.showUses != null) {
			//REICompat.onKeyPressed(1);
			if (hasJEI) {
				JEICompat.onKeyPressed(1);
			}
		}
	}

	private static void onGui(ScreenEvent.Init event) {
		JadeClient.onGui(event.getScreen());
	}

	private static final List<KeyMapping> keys = Lists.newArrayList();

	public static KeyMapping registerKeyBinding(String desc, int defaultKey) {
		KeyMapping key = new KeyMapping("key.jade." + desc, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(defaultKey), Jade.NAME);
		keys.add(key);
		return key;
	}

	private static void onKeyMappingEvent(RegisterKeyMappingsEvent event) {
		keys.forEach(event::register);
		keys.clear();
	}

	public static boolean shouldRegisterRecipeViewerKeys() {
		return hasJEI || hasREI;
	}

	public static void requestBlockData(BlockEntity blockEntity, boolean showDetails) {
		Jade.NETWORK.sendToServer(new RequestTilePacket(blockEntity, showDetails));
	}

	public static void requestEntityData(Entity entity, boolean showDetails) {
		Jade.NETWORK.sendToServer(new RequestEntityPacket(entity, showDetails));
	}

	public static ItemStack getEntityPickedResult(Entity entity, Player player, EntityHitResult hitResult) {
		return entity.getPickedResult(hitResult);
	}

	public static ItemStack getBlockPickedResult(BlockState state, Player player, BlockHitResult hitResult) {
		return state.getCloneItemStack(hitResult, player.level, hitResult.getBlockPos(), player);
	}

	public static IElement elementFromLiquid(LiquidBlock block) {
		Fluid fluid = block.getFluid();
		FluidStack fluidStack = new FluidStack(fluid, 1);
		return new FluidStackElement(fluidStack);//.size(new Size(18, 18));
	}

	private static final List<PreparableReloadListener> listeners = Lists.newArrayList();

	public static void registerReloadListener(ResourceManagerReloadListener listener) {
		listeners.add(listener);
	}

	private static void onRegisterReloadListener(RegisterClientReloadListenersEvent event) {
		listeners.forEach(event::registerReloadListener);
		listeners.clear();
	}

	private static boolean bossbarShown;
	private static int bossbarHeight;

	private static void onDrawBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
		BossBarOverlapMode mode = Jade.CONFIG.get().getGeneral().getBossBarOverlapMode();
		if (mode == BossBarOverlapMode.NO_OPERATION)
			return;
		if (mode == BossBarOverlapMode.HIDE_BOSS_BAR && OverlayRenderer.shown) {
			event.setCanceled(true);
			return;
		}
		if (mode == BossBarOverlapMode.PUSH_DOWN) {
			if (event.isCanceled())
				return;
			bossbarHeight = event.getY() + event.getIncrement();
			bossbarShown = true;
		}
	}

	@Nullable
	public static Rect2i getBossBarRect() {
		if (!bossbarShown)
			return null;
		int i = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int k = i / 2 - 91;
		return new Rect2i(k, 12, 182, bossbarHeight - 12);
	}

	public static boolean isShowDetailsPressed() {
		return JadeClient.showDetails.isDown();
	}

	public static boolean shouldShowWithOverlay(Minecraft mc, @Nullable Screen screen) {
		return screen == null || screen instanceof BaseOptionsScreen || screen instanceof ChatScreen;
	}
}
