package snownee.jade.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Services;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraftforge.client.ItemDecoratorHandler;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeKeys;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.Rect2f;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.command.JadeClientCommand;
import snownee.jade.compat.PolydexCompat;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.ui.FluidStackElement;
import snownee.jade.mixin.KeyAccess;
import snownee.jade.network.ClientHandshakePacket;
import snownee.jade.overlay.DatapackBlockManager;
import snownee.jade.overlay.OverlayRenderer;

public final class ClientProxy {

	public static JadeMetadata metadata = new ForgeJadeMetadata();
	private static final List<KeyMapping> keys = Lists.newArrayList();
	private static final List<KeyedReloadListener> listeners = Lists.newArrayList();
	private static boolean bossbarShown;
	private static int bossbarHeight;

	public static Optional<String> getModName(String namespace, boolean translate) {
		if (translate) {
			String modMenuKey = "modmenu.nameTranslation.%s".formatted(namespace);
			if (JadeUI.hasTranslation(modMenuKey)) {
				return Optional.of(I18n.get(modMenuKey));
			}
		}
		return ModList.getModContainerById(namespace)
				.map(ModContainer::getModInfo)
				.map(IModInfo::getDisplayName)
				.filter(Predicate.not(Strings::isNullOrEmpty));
	}

	public static void registerCommands(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(JadeClientCommand.create(
				Commands::literal,
				Commands::argument,
				(source, component) -> source.sendSuccess(() -> component, false),
				CommandSourceStack::sendFailure));
	}

	private static void onEntityJoin(EntityJoinLevelEvent event) {
		try {
			DatapackBlockManager.onEntityJoin(event.getEntity());
		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
		}
	}

	private static void onEntityLeave(EntityLeaveLevelEvent event) {
		try {
			DatapackBlockManager.onEntityLeave(event.getEntity());
		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
		}
	}

	private static void onTooltip(RenderTooltipEvent.GatherComponents event) {
		if (event.getItemStack().isEmpty()) {
			return;
		}
		FormattedText name = JadeClient.appendModName(event.getItemStack());
		if (name != null) {
			event.getTooltipElements().add(Either.left(name));
		}
	}

	public static void onRenderTick(GuiGraphicsExtractor guiGraphics, float tickDelta) {
		try {
			OverlayRenderer.renderOverlay478757(guiGraphics, tickDelta);
		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
		} finally {
			bossbarShown = false;
		}
	}

	private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
		try {
			JadeClient.tickHandler().tickClient();
		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
		}
	}

	private static void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
		try {
			sendPacket(new ClientHandshakePacket(Jade.PROTOCOL_VERSION));
		} catch (Exception ignored) {
		}
	}

	private static void onPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
		ObjectDataCenter.serverConnected = false;
		WailaClientRegistration.instance().setServerConfig(Map.of());
	}

	private static void onKeyPressed(InputEvent.Key event) {
		JadeClient.onKeyPressed(event.getAction());
	}

	private static void onGui(ScreenEvent.Init.Pre event) {
		JadeClient.onGui(event.getScreen());
	}

	public static KeyMapping registerKeyBinding(String desc, int defaultKey) {
		// Forge keeps the input type and key code separate, and takes a sort order, where NeoForge
		// passed a resolved InputConstants.Key.
		KeyMapping key = new KeyMapping(
				"key.jade." + desc,
				KeyConflictContext.IN_GAME,
				KeyModifier.NONE,
				InputConstants.Type.KEYSYM,
				defaultKey,
				JadeClient.keyMappingCategory,
				0);
		keys.add(key);
		return key;
	}

	public static boolean shouldRegisterRecipeViewerKeys() {
		return true;
	}

	public static Element elementFromLiquid(BlockState blockState) {
		FluidState fluidState = blockState.getFluidState();
		return new FluidStackElement(JadeFluidObject.of(fluidState.getType()));//.size(new Size(18, 18));
	}

	public static void registerReloadListener(KeyedReloadListener listener) {
		listeners.add(listener);
	}

	// EventBus 7 cancels through the listener's return value rather than a method on the event,
	// so this returns true to cancel and false to let the bar draw.
	private static boolean drawBossBarPre(CustomizeGuiOverlayEvent.BossEventProgress event) {
		IWailaConfig.BossBarOverlapMode mode = Jade.config().general().getBossBarOverlapMode();
		if (mode == IWailaConfig.BossBarOverlapMode.HIDE_BOSS_BAR && OverlayRenderer.shown) {
			return true;
		}
		return false;
	}

	public static void drawBossBarPost(LerpingBossEvent bossEvent, int bottom) {
		// NO-OP
	}

	public static void drawBossBarPostInternal(CustomizeGuiOverlayEvent.BossEventProgress event) {
		IWailaConfig.BossBarOverlapMode mode = Jade.config().general().getBossBarOverlapMode();
		if (mode == IWailaConfig.BossBarOverlapMode.PUSH_DOWN) {
			bossbarHeight = event.getY() + event.getIncrement();
			bossbarShown = true;
		}
	}

	@Nullable
	public static Rect2f getBossBarRect() {
		if (!bossbarShown) {
			return null;
		}
		int i = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int k = i / 2 - 91;
		return new Rect2f(k, 0, 182, bossbarHeight - 12);
	}

	public static boolean isShowDetailsPressed() {
		return JadeKeys.showDetails().isDown();
	}

	public static boolean shouldHideWithGui(Minecraft mc, @Nullable Screen screen) {
		return screen != null && !shouldShowBeforeGui(mc, screen) && !shouldShowAfterGui(mc, screen);
	}

	public static boolean shouldShowAfterGui(Minecraft mc, Screen screen) {
		return screen instanceof PreviewOptionsScreen || JadeUI.isPinned();
	}

	public static boolean shouldShowBeforeGui(Minecraft mc, Screen screen) {
		if (mc.level == null || mc.gui.screen() instanceof GenericMessageScreen || mc.gui.screen() instanceof ProgressScreen) {
			return false;
		}
		IWailaConfig.General config = IWailaConfig.get().general();
		return !config.shouldHideFromGUIs();
	}

	public static void getFluidSpriteAndColor(JadeFluidObject fluid, BiConsumer<@Nullable TextureAtlasSprite, Integer> consumer) {
		Fluid type = fluid.typeHolder().value();
		FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(type.defaultFluidState());
		TextureAtlasSprite sprite = model.stillMaterial().sprite();
		BlockTintSource tintSource = model.tintSource();
		int fluidColor = tintSource == null ? -1 : tintSource.colorAsTerrainParticle(
				type.defaultFluidState().createLegacyBlock(),
				Objects.requireNonNull(Minecraft.getInstance().level),
				BlockPos.ZERO);
		consumer.accept(sprite, fluidColor);
	}

	public static void renderItemDecorationsExtra(GuiGraphicsExtractor guiGraphics, Font font, ItemStack stack, int x, int y, String text) {
		ItemDecoratorHandler.of(stack).render(guiGraphics, font, stack, x, y);
	}

	public static GameType getGameMode() {
		MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
		return gameMode == null ? GameType.SURVIVAL : gameMode.getPlayerMode();
	}

	@Nullable
	public static <IN, OUT> List<ClientViewGroup<OUT>> mapToClientGroups(
			Accessor<?> accessor,
			Identifier key,
			StreamCodec<RegistryFriendlyByteBuf, Map.Entry<Identifier, List<ViewGroup<IN>>>> codec,
			Function<Identifier, @Nullable IClientExtensionProvider<IN, OUT>> mapper,
			ITooltip tooltip) {
		Tag tag = accessor.getServerData().get(key.toString());
		if (tag == null) {
			return null;
		}
		Map.Entry<Identifier, List<ViewGroup<IN>>> entry = accessor.decodeFromNbt(codec, tag).orElse(null);
		if (entry == null) {
			return null;
		}
		IClientExtensionProvider<IN, OUT> provider = mapper.apply(entry.getKey());
		if (provider == null) {
			return null;
		}
		try {
			return provider.getClientGroups(accessor, entry.getValue());
		} catch (Exception e) {
			WailaExceptionHandler.handleErr(e, provider, tooltip::add);
			return null;
		}
	}

	public static float getEnchantPowerBonus(BlockState state, Level world, BlockPos pos) {
		if (WailaClientRegistration.instance().customEnchantPowers.containsKey(state.getBlock())) {
			return WailaClientRegistration.instance().customEnchantPowers.get(state.getBlock()).getEnchantPowerBonus(state, world, pos);
		}
		return state.is(BlockTags.ENCHANTMENT_POWER_PROVIDER) ? 1 : 0;
	}

	// EventBus 7 has no single game bus to register against: every event type carries its own
	// static BUS, and the mod-bus events are reached through the mod's BusGroup.
	public static void init(BusGroup modBusGroup) {
		EntityJoinLevelEvent.BUS.addListener(ClientProxy::onEntityJoin);
		EntityLeaveLevelEvent.BUS.addListener(ClientProxy::onEntityLeave);
		RenderTooltipEvent.GatherComponents.BUS.addListener(Priority.LOWEST, ClientProxy::onTooltip);
		TickEvent.ClientTickEvent.Post.BUS.addListener(ClientProxy::onClientTick);
		ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(ClientProxy::onPlayerJoin);
		ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(ClientProxy::onPlayerLeave);
		RegisterClientCommandsEvent.BUS.addListener(ClientProxy::registerCommands);
		InputEvent.Key.BUS.addListener(ClientProxy::onKeyPressed);
		ScreenEvent.Init.Pre.BUS.addListener(ClientProxy::onGui);
		CustomizeGuiOverlayEvent.BossEventProgress.BUS.addListener(Priority.HIGHEST, ClientProxy::drawBossBarPre);
		CustomizeGuiOverlayEvent.BossEventProgress.BUS.addListener(Priority.LOWEST, ClientProxy::drawBossBarPostInternal);

		// Forge 26.2 dropped RenderGuiEvent in favour of a HUD layer stack, so the overlay is added
		// as a layer instead of drawn from a post-render event.
		AddGuiOverlayLayersEvent.BUS.addListener(event -> event.getLayeredDraw().add(
				Identifier.fromNamespaceAndPath(Jade.ID, "overlay"),
				(guiGraphics, deltaTracker) -> {
					if (Minecraft.getInstance().gui.screen() == null) {
						onRenderTick(guiGraphics, deltaTracker.getRealtimeDeltaTicks());
					}
				}));

		ScreenEvent.Render.Pre.BUS.addListener(event -> {
			Minecraft mc = Minecraft.getInstance();
			Screen screen = event.getScreen();
			if (shouldShowBeforeGui(mc, screen) && !shouldShowAfterGui(mc, screen)) {
				onRenderTick(event.getGuiGraphics(), event.getPartialTick());
			}
		});
		ScreenEvent.Render.Post.BUS.addListener(event -> {
			if (shouldShowAfterGui(Minecraft.getInstance(), event.getScreen())) {
				onRenderTick(event.getGuiGraphics(), event.getPartialTick());
			}
		});

		RegisterClientReloadListenersEvent.BUS.addListener(event -> {
			listeners.forEach($ -> event.registerReloadListener($));
			listeners.clear();
		});
		RegisterKeyMappingsEvent.BUS.addListener(event -> {
			keys.forEach(event::register);
			keys.clear();
		});

		// Forge 26.2 registers the config screen directly rather than through an extension point.
		MinecraftForge.registerConfigScreen((minecraft, screen) -> new HomeConfigScreen(screen));

		//noinspection ConstantValue
		for (int i = InputConstants.KEY_NUMPAD0; i <= InputConstants.KEY_NUMPAD9; i++) {
			InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(i);
			((KeyAccess) (Object) key).setDisplayName(Suppliers.memoize(() -> Component.translatable(key.getName())));
		}
		JadeClient.init();
		JadeClient.recipeLookupPlugins.add(new PolydexCompat());
		if (CommonProxy.isModLoaded("roughlyenoughitems")) {
			JadeClient.addRecipeLookupPlugin("snownee.jade.compat.REICompat");
		}
		if (CommonProxy.isModLoaded("jei")) {
			JadeClient.addRecipeLookupPlugin("snownee.jade.compat.JEICompat");
		}
	}

	public static void sendPacket(CustomPacketPayload payload) {
		// Must go through the channel: it owns the codecs for Jade's payloads. Sending a vanilla
		// custom-payload packet directly fails to encode.
		CommonProxy.CHANNEL.send(payload, Objects.requireNonNull(Minecraft.getInstance().getConnection()).getConnection());
	}

	public static boolean shouldFetchFromServer(@Nullable UUID uuid) {
		String name = lookupPlayerName(uuid);
		return name == null || name.equals(PlayerNameLookup.DUMMY_NAME);
	}

	@Nullable
	public static String lookupPlayerName(@Nullable UUID uuid) {
		if (uuid == null) {
			return null;
		}
		Services services = Minecraft.getInstance().services();
		String name = PlayerNameLookup.get(uuid, services);
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null) {
			Player player = level.getPlayerByUUID(uuid);
			if (name != null && player != null && StringUtil.isValidPlayerName(player.getPlainTextName()) &&
					!player.getPlainTextName().equals(name)) {
				services.nameToIdCache().add(player.nameAndId());
			}
			if (name == null && player != null) {
				return player.getPlainTextName();
			}
		}
		return name;
	}

	public static void runWithContext(Minecraft client, Runnable runnable) {
		client.execute(runnable);
	}
}