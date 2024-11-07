package snownee.jade.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.command.JadeClientCommand;
import snownee.jade.compat.JEICompat;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.impl.EntityAccessorImpl;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.theme.ThemeHelper;
import snownee.jade.impl.ui.FluidStackElement;
import snownee.jade.mixin.KeyAccess;
import snownee.jade.network.ClientHandshakePacket;
import snownee.jade.network.ReceiveDataPacket;
import snownee.jade.network.RequestBlockPacket;
import snownee.jade.network.RequestEntityPacket;
import snownee.jade.network.ServerHandshakePacket;
import snownee.jade.network.ShowOverlayPacket;
import snownee.jade.overlay.DatapackBlockManager;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.overlay.WailaTickHandler;

public final class ClientProxy implements ClientModInitializer {

	public static boolean hasJEI = CommonProxy.isModLoaded("jei");
	public static boolean hasREI = false; //isModLoaded("roughlyenoughitems");
	public static boolean hasFastScroll = CommonProxy.isModLoaded("fastscroll");
	public static boolean hasAccessibilityMod = CommonProxy.isModLoaded("minecraft_access");

	public static Optional<String> getModName(String namespace) {
		String modMenuKey = "modmenu.nameTranslation.%s".formatted(namespace);
		if (I18n.exists(modMenuKey)) {
			return Optional.of(I18n.get(modMenuKey));
		}
		return FabricLoader.getInstance().getModContainer(namespace)
				.map(ModContainer::getMetadata)
				.map(ModMetadata::getName)
				.filter(Predicate.not(Strings::isNullOrEmpty));
	}

	public static void registerClientCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(JadeClientCommand.create(
				ClientCommandManager::literal,
				ClientCommandManager::argument,
				FabricClientCommandSource::sendFeedback,
				FabricClientCommandSource::sendError));
	}

	private static void onEntityJoin(Entity entity, ClientLevel level) {
		DatapackBlockManager.onEntityJoin(entity);
	}

	private static void onEntityLeave(Entity entity, ClientLevel level) {
		DatapackBlockManager.onEntityLeave(entity);
	}

	private static void onTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipFlag tooltipType, List<Component> lines) {
		JadeClient.appendModName(lines, stack, tooltipContext, tooltipType);
	}

	public static void onRenderTick(GuiGraphics guiGraphics, float tickDelta) {
		try {
			OverlayRenderer.renderOverlay478757(guiGraphics, tickDelta);
		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
		}
	}

	private static void onClientTick(Minecraft mc) {
		try {
			WailaTickHandler.instance().tickClient();
		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
		}
	}

	private static void onPlayerLeave(ClientPacketListener handler, Minecraft client) {
		ObjectDataCenter.serverConnected = false;
		WailaClientRegistration.instance().setServerConfig(Map.of());
	}

	private static void onKeyPressed(Minecraft mc) {
		JadeClient.onKeyPressed(1);
		if (JadeClient.showUses != null) {
			//REICompat.onKeyPressed(1);
			if (hasJEI) {
				JEICompat.onKeyPressed(1);
			}
		}
	}

	private static void onGui(Screen screen) {
		JadeClient.onGui(screen);
	}

	public static KeyMapping registerKeyBinding(String desc, int defaultKey) {
		KeyMapping key = new KeyMapping("key.jade." + desc, InputConstants.Type.KEYSYM, defaultKey, "modmenu.nameTranslation.jade");
		KeyBindingHelper.registerKeyBinding(key);
		return key;
	}

	public static boolean shouldRegisterRecipeViewerKeys() {
		return hasJEI || hasREI;
	}

	public static void requestBlockData(BlockAccessor accessor, List<IServerDataProvider<BlockAccessor>> providers) {
		ClientPlayNetworking.send(new RequestBlockPacket(new BlockAccessorImpl.SyncData(accessor), providers));
	}

	public static void requestEntityData(EntityAccessor accessor, List<IServerDataProvider<EntityAccessor>> providers) {
		ClientPlayNetworking.send(new RequestEntityPacket(new EntityAccessorImpl.SyncData(accessor), providers));
	}

	public static IElement elementFromLiquid(BlockState blockState) {
		FluidState fluidState = blockState.getFluidState();
		return new FluidStackElement(JadeFluidObject.of(fluidState.getType()));//.size(new Size(18, 18));
	}

	public static void registerReloadListener(ResourceManagerReloadListener listener) {
		Minecraft.getInstance().execute(() -> {
			ReloadableResourceManager manager = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
			manager.registerReloadListener(listener);
			listener.onResourceManagerReload(manager);
		});
	}

	@Nullable
	public static Rect2i getBossBarRect() {
		Minecraft mc = Minecraft.getInstance();
		int size = mc.gui.getBossOverlay().events.size();
		if (size == 0) {
			return null;
		}
		int i = mc.getWindow().getGuiScaledWidth();
		int k = i / 2 - 91;
		int height = 10 + mc.font.lineHeight;
		size = Math.min(size, (mc.getWindow().getGuiScaledHeight() / 3 - 12) / height + 1);
		return new Rect2i(k, 12, 182, height * size);
	}

	public static boolean isShowDetailsPressed() {
		return Screen.hasShiftDown() || JadeClient.showDetails.isDown();
	}

	public static boolean shouldShowWithGui(Minecraft mc, @Nullable Screen screen) {
		return screen == null || shouldShowBeforeGui(mc, screen) || shouldShowAfterGui(mc, screen);
	}

	public static boolean shouldShowAfterGui(Minecraft mc, @NotNull Screen screen) {
		return screen instanceof PreviewOptionsScreen || screen instanceof ChatScreen;
	}

	public static boolean shouldShowBeforeGui(Minecraft mc, @NotNull Screen screen) {
		if (mc.level == null) {
			return false;
		}
		IWailaConfig.General config = IWailaConfig.get().general();
		return !config.shouldHideFromGUIs();
	}

	public static void getFluidSpriteAndColor(JadeFluidObject fluid, BiConsumer<@Nullable TextureAtlasSprite, Integer> consumer) {
		Fluid type = fluid.getType();
		FluidVariant variant = FluidVariant.of(type, fluid.getComponents());
		FluidVariantRenderHandler handler = FluidVariantRendering.getHandlerOrDefault(type);
		TextureAtlasSprite[] sprites = handler.getSprites(variant);
		TextureAtlasSprite fluidStillSprite = sprites == null ? null : sprites[0];
		int fluidColor = handler.getColor(variant, Minecraft.getInstance().level, null);
		consumer.accept(fluidStillSprite, fluidColor);
	}

	public static KeyMapping registerDetailsKeyBinding() {
		return registerKeyBinding("show_details_alternative", InputConstants.UNKNOWN.getValue());
	}

	public static void renderItemDecorationsExtra(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y, String text) {
		// NO-OP
	}

	public static InputConstants.Key getBoundKeyOf(KeyMapping keyMapping) {
		return KeyBindingHelper.getBoundKeyOf(keyMapping);
	}

	public static GameType getGameMode() {
		MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
		return gameMode == null ? GameType.SURVIVAL : gameMode.getPlayerMode();
	}

	public static boolean hasAccessibilityMod() {
		return hasAccessibilityMod;
	}

	@Nullable
	public static <IN, OUT> List<ClientViewGroup<OUT>> mapToClientGroups(
			Accessor<?> accessor,
			ResourceLocation key,
			StreamCodec<RegistryFriendlyByteBuf, Map.Entry<ResourceLocation, List<ViewGroup<IN>>>> codec,
			Function<ResourceLocation, IClientExtensionProvider<IN, OUT>> mapper,
			ITooltip tooltip) {
		Tag tag = accessor.getServerData().get(key.toString());
		if (tag == null) {
			return null;
		}
		Map.Entry<ResourceLocation, List<ViewGroup<IN>>> entry = accessor.decodeFromNbt(codec, tag).orElse(null);
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
		return state.is(Blocks.BOOKSHELF) ? 1 : 0;
	}

	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register(mc -> CommonProxy.loadComplete());
		ClientEntityEvents.ENTITY_LOAD.register(ClientProxy::onEntityJoin);
		ClientEntityEvents.ENTITY_UNLOAD.register(ClientProxy::onEntityLeave);
		ResourceLocation lowest = JadeIds.CORE_MOD_NAME;
		ItemTooltipCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, lowest);
		ItemTooltipCallback.EVENT.register(lowest, ClientProxy::onTooltip);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientProxy::onPlayerLeave);
		ClientTickEvents.END_CLIENT_TICK.register(ClientProxy::onClientTick);
		ClientTickEvents.END_CLIENT_TICK.register(ClientProxy::onKeyPressed);
		ScreenEvents.AFTER_INIT.register((Minecraft client, Screen screen, int scaledWidth, int scaledHeight) -> onGui(screen));
		ClientCommandRegistrationCallback.EVENT.register(ClientProxy::registerClientCommand);
		HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
			if (Minecraft.getInstance().screen == null) {
				onRenderTick(guiGraphics, deltaTracker.getRealtimeDeltaTicks());
			}
		});
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (shouldShowAfterGui(client, screen)) {
				ScreenEvents.afterRender(screen).register((screen1, guiGraphics, mouseX, mouseY, tickDelta) -> {
					onRenderTick(guiGraphics, tickDelta);
				});
			} else if (shouldShowBeforeGui(client, screen)) {
				ScreenEvents.beforeRender(screen).register((screen1, guiGraphics, mouseX, mouseY, tickDelta) -> {
					onRenderTick(guiGraphics, tickDelta);
				});
			}
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ClientPlayNetworking.send(new ClientHandshakePacket(Jade.PROTOCOL_VERSION));
		});

		ClientPlayNetworking.registerGlobalReceiver(ReceiveDataPacket.TYPE, (payload, context) -> {
			ReceiveDataPacket.handle(payload, context.client()::execute);
		});
		ClientPlayNetworking.registerGlobalReceiver(ServerHandshakePacket.TYPE, (payload, context) -> {
			ServerHandshakePacket.handle(payload, context.client()::execute);
		});
		ClientPlayNetworking.registerGlobalReceiver(ShowOverlayPacket.TYPE, (payload, context) -> {
			ShowOverlayPacket.handle(payload, context.client()::execute);
		});

		for (int i = 320; i < 330; i++) {
			InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(i);
			//noinspection deprecation
			((KeyAccess) (Object) key).setDisplayName(new LazyLoadedValue<>(() -> Component.translatable(key.getName())));
		}
		JadeClient.init();
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
				.registerReloadListener((IdentifiableResourceReloadListener) ThemeHelper.INSTANCE);
		ResourceManagerHelper.get(PackType.SERVER_DATA)
				.registerReloadListener((IdentifiableResourceReloadListener) HarvestToolProvider.INSTANCE);
		UsernameCache.load();
	}
}
