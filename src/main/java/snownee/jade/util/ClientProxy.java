package snownee.jade.util;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.ItemDecoratorHandler;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforgespi.language.IModInfo;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.addon.harvest.SimpleToolHandler;
import snownee.jade.addon.harvest.ToolHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElement;
import snownee.jade.command.JadeClientCommand;
import snownee.jade.compat.JEICompat;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.theme.ThemeHelper;
import snownee.jade.impl.ui.FluidStackElement;
import snownee.jade.network.RequestEntityPacket;
import snownee.jade.network.RequestTilePacket;
import snownee.jade.overlay.DatapackBlockManager;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.overlay.WailaTickHandler;

public final class ClientProxy {

	private static final List<KeyMapping> keys = Lists.newArrayList();
	private static final List<PreparableReloadListener> listeners = Lists.newArrayList();
	public static boolean hasJEI = CommonProxy.isModLoaded("jei");
	public static boolean hasREI = false; //isModLoaded("roughlyenoughitems");
	public static boolean hasFastScroll = CommonProxy.isModLoaded("fastscroll");
	public static boolean maybeLowVisionUser = CommonProxy.isModLoaded("minecraft_access");
	private static boolean bossbarShown;
	private static int bossbarHeight;

	public static void initModNames(Map<String, String> map) {
		List<IModInfo> mods = ImmutableList.copyOf(ModList.get().getMods());
		for (IModInfo mod : mods) {
			String modid = mod.getModId();
			String modMenuKey = "modmenu.nameTranslation.%s".formatted(modid);
			if (I18n.exists(modMenuKey)) {
				map.put(modid, I18n.get(modMenuKey));
				continue;
			}
			String name = mod.getDisplayName();
			if (Strings.isNullOrEmpty(name)) {
				name = StringUtils.capitalize(modid);
			}
			map.put(modid, name);
		}
	}

	public static void init() {
		NeoForge.EVENT_BUS.addListener(ClientProxy::onEntityJoin);
		NeoForge.EVENT_BUS.addListener(ClientProxy::onEntityLeave);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ClientProxy::onTooltip);
		NeoForge.EVENT_BUS.addListener(ClientProxy::onClientTick);
		NeoForge.EVENT_BUS.addListener(ClientProxy::onPlayerLeave);
		NeoForge.EVENT_BUS.addListener(ClientProxy::registerCommands);
		NeoForge.EVENT_BUS.addListener(ClientProxy::onKeyPressed);
		NeoForge.EVENT_BUS.addListener(ClientProxy::onGui);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, ClientProxy::onDrawBossBar);
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderGuiEvent.Post.class, event -> {
			if (Minecraft.getInstance().screen == null) {
				onRenderTick(event.getGuiGraphics());
			}
		});
		NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ScreenEvent.Render.Post.class, event -> {
			onRenderTick(event.getGuiGraphics());
		});
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(EventPriority.NORMAL, false, RegisterClientReloadListenersEvent.class, event -> {
			event.registerReloadListener(ThemeHelper.INSTANCE);
			listeners.forEach(event::registerReloadListener);
			listeners.clear();
		});
		modEventBus.addListener(EventPriority.NORMAL, false, RegisterKeyMappingsEvent.class, event -> {
			keys.forEach(event::register);
			keys.clear();
		});
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new HomeConfigScreen(screen)));

		for (int i = 320; i < 330; i++) {
			InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(i);
			//noinspection deprecation
			key.displayName = new LazyLoadedValue<>(() -> Component.translatable(key.getName()));
		}
		JadeClient.init();
	}

	private static void onEntityJoin(EntityJoinLevelEvent event) {
		DatapackBlockManager.onEntityJoin(event.getEntity());
	}

	private static void onEntityLeave(EntityLeaveLevelEvent event) {
		DatapackBlockManager.onEntityLeave(event.getEntity());
	}

	private static void onTooltip(ItemTooltipEvent event) {
		JadeClient.onTooltip(event.getToolTip(), event.getItemStack(), event.getFlags());
	}

	public static void onRenderTick(GuiGraphics guiGraphics) {
		try {
			OverlayRenderer.renderOverlay478757(guiGraphics);
		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
		} finally {
			bossbarShown = false;
		}
	}

	private static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			try {
				WailaTickHandler.instance().tickClient();
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, null, null);
			}
		}
	}

	private static void onPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
		ObjectDataCenter.serverConnected = false;
	}

	public static void registerCommands(RegisterClientCommandsEvent event) {
		JadeClientCommand.register(event.getDispatcher());
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

	private static void onGui(ScreenEvent.Init.Pre event) {
		JadeClient.onGui(event.getScreen());
	}

	public static KeyMapping registerKeyBinding(String desc, int defaultKey) {
		KeyMapping key = new KeyMapping("key.jade." + desc, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(defaultKey), "modmenu.nameTranslation.jade");
		keys.add(key);
		return key;
	}

	public static boolean shouldRegisterRecipeViewerKeys() {
		return hasJEI || hasREI;
	}

	public static void requestBlockData(BlockAccessor accessor) {
		CommonProxy.NETWORK.sendToServer(new RequestTilePacket(accessor));
	}

	public static void requestEntityData(EntityAccessor accessor) {
		CommonProxy.NETWORK.sendToServer(new RequestEntityPacket(accessor));
	}

	public static IElement elementFromLiquid(LiquidBlock block) {
		Fluid fluid = block.getFluid();
		return new FluidStackElement(JadeFluidObject.of(fluid));//.size(new Size(18, 18));
	}

	public static void registerReloadListener(ResourceManagerReloadListener listener) {
		listeners.add(listener);
	}

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

	public static void getFluidSpriteAndColor(JadeFluidObject fluid, BiConsumer<@Nullable TextureAtlasSprite, Integer> consumer) {
		Fluid type = fluid.getType();
		FluidStack fluidStack = CommonProxy.toFluidStack(fluid);
		Minecraft minecraft = Minecraft.getInstance();
		IClientFluidTypeExtensions handler = IClientFluidTypeExtensions.of(type);
		ResourceLocation fluidStill = handler.getStillTexture(fluidStack);
		TextureAtlasSprite fluidStillSprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
		int fluidColor = handler.getTintColor(fluidStack);
		if (OverlayRenderer.alpha != 1) {
			fluidColor = IWailaConfig.IConfigOverlay.applyAlpha(fluidColor, OverlayRenderer.alpha);
		}
		consumer.accept(fluidStillSprite, fluidColor);
	}

	public static KeyMapping registerDetailsKeyBinding() {
		return registerKeyBinding("show_details", InputConstants.KEY_LSHIFT);
	}

	public static ToolHandler createSwordToolHandler() {
		return SimpleToolHandler.create(Identifiers.JADE("sword"), false, List.of(Items.WOODEN_SWORD))
			.addBlock(Blocks.COBWEB)
			.addBlock(Blocks.BAMBOO);
	}

	public static void renderItemDecorationsExtra(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y, String text) {
		ItemDecoratorHandler.of(stack).render(guiGraphics, font, stack, x, y);
	}

	public static InputConstants.Key getBoundKeyOf(KeyMapping keyMapping) {
		return keyMapping.getKey();
	}
}
