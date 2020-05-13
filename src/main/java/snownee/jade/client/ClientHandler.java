package snownee.jade.client;

import java.awt.Rectangle;

import mcp.mobius.waila.api.event.WailaRenderEvent;
import mcp.mobius.waila.api.impl.config.PluginConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.multiplayer.PlayerController;
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
        AbstractGui.fill(rect.x + 1, rect.height + 1, rect.x + 1 + (int) (rect.width * playerController.curBlockDamageMP), rect.height + 2, color);

    }

}
