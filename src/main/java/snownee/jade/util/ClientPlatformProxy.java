package snownee.jade.util;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.ui.IElement;
import snownee.jade.command.DumpHandlersCommand;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.ui.FluidStackElement;
import snownee.jade.network.RequestEntityPacket;
import snownee.jade.network.RequestTilePacket;
import snownee.jade.overlay.DatapackBlockManager;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.overlay.WailaTickHandler;

public final class ClientPlatformProxy {

	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		return UsernameCache.getLastKnownUsername(uuid);
	}

	public static void initModNames(Map<String, String> map) {
		List<IModInfo> mods = ImmutableList.copyOf(ModList.get().getMods());
		for (IModInfo mod : mods) {
			String modid = mod.getModId();
			String name = mod.getDisplayName();
			if (Strings.isNullOrEmpty(name)) {
				StringUtils.capitalize(modid);
			}
			map.put(modid, name);
		}
	}

	public static void init() {
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onEntityJoin);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onEntityLeave);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, ClientPlatformProxy::onTooltip);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onClientTick);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onRenderTick);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onPlayerLeave);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onKeyPressed);
		MinecraftForge.EVENT_BUS.addListener(ClientPlatformProxy::onGui);
		ModLoadingContext.get().registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory((minecraft, screen) -> new HomeConfigScreen(screen)));

	}

	public static void onEntityJoin(EntityJoinWorldEvent event) {
		DatapackBlockManager.onEntityJoin(event.getEntity());
	}

	public static void onEntityLeave(EntityLeaveWorldEvent event) {
		DatapackBlockManager.onEntityLeave(event.getEntity());
	}

	public static void onTooltip(ItemTooltipEvent event) {
		JadeClient.onTooltip(event.getToolTip(), event.getItemStack());
	}

	public static void onRenderTick(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			OverlayRenderer.renderOverlay();
	}

	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			WailaTickHandler.instance().tickClient();
	}

	public static void onPlayerLeave(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		ObjectDataCenter.serverConnected = false;
	}

	public static void registerCommands(RegisterClientCommandsEvent event) {
		DumpHandlersCommand.register(event.getDispatcher());
	}

	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
		JadeClient.onKeyPressed(event.getAction());
	}

	public static void onGui(ScreenEvent.InitScreenEvent event) {
		JadeClient.onGui(event.getScreen());
	}

	public static KeyMapping registerKeyBinding(String desc, int defaultKey) {
		KeyMapping key = new KeyMapping("key.jade." + desc, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM.getOrCreate(defaultKey), Jade.NAME);
		ClientRegistry.registerKeyBinding(key);
		return key;
	}

	public static boolean shouldRegisterRecipeViewerKeys() {
		return ModList.get().isLoaded("jei");
	}

	public static void requestBlockData(BlockEntity blockEntity, boolean showDetails) {
		Jade.NETWORK.sendToServer(new RequestTilePacket(blockEntity, showDetails));
	}

	public static void requestEntityData(Entity entity, boolean showDetails) {
		Jade.NETWORK.sendToServer(new RequestEntityPacket(entity, showDetails));
	}

	public static ItemStack getEntityPickedResult(Entity entity, Player player, EntityHitResult hitResult) {
		return entity.getPickedResult(hitResult);
	}

	public static ItemStack getBlockPickedResult(BlockState state, Player player, BlockHitResult hitResult) {
		return state.getCloneItemStack(hitResult, player.level, hitResult.getBlockPos(), player);
	}

	public static IElement elementFromLiquid(LiquidBlock block) {
		Fluid fluid = block.getFluid();
		FluidStack fluidStack = new FluidStack(fluid, 1);
		return new FluidStackElement(fluidStack);//.size(new Size(18, 18));
	}

}
