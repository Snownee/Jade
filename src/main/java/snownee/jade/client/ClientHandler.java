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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.jade.JadePlugin;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public final class ClientHandler {

    @SubscribeEvent
    public static void pre(WailaRenderEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.ingameGUI.getTabList().visible) {
            event.setCanceled(true);
        } else if (mc.loadingGui != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void post(WailaRenderEvent.Post event) {
        if (!PluginConfig.INSTANCE.get(JadePlugin.BREAKING_PROGRESS)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        PlayerController playerController = mc.playerController;
        if (playerController == null || !playerController.getIsHittingBlock()) {
            return;
        }
        BlockState state = mc.world.getBlockState(playerController.currentBlock);
        boolean canHarvest = ForgeHooks.canHarvestBlock(state, mc.player, mc.world, playerController.currentBlock);
        int color = canHarvest ? 0x88FFFFFF : 0x88FF4444;
        Rectangle rect = event.getPosition();
        float progress = state.getPlayerRelativeBlockHardness(mc.player, mc.player.world, playerController.currentBlock);
        progress = playerController.curBlockDamageMP + mc.getRenderPartialTicks() * progress;
        progress = MathHelper.clamp(progress, 0, 1);
        AbstractGui.fill(RenderContext.matrixStack, rect.x + 1, rect.y + rect.height, rect.x + 1 + (int) (rect.width * progress), rect.y + rect.height + 1, color);
    }

}
