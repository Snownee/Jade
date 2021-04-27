package mcp.mobius.waila.overlay;

import java.util.List;

import com.mojang.text2speech.Narrator;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.addons.core.CorePlugin;
import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.event.WailaRayTraceEvent;
import mcp.mobius.waila.api.event.WailaTooltipEvent;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mcp.mobius.waila.impl.Tooltip;
import mcp.mobius.waila.impl.WailaRegistrar;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.network.RequestEntityPacket;
import mcp.mobius.waila.network.RequestTilePacket;
import mcp.mobius.waila.overlay.element.TextElement;
import mcp.mobius.waila.utils.WailaExceptionHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextProcessing;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Waila.MODID, value = Dist.CLIENT)
public class WailaTickHandler {

	private static WailaTickHandler INSTANCE = new WailaTickHandler();
	private static Narrator narrator;
	private static String lastNarration = "";
	public TooltipRenderer tooltipRenderer = null;
	public ProgressTracker progressTracker = new ProgressTracker();

	public void tickClient() {
		progressTracker.tick();

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

		if (target == null || target.getType() == RayTraceResult.Type.MISS) {
			tooltipRenderer = null;
			return;
		}

		Accessor accessor;
		if (target instanceof BlockRayTraceResult) {
			BlockRayTraceResult blockTarget = (BlockRayTraceResult) target;
			BlockState state = world.getBlockState(blockTarget.getPos());
			TileEntity tileEntity = world.getTileEntity(blockTarget.getPos());
			accessor = new BlockAccessor(state, tileEntity, world, player, ObjectDataCenter.getServerData(), blockTarget, ObjectDataCenter.serverConnected);
		} else if (target instanceof EntityRayTraceResult) {
			EntityRayTraceResult entityTarget = (EntityRayTraceResult) target;
			accessor = new EntityAccessor(entityTarget.getEntity(), world, player, ObjectDataCenter.getServerData(), entityTarget, ObjectDataCenter.serverConnected);
		} else {
			tooltipRenderer = null;
			return;
		}

		WailaRayTraceEvent event = new WailaRayTraceEvent(accessor);
		MinecraftForge.EVENT_BUS.post(event);
		ObjectDataCenter.set(accessor = event.getTarget());
		if (accessor == null || accessor.getHitResult() == null)
			return;

		if (accessor instanceof BlockAccessor) {
			TileEntity tileEntity = ((BlockAccessor) accessor).getTileEntity();
			if (accessor.isServerConnected() && tileEntity != null && Waila.CONFIG.get().getGeneral().shouldDisplayTooltip()) {
				if (ObjectDataCenter.isTimeElapsed(ObjectDataCenter.rateLimiter)) {
					ObjectDataCenter.resetTimer();
					if (!WailaRegistrar.INSTANCE.getBlockNBTProviders(tileEntity).isEmpty())
						Waila.NETWORK.sendToServer(new RequestTilePacket(tileEntity));
				}
				if (ObjectDataCenter.getServerData() == null) {
					if (!WailaRegistrar.INSTANCE.getBlockNBTProviders(tileEntity).isEmpty())
						return;
				}
			}
		} else if (accessor instanceof EntityAccessor) {
			Entity entity = ((EntityAccessor) accessor).getEntity();
			if (accessor.isServerConnected() && entity != null && Waila.CONFIG.get().getGeneral().shouldDisplayTooltip()) {
				if (ObjectDataCenter.isTimeElapsed(ObjectDataCenter.rateLimiter)) {
					ObjectDataCenter.resetTimer();
					if (!WailaRegistrar.INSTANCE.getEntityNBTProviders(entity).isEmpty())
						Waila.NETWORK.sendToServer(new RequestEntityPacket(entity));
				}
				if (ObjectDataCenter.getServerData() == null) {
					if (!WailaRegistrar.INSTANCE.getEntityNBTProviders(entity).isEmpty())
						return;
				}
			}
		}

		instance().gatherComponents(accessor, currentTip, TooltipPosition.HEAD);
		instance().gatherComponents(accessor, currentTipBody, TooltipPosition.BODY);
		if (Waila.CONFIG.get().getGeneral().shouldShiftForDetails() && !currentTipBody.isEmpty() && !player.isSecondaryUseActive()) {
			currentTip.sneakDetails = true;
		} else {
			currentTip.lines.addAll(currentTipBody.lines);
		}
		instance().gatherComponents(accessor, currentTip, TooltipPosition.TAIL);

		tooltipRenderer = new TooltipRenderer(currentTip, true);
	}

	private static Narrator getNarrator() {
		return narrator == null ? narrator = Narrator.getNarrator() : narrator;
	}

	public static WailaTickHandler instance() {
		if (INSTANCE == null)
			INSTANCE = new WailaTickHandler();
		return INSTANCE;
	}

	public void gatherComponents(Accessor accessor, Tooltip tooltip, TooltipPosition position) {
		accessor.setTooltipPosition(position);
		if (accessor instanceof BlockAccessor) {
			gatherBlockComponents((BlockAccessor) accessor, tooltip, position);
		} else if (accessor instanceof EntityAccessor) {
			gatherEntityComponents((EntityAccessor) accessor, tooltip, position);
		}
		accessor.setTooltipPosition(null);
	}

	private void gatherBlockComponents(BlockAccessor accessor, Tooltip tooltip, TooltipPosition position) {
		Block block = accessor.getBlock();
		List<IComponentProvider> providers = WailaRegistrar.INSTANCE.getBlockProviders(block, position);
		for (IComponentProvider provider : providers) {
			try {
				provider.appendTooltip(tooltip, accessor, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider.getClass().toString(), tooltip);
			}
		}
	}

	private void gatherEntityComponents(EntityAccessor accessor, Tooltip tooltip, TooltipPosition position) {
		List<IEntityComponentProvider> providers = WailaRegistrar.INSTANCE.getEntityProviders(accessor.getEntity(), position);
		for (IEntityComponentProvider provider : providers) {
			try {
				provider.appendTooltip(tooltip, accessor, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider.getClass().toString(), tooltip);
			}
		}
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
