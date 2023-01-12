package snownee.jade;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import snownee.jade.addon.vanilla.VanillaPlugin;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.config.IWailaConfig.TTSMode;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.mixin.KeyAccess;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.WailaTickHandler;
import snownee.jade.util.ClientPlatformProxy;
import snownee.jade.util.ModIdentification;
import snownee.jade.util.PlatformProxy;

@SuppressWarnings("deprecation")
public final class JadeClient implements ClientModInitializer {

	public static KeyMapping openConfig;
	public static KeyMapping showOverlay;
	public static KeyMapping toggleLiquid;
	public static KeyMapping showDetails;
	public static KeyMapping narrate;
	public static KeyMapping showRecipes;
	public static KeyMapping showUses;

	@Override
	public void onInitializeClient() {
		ClientPlatformProxy.init();
		for (int i = 320; i < 330; i++) {
			InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(i);
			((KeyAccess) (Object) key).setDisplayName(new LazyLoadedValue<>(() -> Component.translatable(key.getName())));
		}

		openConfig = ClientPlatformProxy.registerKeyBinding("config", 320);
		showOverlay = ClientPlatformProxy.registerKeyBinding("show_overlay", 321);
		toggleLiquid = ClientPlatformProxy.registerKeyBinding("toggle_liquid", 322);
		if (ClientPlatformProxy.shouldRegisterRecipeViewerKeys()) {
			showRecipes = ClientPlatformProxy.registerKeyBinding("show_recipes", 323);
			showUses = ClientPlatformProxy.registerKeyBinding("show_uses", 324);
		}
		narrate = ClientPlatformProxy.registerKeyBinding("narrate", 325);
		showDetails = ClientPlatformProxy.registerKeyBinding("show_details_alternative", InputConstants.UNKNOWN.getValue());

		ClientPlatformProxy.registerReloadListener(ModIdentification.INSTANCE);

		ClientLifecycleEvents.CLIENT_STARTED.register(mc -> Jade.loadComplete());
	}

	public static void onKeyPressed(int action) {
		while (openConfig.consumeClick()) {
			Jade.CONFIG.invalidate();
			Minecraft.getInstance().setScreen(new HomeConfigScreen(null));
		}

		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		while (showOverlay.consumeClick()) {
			DisplayMode mode = general.getDisplayMode();
			if (mode == IWailaConfig.DisplayMode.TOGGLE) {
				general.setDisplayTooltip(!general.shouldDisplayTooltip());
				if (!general.shouldDisplayTooltip() && general.hintOverlayToggle) {
					SystemToast.add(Minecraft.getInstance().getToasts(), SystemToastIds.TUTORIAL_HINT, Component.translatable("toast.jade.toggle_hint.1"), Component.translatable("toast.jade.toggle_hint.2", showOverlay.getTranslatedKeyMessage()));
					general.hintOverlayToggle = false;
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
				Jade.CONFIG.save();
			} else if (WailaTickHandler.instance().tooltipRenderer != null) {
				WailaTickHandler.narrate(WailaTickHandler.instance().tooltipRenderer.getTooltip(), false);
			}
		}
	}

	private static boolean translationChecked;

	public static void onGui(Screen screen) {
		if (!translationChecked && screen instanceof TitleScreen && PlatformProxy.isDevEnv()) {
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

	public static boolean hideModName;

	public static void onTooltip(List<Component> tooltip, ItemStack stack) {
		appendModName(tooltip, stack);
		if (Jade.CONFIG.get().getGeneral().isDebug() && stack.hasTag()) {
			tooltip.add(NbtUtils.toPrettyComponent(stack.getTag()));
		}
	}

	private static void appendModName(List<Component> tooltip, ItemStack stack) {
		if (hideModName || !Jade.CONFIG.get().getGeneral().showItemModNameTooltip())
			return;
		String name = String.format(Jade.CONFIG.get().getFormatting().getModName(), ModIdentification.getModName(stack));
		tooltip.add(Component.literal(name));
	}

	@Nullable
	public static Accessor<?> builtInOverrides(HitResult hitResult, @Nullable Accessor<?> accessor, @Nullable Accessor<?> originalAccessor) {
		if (accessor instanceof BlockAccessor target) {
			Player player = accessor.getPlayer();
			if (player.isCreative() || player.isSpectator())
				return accessor;
			IWailaClientRegistration client = VanillaPlugin.CLIENT_REGISTRATION;
			if (target.getBlock() instanceof TrappedChestBlock) {
				BlockState state = VanillaPlugin.getCorrespondingNormalChest(target.getBlockState());
				if (state != target.getBlockState()) {
					return client.blockAccessor().from(target).blockState(state).build();
				}
			} else if (target.getBlock() instanceof InfestedBlock) {
				Block block = ((InfestedBlock) target.getBlock()).getHostBlock();
				return client.blockAccessor().from(target).blockState(block.defaultBlockState()).build();
			} else if (target.getBlock() == Blocks.POWDER_SNOW) {
				Block block = Blocks.SNOW_BLOCK;
				return client.blockAccessor().from(target).blockState(block.defaultBlockState()).build();
			}
		}
		return accessor;
	}

	private static float savedProgress;
	private static float progressAlpha;
	private static boolean canHarvest;

	public static void drawBreakingProgress(ITooltip tooltip, Rect2i rect, PoseStack matrixStack, Accessor<?> accessor) {
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
			canHarvest = PlatformProxy.isCorrectToolForDrops(state, mc.player);
		int color = canHarvest ? 0xFFFFFF : 0xFF4444;
		int height = rect.getHeight();
		int width = rect.getWidth();
		if (!IWailaConfig.get().getOverlay().getSquare()) {
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
		DisplayHelper.fill(matrixStack, 0, height - 1, width * savedProgress, height, color);
	}

}
