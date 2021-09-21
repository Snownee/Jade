package snownee.jade.client;

import java.awt.Rectangle;

import mcp.mobius.waila.api.RenderContext;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.jade.JadePlugin;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public final class ClientHandler {

	private static float savedProgress;
	private static float progressAlpha;
	private static boolean canHarvest;

	@SubscribeEvent
	public static void post(WailaRenderEvent.Post event) {
		if (!PluginConfig.INSTANCE.get(JadePlugin.BREAKING_PROGRESS)) {
			progressAlpha = 0;
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		PlayerController playerController = mc.playerController;
		if (playerController == null || !mc.world.isBlockPresent(playerController.currentBlock)) {
			return;
		}
		BlockState state = mc.world.getBlockState(playerController.currentBlock);
		if (playerController.getIsHittingBlock())
			canHarvest = ForgeHooks.canHarvestBlock(state, mc.player, mc.world, playerController.currentBlock);
		int color = canHarvest ? 0xFFFFFF : 0xFF4444;
		Rectangle rect = event.getPosition();
		progressAlpha += mc.getTickLength() * (playerController.getIsHittingBlock() ? 0.1F : -0.1F);
		if (playerController.getIsHittingBlock()) {
			progressAlpha = Math.min(progressAlpha, 0.53F); //0x88 = 0.53 * 255
			float progress = state.getPlayerRelativeBlockHardness(mc.player, mc.player.world, playerController.currentBlock);
			progress = playerController.curBlockDamageMP + mc.getRenderPartialTicks() * progress;
			progress = MathHelper.clamp(progress, 0, 1);
			savedProgress = progress;
		} else {
			progressAlpha = Math.max(progressAlpha, 0);
		}
		color = applyAlpha(color, progressAlpha);
		AbstractGui.fill(RenderContext.matrixStack, rect.x + 1, rect.y + rect.height, rect.x + 1 + (int) (rect.width * savedProgress), rect.y + rect.height + 1, color);
	}

	private static int applyAlpha(int color, float alpha) {
		int prevAlphaChannel = (color >> 24) & 0xFF;
		if (prevAlphaChannel > 0)
			alpha *= prevAlphaChannel / 256f;
		int alphaChannel = (int) (0xFF * MathHelper.clamp(alpha, 0, 1));
		return (color & 0xFFFFFF) | alphaChannel << 24;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void breakBlock(BreakEvent event) {
		progressAlpha = 1;
	}

}
