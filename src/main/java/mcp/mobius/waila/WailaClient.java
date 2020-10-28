package mcp.mobius.waila;

import mcp.mobius.waila.api.impl.config.WailaConfig;
import mcp.mobius.waila.gui.GuiConfigHome;
import mcp.mobius.waila.overlay.OverlayRenderer;
import mcp.mobius.waila.overlay.WailaTickHandler;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.extensions.IForgeKeybinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Waila.MODID, value = Dist.CLIENT)
public class WailaClient {

    public static IForgeKeybinding openConfig;
    public static IForgeKeybinding showOverlay;
    public static IForgeKeybinding toggleLiquid;

    public static void initClient() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> ((minecraft, screen) -> new GuiConfigHome(screen)));

        WailaClient.openConfig = new KeyBinding("key.waila.config", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM.getOrMakeInput(320), Waila.NAME);
        WailaClient.showOverlay = new KeyBinding("key.waila.show_overlay", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM.getOrMakeInput(321), Waila.NAME);
        WailaClient.toggleLiquid = new KeyBinding("key.waila.toggle_liquid", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM.getOrMakeInput(322), Waila.NAME);

        ClientRegistry.registerKeyBinding(WailaClient.openConfig.getKeyBinding());
        ClientRegistry.registerKeyBinding(WailaClient.showOverlay.getKeyBinding());
        ClientRegistry.registerKeyBinding(WailaClient.toggleLiquid.getKeyBinding());
    }

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.KeyInputEvent event) {
        if (openConfig == null || showOverlay == null || toggleLiquid == null)
            return;

        while (openConfig.getKeyBinding().isPressed()) {
            Minecraft.getInstance().displayGuiScreen(new GuiConfigHome(null));
        }

        while (showOverlay.getKeyBinding().isPressed()) {
            if (Waila.CONFIG.get().getGeneral().getDisplayMode() == WailaConfig.DisplayMode.TOGGLE) {
                Waila.CONFIG.get().getGeneral().setDisplayTooltip(!Waila.CONFIG.get().getGeneral().shouldDisplayTooltip());
            }
        }

        while (toggleLiquid.getKeyBinding().isPressed()) {
            Waila.CONFIG.get().getGeneral().setDisplayFluids(!Waila.CONFIG.get().getGeneral().shouldDisplayFluids());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        String name = String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModInfo(event.getItemStack()).getName());
        event.getToolTip().add(new StringTextComponent(name));
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
}
