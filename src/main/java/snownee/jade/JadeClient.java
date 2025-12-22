package snownee.jade;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ibm.icu.text.MessageFormat;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.addon.vanilla.VanillaPlugin;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.JadeIds;
import snownee.jade.api.JadeKeys;
import snownee.jade.api.TraceableException;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.Overlay;
import snownee.jade.api.config.IWailaConfig.TTSMode;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.ColorPalette;
import snownee.jade.api.ui.Rect2f;
import snownee.jade.api.ui.ScreenDirection;
import snownee.jade.api.ui.TooltipAnimation;
import snownee.jade.compat.RecipeLookupPlugin;
import snownee.jade.compat.RecipeLookupResult;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.theme.ThemeHelper;
import snownee.jade.key_extension.KeyMappingEx;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.WailaTickHandler;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.ModIdentification;
import snownee.jade.util.WailaExceptionHandler;

public final class JadeClient {

	public static final SystemToast.SystemToastId JADE_PLEASE_WAIT = new SystemToast.SystemToastId(2000L);
	public static final KeyMapping[] profiles = new KeyMapping[4];
	public static final KeyMapping.Category keyMappingCategory = KeyMapping.Category.register(JadeIds.JADE("main"));
	public static final List<RecipeLookupPlugin> recipeLookupPlugins = Lists.newArrayList();
	private static final WailaTickHandler tickHandler = new WailaTickHandler();
	public static @Nullable KeyMapping openConfig;
	public static @Nullable KeyMapping showOverlay;
	public static @Nullable KeyMapping toggleLiquid;
	public static @Nullable KeyMapping showDetails;
	public static @Nullable KeyMapping narrate;
	public static @Nullable KeyMapping showRecipes;
	public static @Nullable KeyMapping showUses;
	public static float renderDistanceStart;
	public static float renderDistanceEnd;
	private static boolean translationChecked;
	private static float savedProgress;
	private static float progressAlpha;
	private static boolean canHarvest;

	public static void init() {
		openConfig = ClientProxy.registerKeyBinding("config", InputConstants.KEY_NUMPAD0);
		showOverlay = ClientProxy.registerKeyBinding("show_overlay", InputConstants.KEY_NUMPAD1);
		toggleLiquid = ClientProxy.registerKeyBinding("toggle_liquid", InputConstants.KEY_NUMPAD2);
		if (JadeKeys.hasRecipeViewerKeys()) {
			showRecipes = ClientProxy.registerKeyBinding("show_recipes", InputConstants.KEY_NUMPAD3);
			showUses = ClientProxy.registerKeyBinding("show_uses", InputConstants.KEY_NUMPAD4);
		}
		narrate = ClientProxy.registerKeyBinding("narrate", InputConstants.KEY_NUMPAD5);
		showDetails = ClientProxy.registerKeyBinding("show_details", InputConstants.KEY_LSHIFT);
		for (int i = 0; i < 4; i++) {
			profiles[i] = ClientProxy.registerKeyBinding("profile." + i, InputConstants.UNKNOWN.getValue());
		}

		ClientProxy.registerReloadListener(ModIdentification.INSTANCE);
		ClientProxy.registerReloadListener(HarvestToolProvider.INSTANCE);
		ClientProxy.registerReloadListener(ThemeHelper.INSTANCE);
	}

	public static WailaTickHandler tickHandler() {
		return tickHandler;
	}

	public static void onKeyPressed(int action) {
		Minecraft mc = Minecraft.getInstance();
		while (JadeKeys.openConfig().consumeClick()) {
			Jade.invalidateConfig();
			ItemStorageProvider.targetCache.invalidateAll();
			ItemStorageProvider.containerCache.invalidateAll();
			mc.setScreen(new HomeConfigScreen(null));
		}

		while (JadeKeys.showOverlay().consumeClick()) {
			IWailaConfig.General general = IWailaConfig.get().general();
			DisplayMode mode = general.getDisplayMode();
			if (mode == DisplayMode.TOGGLE) {
				general.setDisplayTooltip(!general.shouldDisplayTooltip());
				if (!general.shouldDisplayTooltip() && Jade.history().hintOverlayToggle) {
					mc.getChatListener().handleSystemMessage(Component.translatable("toast.jade.toggle_hint.1"), false);
					mc.getChatListener().handleSystemMessage(
							Component.translatable(
									"toast.jade.toggle_hint.2",
									JadeKeys.showOverlay().getTranslatedKeyMessage()), false);
					Jade.history().hintOverlayToggle = false;
				}
				narrateKey("show_overlay", general.shouldDisplayTooltip());
				IWailaConfig.get().save();
			}
		}

		while (JadeKeys.toggleLiquid().consumeClick()) {
			IWailaConfig.General general = IWailaConfig.get().general();
			general.setDisplayFluids(!general.shouldDisplayFluids());
			narrateKey("toggle_liquid", general.shouldDisplayFluids());
			IWailaConfig.get().save();
		}

		while (JadeKeys.narrate().consumeClick()) {
			IWailaConfig.Accessibility accessibility = IWailaConfig.get().accessibility();
			if (accessibility.getTTSMode() == TTSMode.TOGGLE) {
				accessibility.toggleTTS();
				if (accessibility.shouldEnableTextToSpeech() && Jade.history().hintNarratorToggle) {
					mc.getChatListener().handleSystemMessage(Component.translatable("toast.jade.tts_hint.1"), false);
					mc.getChatListener().handleSystemMessage(
							Component.translatable(
									"toast.jade.tts_hint.2",
									JadeKeys.narrate().getTranslatedKeyMessage()), false);
					Jade.history().hintNarratorToggle = false;
				}
				IWailaConfig.get().save();
			} else if (tickHandler.rootElement != null) {
				tickHandler.narrate(tickHandler.rootElement, false);
			}
		}

		if (Jade.rootConfig().isEnableProfiles()) {
			for (int i = 0; i < 4; i++) {
				while (profiles[i].consumeClick()) {
					Jade.useProfile(i);
					if (IWailaConfig.get().accessibility().getNarrateKeys()) {
						tickHandler.narrate(I18n.get("narration.jade.key.profile", profiles[i].getName()), false);
					}
				}
			}
		}

		if (JadeKeys.hasRecipeViewerKeys()) {
			while (JadeKeys.showUses().consumeClick()) {
				lookupRecipes(true);
			}

			while (JadeKeys.showRecipes().consumeClick()) {
				lookupRecipes(false);
			}
		}
	}

	public static void lookupRecipes(boolean uses) {
		if (recipeLookupPlugins.isEmpty() || tickHandler.state == null) {
			return;
		}
		Accessor<?> accessor = tickHandler.state.accessor();
		ItemStack itemStack = accessor.getPickedResult();
		if (itemStack.isEmpty()) {
			return;
		}
		Identifier specialId = ModIdentification.getSpecialId(itemStack).orElse(null);
		RecipeLookupResult selected = null;
		List<RecipeLookupResult> results = Lists.newArrayList();
		for (RecipeLookupPlugin plugin : recipeLookupPlugins) {
			try {
				RecipeLookupResult result = plugin.lookup(itemStack, specialId, uses);
				if (result.isFail()) {
					continue;
				}
				results.add(result);
				if (selected == null || result.score() > selected.score()) {
					selected = result;
				}
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, null, null);
			}
		}
		if (selected != null) {
			selected.action().accept(Minecraft.getInstance().screen, results);
		}
	}

	public static void narrateKey(String key, boolean bl) {
		if (IWailaConfig.get().accessibility().getNarrateKeys()) {
			key = "narration.jade.key.%s.%s".formatted(key, bl ? "on" : "off");
			tickHandler.narrate(I18n.get(key), false);
		}
	}

	public static void onGui(Screen screen) {
		if (!translationChecked && screen instanceof TitleScreen && CommonProxy.isDevEnv()) {
			translationChecked = true;
			List<String> keys = Lists.newArrayList();
			for (Identifier id : WailaClientRegistration.instance().getConfigKeys()) {
				String key = "config.jade.plugin_%s.%s".formatted(id.getNamespace(), id.getPath());
				if (!I18n.exists(key)) {
					keys.add(key);
				}
			}
			if (!keys.isEmpty()) {
				throw new AssertionError("Missing config translation: %s".formatted(String.join(",", keys)));
			}
		}
	}

	@Nullable
	public static Component appendModName(ItemStack itemStack) {
		if (!IWailaConfig.get().general().showItemModNameTooltip()) {
			return null;
		}
		if (itemStack.isEmpty()) {
			return null;
		}
		if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen screen && screen.hoveredSlot != null) {
			if (screen.hoveredSlot.container instanceof Inventory ||
					CreativeModeInventoryScreen.selectedTab.getType() != CreativeModeTab.Type.CATEGORY) {
				return null;
			}
		}
		String name;
		try {
			name = ModIdentification.getModName(itemStack);
		} catch (Throwable e) {
			MutableObject<Component> holder = new MutableObject<>();
			WailaExceptionHandler.handleErr(
					TraceableException.create(e, BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace()),
					null,
					holder::setValue);
			return holder.get();
		}
		return Component.literal(name).withStyle(IWailaConfig.get().formatting().getItemModNameStyle());
	}

	public static Accessor<?> builtInOverrides(HitResult hitResult, Accessor<?> accessor, Accessor<?> originalAccessor) {
		if (WailaClientRegistration.instance().maybeLowVisionUser() || !IWailaConfig.get().general().getBuiltinCamouflage()) {
			return accessor;
		}
		if (accessor instanceof BlockAccessor target) {
			Player player = accessor.getPlayer();
			if (player.isCreative() || player.isSpectator()) {
				return accessor;
			}
			IWailaClientRegistration client = WailaClientRegistration.instance();
			if (target.getBlock() instanceof ChestBlock) {
				BlockState state = VanillaPlugin.getCorrespondingNormalChest(target.getBlockState());
				if (state != target.getBlockState()) {
					return client.blockAccessor().from(target).blockState(state).build();
				}
			}
			BlockAccessor.Builder builder = client.blockAccessor().from(target).blockEntity(() -> null);
			if (target.getBlock() instanceof InfestedBlock) {
				Block block = ((InfestedBlock) target.getBlock()).getHostBlock();
				return builder.blockState(block.defaultBlockState()).build();
			} else if (target.getBlock() == Blocks.POWDER_SNOW) {
				Block block = Blocks.SNOW_BLOCK;
				return builder.blockState(block.defaultBlockState()).build();
			} else if (target.getBlock() instanceof BrushableBlock brushable) {
				Block block = brushable.getTurnsInto();
				BlockState blockState = block.defaultBlockState();
				if (!blockState.isAir() && isCamouflageBrushableBlock(blockState)) {
					return builder.blockState(blockState).build();
				}
			}
		}
		return accessor;
	}

	private static boolean isCamouflageBrushableBlock(BlockState blockState) {
		return blockState.typeHolder().unwrapKey().orElseThrow().identifier().getPath().startsWith("suspicious_");
	}

	@Nullable
	public static Accessor<?> limitMobEffectFog(HitResult hitResult, Accessor<?> accessor, Accessor<?> originalAccessor) {
		if (WailaClientRegistration.instance().maybeLowVisionUser()) {
			return accessor;
		}
		Player player = accessor.getPlayer();
		Minecraft mc = Minecraft.getInstance();
		if (mc.gameRenderer.lightmapRenderState.darknessEffectScale > 0.15f &&
				accessor.getLevel().getMaxLocalRawBrightness(BlockPos.containing(accessor.getHitResult().getLocation())) < 7) {
			return null;
		}
		if (renderDistanceStart == 0f && renderDistanceEnd == 0f) {
			return accessor;
		}
		float dist = (renderDistanceStart + renderDistanceEnd) * 0.5f;
		if (accessor.getHitResult().distanceTo(player) > dist * dist) {
			return null;
		}
		return accessor;
	}

	public static void drawBreakingProgress(BoxElement root, TooltipAnimation animation, GuiGraphics graphics, Accessor<?> accessor) {
		if (!IWailaConfig.get().plugin().get(JadeIds.MC_BREAKING_PROGRESS)) {
			progressAlpha = 0;
			return;
		}
		if (!Float.isNaN(root.getBoxProgress())) {
			progressAlpha = 0;
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		MultiPlayerGameMode playerController = mc.gameMode;
		if (playerController == null || mc.level == null || mc.player == null) {
			return;
		}
		BlockPos pos = playerController.destroyBlockPos;
		BlockState state = mc.level.getBlockState(pos);
		if (playerController.isDestroying()) {
			canHarvest = CommonProxy.isCorrectToolForDrops(state, mc.player, mc.level, pos);
		} else if (progressAlpha == 0) {
			return;
		}
		Theme theme = IThemeHelper.get().theme();
		ColorPalette colors = theme.tooltipStyle.boxProgressColors;
		int color = canHarvest ? colors.title() : colors.failure();
		float top = root.getY() + root.getHeight();
		float width = root.getWidth();
		progressAlpha += mc.getDeltaTracker().getGameTimeDeltaTicks() * (playerController.isDestroying() ? 0.1F : -0.1F);
		if (playerController.isDestroying()) {
			progressAlpha = Math.min(progressAlpha, 0.6F);
			float progress = state.getDestroyProgress(mc.player, mc.player.level(), pos);
			if (playerController.destroyProgress + progress >= 1) {
				progressAlpha = savedProgress = 1;
			} else {
				progress = playerController.destroyProgress + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false) * progress;
				savedProgress = Mth.clamp(progress, 0, 1);
			}
		} else {
			progressAlpha = Math.max(progressAlpha, 0);
		}
		if (progressAlpha == 0) {
			return;
		}
		color = Overlay.applyAlpha(color, progressAlpha);
		float offset0 = theme.tooltipStyle.boxProgressOffset(ScreenDirection.UP);
		float offset1 = theme.tooltipStyle.boxProgressOffset(ScreenDirection.RIGHT);
		float offset2 = theme.tooltipStyle.boxProgressOffset(ScreenDirection.DOWN);
		float offset3 = theme.tooltipStyle.boxProgressOffset(ScreenDirection.LEFT);
		width += offset1 - offset3;
		DisplayHelper.fill(graphics, offset3, top - 1 + offset0, offset3 + width * savedProgress, top + offset2, color);
	}

	public static MutableComponent format(String s, Object... objects) {
		return Component.literal(formatString(s, objects));
	}

	public static String formatString(String s, Object... objects) {
		try {
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] instanceof Component component) {
					objects[i] = component.getString();
				}
			}
			return MessageFormat.format(I18n.get(s), objects);
		} catch (Exception e) {
			return I18n.get(s, objects);
		}
	}

	public static void pleaseWait() {
		SystemToast.add(
				Minecraft.getInstance().getToastManager(),
				JADE_PLEASE_WAIT,
				Component.translatable("toast.jade.please_wait.1"),
				Component.translatable("toast.jade.please_wait.2"));
	}

	public static Runnable recoverKeysAction(Predicate<KeyMapping> predicate) {
		ImmutableMap.Builder<KeyMapping, InputConstants.Key> keyMapBuilder = ImmutableMap.builder();
		for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
			if (predicate.test(keyMapping)) {
				keyMapBuilder.put(keyMapping, ((KeyMappingEx) keyMapping).keyEx$key());
			}
		}
		var keyMap = keyMapBuilder.build();
		return () -> keyMap.forEach(KeyMapping::setKey);
	}

	public static void refreshKeyState() {
		boolean active = Jade.rootConfig().isEnableProfiles();
		for (KeyMapping keyMapping : profiles) {
			KeyMappingEx.setActive(keyMapping, active);
		}
	}

	public static void addRecipeLookupPlugin(String clazz) {
		try {
			recipeLookupPlugins.add((RecipeLookupPlugin) Class.forName(clazz).getDeclaredConstructor().newInstance());
		} catch (Throwable e) {
			if (CommonProxy.isDevEnv()) {
				Jade.LOGGER.warn("Failed to load recipe lookup plugin: {}", clazz, e);
			}
		}
	}

	public static Rect2f hotbarArea(GuiGraphics graphics) {
		int screenCenter = graphics.guiWidth() / 2;
//		int hotbarWidth = 182;
		int halfHotbar = 91 + 50;
		return new Rect2f(
				screenCenter - halfHotbar,
				graphics.guiHeight() - 100,
				screenCenter + halfHotbar,
				graphics.guiHeight() + 100);
	}
}
