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
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.ItemDecoratorHandler;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforgespi.language.IModInfo;
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
			if (I18n.exists(modMenuKey)) {
				return Optional.of(I18n.get(modMenuKey));
			}
		}
		return ModList.get().getModContainerById(namespace)
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

	private static void onClientTick(ClientTickEvent.Post event) {
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
		KeyMapping key = new KeyMapping(
				"key.jade." + desc,
				KeyConflictContext.IN_GAME,
				KeyModifier.NONE,
				InputConstants.Type.KEYSYM.getOrCreate(defaultKey),
				JadeClient.keyMappingCategory);
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

	private static void drawBossBarPre(CustomizeGuiOverlayEvent.BossEventProgress event) {
		IWailaConfig.BossBarOverlapMode mode = Jade.config().general().getBossBarOverlapMode();
		if (mode == IWailaConfig.BossBarOverlapMode.HIDE_BOSS_BAR && OverlayRenderer.shown) {
			event.setCanceled(true);
		}
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
		if (mc.level == null || mc.screen instanceof GenericMessageScreen || mc.screen instanceof ProgressScreen) {
			return false;
		}
		IWailaConfig.General config = IWailaConfig.get().general();
		return !config.shouldHideFromGUIs();
	}

	public static void getFluidSpriteAndColor(JadeFluidObject fluid, BiConsumer<@Nullable TextureAtlasSprite, Integer> consumer) {
		Fluid type = fluid.typeHolder().value();
		FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(type.defaultFluidState());
		TextureAtlasSprite sprite = model.stillMaterial().sprite();
		FluidTintSource tintSource = model.fluidTintSource();
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

	public static void init(IEventBus modBus) {
		NeoForge.EVENT_BUS.addListener(ClientProxy::onEntityJoin);
		NeoForge.EVENT_BUS.addListener(ClientProxy::onEntityLeave);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ClientProxy::onTooltip);
		NeoForge.EVENT_BUS.addListener(ClientProxy::onClientTick);
		NeoForge.EVENT_BUS.addListener(ClientProxy::onPlayerJoin);
		NeoForge.EVENT_BUS.addListener(ClientProxy::onPlayerLeave);
		NeoForge.EVENT_BUS.addListener(ClientProxy::registerCommands);
		NeoForge.EVENT_BUS.addListener(ClientProxy::onKeyPressed);
		NeoForge.EVENT_BUS.addListener(ClientProxy::onGui);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ClientProxy::drawBossBarPre);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, ClientProxy::drawBossBarPostInternal);
		NeoForge.EVENT_BUS.addListener(
				RenderGuiEvent.Post.class, event -> {
					if (Minecraft.getInstance().screen == null) {
						onRenderTick(event.getGuiGraphics(), event.getPartialTick().getRealtimeDeltaTicks());
					}
				});
		NeoForge.EVENT_BUS.addListener(
				ScreenEvent.Render.Pre.class, event -> {
					Minecraft mc = Minecraft.getInstance();
					Screen screen = event.getScreen();
					if (shouldShowBeforeGui(mc, screen) && !shouldShowAfterGui(mc, screen)) {
						onRenderTick(event.getGuiGraphics(), event.getPartialTick());
					}
				});
		NeoForge.EVENT_BUS.addListener(
				ScreenEvent.Render.Post.class, event -> {
					if (shouldShowAfterGui(Minecraft.getInstance(), event.getScreen())) {
						onRenderTick(event.getGuiGraphics(), event.getPartialTick());
					}
				});
		modBus.addListener(
				AddClientReloadListenersEvent.class, event -> {
					listeners.forEach($ -> event.addListener($.getUid(), $));
					listeners.clear();
				});
		modBus.addListener(
				RegisterKeyMappingsEvent.class, event -> {
					keys.forEach(event::register);
					keys.clear();
				});
		ModLoadingContext.get().registerExtensionPoint(
				IConfigScreenFactory.class,
				() -> (modContainer, screen) -> new HomeConfigScreen(screen));

		//noinspection ConstantValue
		for (int i = InputConstants.KEY_NUMPAD0; i <= InputConstants.KEY_NUMPAD9; i++) {
			InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(i);
			((KeyAccess) (Object) key).setDisplayName(Suppliers.memoize(() -> Component.translatable(key.getName())));
		}
		JadeClient.init();
		JadeClient.addRecipeLookupPlugin("roughlyenoughitems", "REI", "snownee.jade.compat.REICompat");
		JadeClient.addRecipeLookupPlugin("jei", "JEI", "snownee.jade.compat.JEICompat");
		JadeClient.addRecipeLookupPlugin("rrv", "RRV", "snownee.jade.compat.RRVCompat");
		JadeClient.addRecipeLookupPlugin("", "Polydex", "snownee.jade.compat.PolydexCompat");
	}

	public static void sendPacket(CustomPacketPayload payload) {
		Objects.requireNonNull(Minecraft.getInstance().getConnection()).send(payload);
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
}
