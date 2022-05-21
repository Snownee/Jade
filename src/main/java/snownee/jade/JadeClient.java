package snownee.jade;

import java.util.Map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.addon.vanilla.VanillaPlugin;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.config.IWailaConfig.TTSMode;
import snownee.jade.api.event.WailaRayTraceEvent;
import snownee.jade.api.event.WailaRenderEvent;
import snownee.jade.command.DumpHandlersCommand;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.overlay.WailaTickHandler;
import snownee.jade.util.ModIdentification;

@EventBusSubscriber(Dist.CLIENT)
public final class JadeClient {
	private static float savedProgress;
	private static float progressAlpha;
	private static boolean canHarvest;

	@SubscribeEvent
	public static void post(WailaRenderEvent.Post event) {
		if (!PluginConfig.INSTANCE.get(Identifiers.MC_BREAKING_PROGRESS)) {
			progressAlpha = 0;
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		MultiPlayerGameMode playerController = mc.gameMode;
		if (playerController == null || playerController.destroyBlockPos == null) {
			return;
		}
		BlockState state = mc.level.getBlockState(playerController.destroyBlockPos);
		if (playerController.isDestroying())
			canHarvest = ForgeHooks.isCorrectToolForDrops(state, mc.player);
		int color = canHarvest ? 0xFFFFFF : 0xFF4444;
		Rect2i rect = event.getRect();
		int height = rect.getHeight();
		int width = rect.getWidth();
		if (!VanillaPlugin.CLIENT_REGISTRATION.getConfig().getOverlay().getSquare()) {
			height -= 1;
			width -= 2;
		}
		progressAlpha += mc.getDeltaFrameTime() * (playerController.isDestroying() ? 0.1F : -0.1F);
		if (playerController.isDestroying()) {
			progressAlpha = Math.min(progressAlpha, 0.53F); //0x88 = 0.53 * 255
			float progress = state.getDestroyProgress(mc.player, mc.player.level, playerController.destroyBlockPos);
			if (playerController.destroyProgress + progress >= 1) {
				progressAlpha = 1;
			}
			progress = playerController.destroyProgress + mc.getFrameTime() * progress;
			progress = Mth.clamp(progress, 0, 1);
			savedProgress = progress;
		} else {
			progressAlpha = Math.max(progressAlpha, 0);
		}
		color = IConfigOverlay.applyAlpha(color, progressAlpha);
		DisplayHelper.fill(event.getPoseStack(), 0, height - 1, width * savedProgress, height, color);
	}

	private static final Cache<BlockState, BlockState> CHEST_CACHE = CacheBuilder.newBuilder().build();

	private static BlockState getCorrespondingNormalChest(BlockState state) {
		try {
			return CHEST_CACHE.get(state, () -> {
				ResourceLocation trappedName = state.getBlock().getRegistryName();
				if (trappedName.getPath().startsWith("trapped_")) {
					ResourceLocation chestName = new ResourceLocation(trappedName.getNamespace(), trappedName.getPath().substring(8));
					Block block = ForgeRegistries.BLOCKS.getValue(chestName);
					if (block != null) {
						return copyProperties(state, block.defaultBlockState());
					}
				}
				return state;
			});
		} catch (Exception e) {
			return state;
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> BlockState copyProperties(BlockState oldState, BlockState newState) {
		for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
			Property<T> property = (Property<T>) entry.getKey();
			if (newState.hasProperty(property))
				newState = newState.setValue(property, property.getValueClass().cast(entry.getValue()));
		}
		return newState;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void override(WailaRayTraceEvent event) {
		Player player = event.getAccessor().getPlayer();
		if (player.isCreative() || player.isSpectator())
			return;
		if (event.getAccessor() instanceof BlockAccessor) {
			BlockAccessor target = (BlockAccessor) event.getAccessor();
			if (target.getBlock() instanceof TrappedChestBlock) {
				BlockState state = getCorrespondingNormalChest(target.getBlockState());
				if (state != target.getBlockState()) {
					event.setAccessor(VanillaPlugin.CLIENT_REGISTRATION.createBlockAccessor(state, target.getBlockEntity(), target.getLevel(), player, target.getServerData(), target.getHitResult(), target.isServerConnected()));
				}
			} else if (target.getBlock() instanceof InfestedBlock) {
				Block block = ((InfestedBlock) target.getBlock()).getHostBlock();
				event.setAccessor(VanillaPlugin.CLIENT_REGISTRATION.createBlockAccessor(block.defaultBlockState(), target.getBlockEntity(), target.getLevel(), player, target.getServerData(), target.getHitResult(), target.isServerConnected()));
			} else if (target.getBlock() == Blocks.POWDER_SNOW) {
				Block block = Blocks.SNOW_BLOCK;
				event.setAccessor(VanillaPlugin.CLIENT_REGISTRATION.createBlockAccessor(block.defaultBlockState(), null, target.getLevel(), player, target.getServerData(), target.getHitResult(), target.isServerConnected()));
			}
		}
	}

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
