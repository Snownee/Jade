package snownee.jade;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.addon.vanilla.VanillaPlugin;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.config.IWailaConfig.TTSMode;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.ColorPalette;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.ScreenDirection;
import snownee.jade.api.ui.TooltipRect;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.WailaTickHandler;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.ModIdentification;

public final class JadeClient {

	public static final SystemToast.SystemToastId JADE_TUTORIAL = new SystemToast.SystemToastId(6000L);
	public static final SystemToast.SystemToastId JADE_PLEASE_WAIT = new SystemToast.SystemToastId(2000L);
	public static KeyMapping openConfig;
	public static KeyMapping showOverlay;
	public static KeyMapping toggleLiquid;
	public static KeyMapping showDetails;
	public static KeyMapping narrate;
	public static KeyMapping showRecipes;
	public static KeyMapping showUses;
	private static final Cache<Item.TooltipContext, Item.TooltipContext> hideModName = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.SECONDS)
			.build();
	private static boolean translationChecked;
	private static float savedProgress;
	private static float progressAlpha;
	private static boolean canHarvest;

	public static void init() {
		openConfig = ClientProxy.registerKeyBinding("config", InputConstants.KEY_NUMPAD0);
		showOverlay = ClientProxy.registerKeyBinding("show_overlay", InputConstants.KEY_NUMPAD1);
		toggleLiquid = ClientProxy.registerKeyBinding("toggle_liquid", InputConstants.KEY_NUMPAD2);
		if (ClientProxy.shouldRegisterRecipeViewerKeys()) {
			showRecipes = ClientProxy.registerKeyBinding("show_recipes", InputConstants.KEY_NUMPAD3);
			showUses = ClientProxy.registerKeyBinding("show_uses", InputConstants.KEY_NUMPAD4);
		}
		narrate = ClientProxy.registerKeyBinding("narrate", InputConstants.KEY_NUMPAD5);
		showDetails = ClientProxy.registerDetailsKeyBinding();

		ClientProxy.registerReloadListener(ModIdentification.INSTANCE);
	}

	public static void onKeyPressed(int action) {
		while (openConfig.consumeClick()) {
			Jade.CONFIG.invalidate();
			ItemStorageProvider.targetCache.invalidateAll();
			ItemStorageProvider.containerCache.invalidateAll();
			Minecraft.getInstance().setScreen(new HomeConfigScreen(null));
		}

		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		while (showOverlay.consumeClick()) {
			DisplayMode mode = general.getDisplayMode();
			if (mode == IWailaConfig.DisplayMode.TOGGLE) {
				general.setDisplayTooltip(!general.shouldDisplayTooltip());
				WailaConfig.ConfigHistory history = Jade.CONFIG.get().getHistory();
				if (!general.shouldDisplayTooltip() && history.hintOverlayToggle) {
					SystemToast.add(
							Minecraft.getInstance().getToasts(),
							JADE_TUTORIAL,
							Component.translatable("toast.jade.toggle_hint.1"),
							Component.translatable("toast.jade.toggle_hint.2", showOverlay.getTranslatedKeyMessage()));
					history.hintOverlayToggle = false;
				}
				Jade.CONFIG.save();
			}
		}

		while (toggleLiquid.consumeClick()) {
			general.setDisplayFluids(!general.shouldDisplayFluids());
			Jade.CONFIG.save();
		}

		while (narrate.consumeClick()) {
			if (general.getTTSMode() == TTSMode.TOGGLE) {
				general.toggleTTS();
				WailaConfig.ConfigHistory history = Jade.CONFIG.get().getHistory();
				if (general.shouldEnableTextToSpeech() && history.hintNarratorToggle) {
					SystemToast.add(
							Minecraft.getInstance().getToasts(),
							JADE_TUTORIAL,
							Component.translatable("toast.jade.tts_hint.1"),
							Component.translatable("toast.jade.tts_hint.2", narrate.getTranslatedKeyMessage()));
					history.hintNarratorToggle = false;
				}
				Jade.CONFIG.save();
			} else if (WailaTickHandler.instance().rootElement != null) {
				WailaTickHandler.narrate(WailaTickHandler.instance().rootElement.getTooltip(), false);
			}
		}
	}

	public static void onGui(Screen screen) {
		if (!translationChecked && screen instanceof TitleScreen && CommonProxy.isDevEnv()) {
			translationChecked = true;
			List<String> keys = Lists.newArrayList();
			for (ResourceLocation id : PluginConfig.INSTANCE.getKeys()) {
				String key = "config.jade.plugin_%s.%s".formatted(id.getNamespace(), id.getPath());
				if (!I18n.exists(key)) {
					keys.add(key);
				}
			}
			if (!keys.isEmpty()) {
				throw new AssertionError("Missing config translation: %s".formatted(Joiner.on(',').join(keys)));
			}
		}
	}

	public static void hideModNameIn(Item.TooltipContext context) {
		hideModName.put(context, context);
	}

	public static void appendModName(List<Component> tooltip, ItemStack stack, Item.TooltipContext tooltipContext, TooltipFlag flag) {
		if (hideModName.getIfPresent(tooltipContext) != null || !Jade.CONFIG.get().getGeneral().showItemModNameTooltip()) {
			return;
		}
		if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen screen && screen.hoveredSlot != null &&
				screen.hoveredSlot.getItem() == stack) {
			if (CreativeModeInventoryScreen.selectedTab.getType() != CreativeModeTab.Type.CATEGORY || !flag.isCreative()) {
				return;
			}
		}
		int i = 1;
		String name = ModIdentification.getModName(stack);
		for (; i < tooltip.size(); i++) {
			if (Objects.equals(tooltip.get(i).getString(), name)) {
				break;
			}
		}
		tooltip.add(i, Component.literal(name).withStyle(Jade.CONFIG.get().getFormatting().getItemModNameStyle()));
	}

	@Nullable
	public static Accessor<?> builtInOverrides(
			HitResult hitResult, @Nullable Accessor<?> accessor, @Nullable Accessor<?> originalAccessor) {
		if (WailaClientRegistration.instance().maybeLowVisionUser() || !IWailaConfig.get().getGeneral().getBuiltinCamouflage()) {
			return accessor;
		}
		if (accessor instanceof BlockAccessor target) {
			Player player = accessor.getPlayer();
			if (player.isCreative() || player.isSpectator()) {
				return accessor;
			}
			IWailaClientRegistration client = VanillaPlugin.CLIENT_REGISTRATION;
			if (target.getBlock() instanceof TrappedChestBlock) {
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
				return builder.blockState(block.defaultBlockState()).build();
			}
		}
		return accessor;
	}

	@Nullable
	public static Accessor<?> limitMobEffectFog(
			HitResult hitResult, @Nullable Accessor<?> accessor, @Nullable Accessor<?> originalAccessor) {
		if (accessor == null) {
			return null;
		}
		Player player = accessor.getPlayer();
		Minecraft mc = Minecraft.getInstance();
		LightTexture lightTexture = mc.gameRenderer.lightTexture();
		float darknessEffectScale = mc.options.darknessEffectScale().get().floatValue();
		float gamma = lightTexture.getDarknessGamma(1) * darknessEffectScale;
		gamma = lightTexture.calculateDarknessScale(player, gamma, 1);
		if (gamma > 0.15f && accessor.getLevel().getMaxLocalRawBrightness(BlockPos.containing(accessor.getHitResult().getLocation())) < 7) {
			return null;
		}
		FogRenderer.MobEffectFogFunction fogFunction = FogRenderer.getPriorityFogFunction(player, 1);
		if (fogFunction == null) {
			return accessor;
		}
		FogRenderer.FogData fogData = new FogRenderer.FogData(FogRenderer.FogMode.FOG_TERRAIN);
		fogFunction.setupFog(
				fogData,
				player,
				player.getEffect(fogFunction.getMobEffect()),
				Math.max(32, mc.gameRenderer.getRenderDistance()),
				1);
		float dist = (fogData.start + fogData.end) * 0.5F;
		if (accessor.getHitResult().distanceTo(player) > dist * dist) {
			return null;
		}
		return accessor;
	}

	public static void drawBreakingProgress(IBoxElement rootElement, TooltipRect rect, GuiGraphics guiGraphics, Accessor<?> accessor) {
		if (!PluginConfig.INSTANCE.get(JadeIds.MC_BREAKING_PROGRESS)) {
			progressAlpha = 0;
			return;
		}
		if (!Float.isNaN(rootElement.getBoxProgress())) {
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
		int color = canHarvest ? colors.normal() : colors.failure();
		float top = rootElement.getCachedSize().y;
		float width = rootElement.getCachedSize().x;
		boolean roundCorner = !IWailaConfig.get().getOverlay().getSquare();
		if (roundCorner && theme.tooltipStyle instanceof BoxStyle.GradientBorder) {
			top += 1;
		}
		progressAlpha += mc.getTimer().getGameTimeDeltaTicks() * (playerController.isDestroying() ? 0.1F : -0.1F);
		if (playerController.isDestroying()) {
			progressAlpha = Math.min(progressAlpha, 0.6F);
			float progress = state.getDestroyProgress(mc.player, mc.player.level(), pos);
			if (playerController.destroyProgress + progress >= 1) {
				progressAlpha = savedProgress = 1;
			} else {
				progress = playerController.destroyProgress + mc.getTimer().getGameTimeDeltaPartialTick(false) * progress;
				savedProgress = Mth.clamp(progress, 0, 1);
			}
		} else {
			progressAlpha = Math.max(progressAlpha, 0);
		}
		if (progressAlpha == 0) {
			return;
		}
		color = IConfigOverlay.applyAlpha(color, progressAlpha);
		float offset0 = theme.tooltipStyle.boxProgressOffset(ScreenDirection.UP);
		float offset1 = theme.tooltipStyle.boxProgressOffset(ScreenDirection.RIGHT);
		float offset2 = theme.tooltipStyle.boxProgressOffset(ScreenDirection.DOWN);
		float offset3 = theme.tooltipStyle.boxProgressOffset(ScreenDirection.LEFT);
		width += offset1 - offset3;
		DisplayHelper.fill(guiGraphics, offset3, top - 1 + offset0, offset3 + width * savedProgress, top + offset2, color);
	}

	public static MutableComponent format(String s, Object... objects) {
		try {
			return Component.literal(MessageFormat.format(I18n.get(s), objects));
		} catch (Exception e) {
			return Component.translatable(s, objects);
		}
	}

	public static void pleaseWait() {
		SystemToast.add(
				Minecraft.getInstance().getToasts(),
				JADE_PLEASE_WAIT,
				Component.translatable("toast.jade.please_wait.1"),
				Component.translatable("toast.jade.please_wait.2"));
	}
}
