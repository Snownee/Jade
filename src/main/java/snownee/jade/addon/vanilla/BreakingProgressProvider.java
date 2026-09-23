package snownee.jade.addon.vanilla;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.Accessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.JadeIds;
import snownee.jade.api.ui.BoxProgress;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.IBoxProgressProvider;
import snownee.jade.api.ui.MessageType;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.util.CommonProxy;

/**
 * Drives the vanilla block-breaking progress bar rendered below the root tooltip.
 */
public enum BreakingProgressProvider implements IBoxProgressProvider {
	INSTANCE;

	private float progressAlpha;

	public static void register(IWailaClientRegistration registration) {
		registration.addTooltipCollectedCallback((root, accessor) -> root.addProgressProvider(0, INSTANCE));
	}

	@Override
	public @Nullable BoxProgress getProgress(IBoxElement box, @Nullable Accessor<?> accessor, float partialTicks) {
		if (!PluginConfig.INSTANCE.get(JadeIds.MC_BREAKING_PROGRESS)) {
			progressAlpha = 0;
			return null;
		}
		Minecraft mc = Minecraft.getInstance();
		MultiPlayerGameMode playerController = mc.gameMode;
		if (playerController == null || mc.level == null || mc.player == null || !playerController.isDestroying()) {
			progressAlpha = 0;
			return null;
		}
		BlockPos pos = playerController.destroyBlockPos;
		BlockState state = mc.level.getBlockState(pos);
		boolean canHarvest = CommonProxy.isCorrectToolForDrops(state, mc.player, mc.level, pos);
		progressAlpha = Math.min(progressAlpha + mc.getTimer().getGameTimeDeltaTicks() * 0.1F, 0.6F);
		float progress = state.getDestroyProgress(mc.player, mc.player.level(), pos);
		if (playerController.destroyProgress + progress >= 1) {
			progressAlpha = progress = 1;
		} else {
			progress = playerController.destroyProgress + mc.getTimer().getGameTimeDeltaPartialTick(false) * progress;
			progress = Mth.clamp(progress, 0, 1);
		}
		return new BoxProgress(progress, canHarvest ? MessageType.NORMAL : MessageType.FAILURE, null, progressAlpha);
	}

}
