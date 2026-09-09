package snownee.jade.addon.vanilla;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.Accessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.BoxProgress;
import snownee.jade.api.ui.IBoxProgressProvider;
import snownee.jade.api.ui.MessageType;
import snownee.jade.util.CommonProxy;

/**
 * Drives the vanilla block-breaking progress bar rendered below the root tooltip.
 */
public enum BreakingProgressProvider implements IBoxProgressProvider {
	INSTANCE;

	private float savedProgress;
	private float progressAlpha;
	private boolean canHarvest;

	public static void register(IWailaClientRegistration registration) {
		registration.addTooltipCollectedCallback((root, accessor) -> root.addProgressProvider(0, INSTANCE));
	}

	@Override
	public @Nullable BoxProgress getProgress(BoxElement box, Accessor<?> accessor, float partialTicks) {
		if (!IWailaConfig.get().plugin().get(JadeIds.MC_BREAKING_PROGRESS)) {
			progressAlpha = 0;
			return null;
		}
		Minecraft mc = Minecraft.getInstance();
		MultiPlayerGameMode playerController = mc.gameMode;
		if (playerController == null || mc.level == null || mc.player == null) {
			return null;
		}
		BlockPos pos = playerController.destroyBlockPos;
		BlockState state = mc.level.getBlockState(pos);
		if (playerController.isDestroying()) {
			canHarvest = CommonProxy.isCorrectToolForDrops(state, mc.player, mc.level, pos);
		} else if (progressAlpha == 0) {
			return null;
		}
		float deltaTicks = mc.getDeltaTracker().getGameTimeDeltaTicks();
		progressAlpha += deltaTicks * (playerController.isDestroying() ? 0.1F : -0.1F);
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
			return null;
		}
		return new BoxProgress(savedProgress, canHarvest ? MessageType.TITLE : MessageType.FAILURE, null, progressAlpha);
	}

}