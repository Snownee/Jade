package mcp.mobius.waila.overlay;

import java.util.List;

import com.mojang.text2speech.Narrator;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.addons.core.CorePlugin;
import mcp.mobius.waila.api.IElement;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.event.WailaTooltipEvent;
import mcp.mobius.waila.api.impl.DataAccessor;
import mcp.mobius.waila.api.impl.MetaDataProvider;
import mcp.mobius.waila.api.impl.Tooltip;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import mcp.mobius.waila.network.RequestEntityPacket;
import mcp.mobius.waila.network.RequestTilePacket;
import mcp.mobius.waila.overlay.element.TextElement;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextProcessing;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Waila.MODID, value = Dist.CLIENT)
public class WailaTickHandler {

    public static WailaTickHandler INSTANCE = new WailaTickHandler();
    private static Narrator narrator;
    private static String lastNarration = "";
    public TooltipRenderer tooltipRenderer = null;
    public MetaDataProvider handler = new MetaDataProvider();

    public void tickClient() {
        if (!Waila.CONFIG.get().getGeneral().shouldDisplayTooltip()) {
            tooltipRenderer = null;
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.isGamePaused() || client.currentScreen != null || client.keyboardListener == null) {
            return;
        }

        World world = client.world;
        PlayerEntity player = client.player;
        if (world == null || player == null) {
            tooltipRenderer = null;
            return;
        }

        RayTracing.INSTANCE.fire();
        RayTraceResult target = RayTracing.INSTANCE.getTarget();

        Tooltip currentTip = new Tooltip();
        Tooltip currentTipBody = new Tooltip();

        if (target == null || target.getType() == Type.MISS) {
            tooltipRenderer = null;
            return;
        }
        DataAccessor accessor = DataAccessor.INSTANCE;
        Entity targetEntity = null;
        if (target.getType() == RayTraceResult.Type.BLOCK) {
            accessor.set(world, player, target);

            if (accessor.serverConnected && accessor.getTileEntity() != null && Waila.CONFIG.get().getGeneral().shouldDisplayTooltip()) {
                if (accessor.isTimeElapsed(MetaDataProvider.rateLimiter)) {
                    accessor.resetTimer();
                    if (!WailaRegistrar.INSTANCE.getBlockNBTProviders(accessor.getTileEntity()).isEmpty())
                        Waila.NETWORK.sendToServer(new RequestTilePacket(accessor.getTileEntity()));
                }
                if (DataAccessor.INSTANCE.serverData == null) {
                    if (!WailaRegistrar.INSTANCE.getBlockNBTProviders(accessor.getTileEntity()).isEmpty())
                        return;
                }
            }
        } else if (target.getType() == RayTraceResult.Type.ENTITY) {
            accessor.set(world, player, target);

            if (accessor.serverConnected && accessor.getEntity() != null && Waila.CONFIG.get().getGeneral().shouldDisplayTooltip()) {
                if (accessor.isTimeElapsed(MetaDataProvider.rateLimiter)) {
                    accessor.resetTimer();
                    if (!WailaRegistrar.INSTANCE.getEntityNBTProviders(accessor.getEntity()).isEmpty())
                        Waila.NETWORK.sendToServer(new RequestEntityPacket(accessor.getEntity()));
                }
                if (DataAccessor.INSTANCE.serverData == null) {
                    if (!WailaRegistrar.INSTANCE.getEntityNBTProviders(accessor.getEntity()).isEmpty())
                        return;
                }
            }

            targetEntity = RayTracing.INSTANCE.getTargetEntity(); // This need to be replaced by the override check.

            if (targetEntity == null) {
                return;
            }
        }
        accessor.pickedResult = RayTracing.INSTANCE.getTargetStack();

        instance().handler.gatherComponents(targetEntity, accessor, currentTip, TooltipPosition.HEAD);
        instance().handler.gatherComponents(targetEntity, accessor, currentTipBody, TooltipPosition.BODY);
        if (Waila.CONFIG.get().getGeneral().shouldShiftForDetails() && !currentTipBody.isEmpty() && !player.isSecondaryUseActive()) {
            currentTip.add(new TranslationTextComponent("tooltip.waila.sneak_for_details").setStyle(Style.EMPTY.setItalic(true)));
        } else {
            currentTip.lines.addAll(currentTipBody.lines);
        }
        instance().handler.gatherComponents(targetEntity, accessor, currentTip, TooltipPosition.TAIL);

        tooltipRenderer = new TooltipRenderer(currentTip, !accessor.pickedResult.isEmpty());
    }

    private static Narrator getNarrator() {
        return narrator == null ? narrator = Narrator.getNarrator() : narrator;
    }

    public static WailaTickHandler instance() {
        if (INSTANCE == null)
            INSTANCE = new WailaTickHandler();
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onTooltip(WailaTooltipEvent event) {
        if (event.getTooltip().isEmpty())
            return;

        if (!getNarrator().active() || !Waila.CONFIG.get().getGeneral().shouldEnableTextToSpeech())
            return;

        if (Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().gameSettings.chatVisibility != ChatVisibility.HIDDEN)
            return;

        if (Minecraft.getInstance().world != null && Minecraft.getInstance().world.getGameTime() % 5 > 0) {
            return;
        }

        List<IElement> elements = event.getTooltip().get(CorePlugin.TAG_OBJECT_NAME);
        for (IElement element : elements) {
            if (element instanceof TextElement) {
                String narrate = TextProcessing.func_244782_a(((TextElement) element).component);
                if (lastNarration.equalsIgnoreCase(narrate))
                    return;
                getNarrator().clear();
                getNarrator().say(narrate, true);
                lastNarration = narrate;
            }
        }

    }
}
