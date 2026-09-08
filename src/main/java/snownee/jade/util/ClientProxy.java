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
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Services;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
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
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.ui.FluidStackElement;
import snownee.jade.network.ClientHandshakePacket;
import snownee.jade.network.ClientPayloadContext;
import snownee.jade.network.ReceiveDataPacket;
import snownee.jade.network.ServerHandshakePacket;
import snownee.jade.network.ShowOverlayPacket;
import snownee.jade.overlay.DatapackBlockManager;
import snownee.jade.overlay.OverlayRenderer;

public final class ClientProxy implements ClientModInitializer {

	public static final JadeMetadata metadata = new FabricJadeMetadata();
	private static boolean bossbarShown;
	private static int bossbarHeight;

	public static Optional<String> getModName(String namespace, boolean translate) {
		if (translate) {
			String modMenuKey = "modmenu.nameTranslation.%s".formatted(namespace);
			if (JadeUI.hasTranslation(modMenuKey)) {
				return Optional.of(I18n.get(modMenuKey));
			}
		}
		return FabricLoader.getInstance().getModContainer(namespace)
				.map(ModContainer::getMetadata)
				.map(ModMetadata::getName)
				.filter(Predicate.not(Strings::isNullOrEmpty));
	}

	public static void registerClientCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(JadeClientCommand.create(
				ClientCommands::literal,
				ClientCommands::argument,
				FabricClientCommandSource::sendFeedback,
				FabricClientCommandSource::sendError));
	}

	private static void onEntityJoin(Entity entity, ClientLevel level) {
		try {
			DatapackBlockManager.onEntityJoin(entity);
		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
		}
	}

	private static void onEntityLeave(Entity entity, ClientLevel level) {
		try {
			DatapackBlockManager.onEntityLeave(entity);
		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
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

	private static void onClientTick(Minecraft mc) {
		try {
			JadeClient.tickHandler().tickClient();
		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
		}
	}

	private static void onPlayerLeave(ClientPacketListener handler, Minecraft client) {
		ObjectDataCenter.disconnect();
		WailaClientRegistration.instance().setServerConfig(Map.of());
	}

	private static void onKeyPressed(Minecraft mc) {
		JadeClient.onKeyPressed(1);
	}

	private static void onGui(Screen screen) {
		JadeClient.onGui(screen);
	}

	public static KeyMapping registerKeyBinding(String desc, int defaultKey) {
		KeyMapping key = new KeyMapping("key.jade." + desc, defaultKey, JadeClient.keyMappingCategory);
		KeyMappingHelper.registerKeyMapping(key);
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
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(listener.getUid(), listener);
	}

	public static void drawBossBarPost(LerpingBossEvent bossEvent, int bottom) {
		IWailaConfig.BossBarOverlapMode mode = Jade.config().general().getBossBarOverlapMode();
		if (mode == IWailaConfig.BossBarOverlapMode.PUSH_DOWN) {
			bossbarHeight = bottom;
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
		FluidVariant variant = FluidVariant.of(type, fluid.getComponents());
		FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(type.defaultFluidState());
		TextureAtlasSprite sprite = model.stillMaterial().sprite();
		FluidVariantRenderHandler handler = FluidVariantRendering.getHandlerOrDefault(type);
		int fluidColor = handler.getColor(variant, Minecraft.getInstance().level, BlockPos.ZERO);
		consumer.accept(sprite, fluidColor);
	}

	public static void renderItemDecorationsExtra(
			GuiGraphicsExtractor guiGraphics,
			Font font,
			ItemStack stack,
			int x,
			int y,
			@Nullable String text) {
		// NO-OP
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
		if (!state.is(BlockTags.ENCHANTMENT_POWER_PROVIDER)) {
			return 0;
		}
		return state.getBlock().getProvidedEnchantmentPower(state, world, pos);
	}

	public static void sendPacket(CustomPacketPayload payload) {
		ClientPlayNetworking.send(payload);
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
		PacketContext.runWithContext(Objects.requireNonNull(client.player), runnable);
	}

	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register(mc -> CommonProxy.loadComplete());
		ClientEntityEvents.ENTITY_LOAD.register(ClientProxy::onEntityJoin);
		ClientEntityEvents.ENTITY_UNLOAD.register(ClientProxy::onEntityLeave);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientProxy::onPlayerLeave);
		ClientTickEvents.END_CLIENT_TICK.register(ClientProxy::onClientTick);
		ClientTickEvents.END_CLIENT_TICK.register(ClientProxy::onKeyPressed);
		ScreenEvents.AFTER_INIT.register((Minecraft client, Screen screen, int scaledWidth, int scaledHeight) -> onGui(screen));
		ClientCommandRegistrationCallback.EVENT.register(ClientProxy::registerClientCommand);
		HudElementRegistry.addLast(
				JadeIds.UI_MAIN, (guiGraphics, deltaTracker) -> {
					if (Minecraft.getInstance().gui.screen() == null) {
						onRenderTick(guiGraphics, deltaTracker.getRealtimeDeltaTicks());
					}
				});
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (shouldShowAfterGui(client, screen)) {
				ScreenEvents.afterExtract(screen).register((screen1, guiGraphics, mouseX, mouseY, tickDelta) -> {
					onRenderTick(guiGraphics, tickDelta);
				});
			} else if (shouldShowBeforeGui(client, screen)) {
				ScreenEvents.beforeExtract(screen).register((screen1, guiGraphics, mouseX, mouseY, tickDelta) -> {
					onRenderTick(guiGraphics, tickDelta);
				});
			}
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ClientPlayNetworking.send(new ClientHandshakePacket(Jade.PROTOCOL_VERSION));
		});

		ClientPlayNetworking.registerGlobalReceiver(
				ReceiveDataPacket.TYPE, (payload, context) -> {
					ReceiveDataPacket.handle(payload, ClientPayloadContext.of(context.client()));
				});
		ClientPlayNetworking.registerGlobalReceiver(
				ServerHandshakePacket.TYPE, (payload, context) -> {
					ServerHandshakePacket.handle(payload, ClientPayloadContext.of(context.client()));
				});
		ClientPlayNetworking.registerGlobalReceiver(
				ShowOverlayPacket.TYPE, (payload, context) -> {
					ShowOverlayPacket.handle(payload, ClientPayloadContext.of(context.client()));
				});

		JadeClient.init();
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				HarvestToolProvider.INSTANCE.getUid(),
				HarvestToolProvider.INSTANCE);
		CommonLifecycleEvents.TAGS_LOADED.register((registryAccess, client) -> {
			HarvestToolProvider.INSTANCE.invalidateCache();
		});

		JadeClient.recipeLookupPlugins.add(new PolydexCompat());
		if (CommonProxy.isModLoaded("roughlyenoughitems")) {
			JadeClient.addRecipeLookupPlugin("snownee.jade.compat.REICompat");
		}
		if (CommonProxy.isModLoaded("jei")) {
			JadeClient.addRecipeLookupPlugin("snownee.jade.compat.JEICompat");
		}
		if (CommonProxy.isModLoaded("rrv")) {
			JadeClient.addRecipeLookupPlugin("snownee.jade.compat.RRVCompat");
		}
	}
}
