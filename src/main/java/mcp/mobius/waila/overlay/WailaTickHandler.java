package mcp.mobius.waila.overlay;

import java.util.List;

import com.mojang.text2speech.Narrator;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.addons.core.CorePlugin;
import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.WailaConfig.ConfigGeneral;
import mcp.mobius.waila.api.config.WailaConfig.DisplayMode;
import mcp.mobius.waila.api.event.WailaRayTraceEvent;
import mcp.mobius.waila.api.event.WailaTooltipEvent;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.gui.OptionsScreen;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mcp.mobius.waila.impl.Tooltip;
import mcp.mobius.waila.impl.WailaRegistrar;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.impl.ui.TextElement;
import mcp.mobius.waila.network.RequestEntityPacket;
import mcp.mobius.waila.network.RequestTilePacket;
import mcp.mobius.waila.utils.WailaExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
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

		ConfigGeneral config = Waila.CONFIG.get().getGeneral();
		if (!config.shouldDisplayTooltip()) {
			tooltipRenderer = null;
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (!(client.screen instanceof OptionsScreen)) {
			if (client.isPaused() || client.screen != null || client.keyboardHandler == null) {
				return;
			}
		}

		Level world = client.level;
		Player player = client.player;
		if (world == null || player == null) {
			tooltipRenderer = null;
			return;
		}

		RayTracing.INSTANCE.fire();
		HitResult target = RayTracing.INSTANCE.getTarget();

		Tooltip currentTip = new Tooltip();
		Tooltip currentTipBody = new Tooltip();

		if (target == null || target.getType() == HitResult.Type.MISS) {
			tooltipRenderer = null;
			return;
		}

		Accessor accessor;
		if (target instanceof BlockHitResult) {
			BlockHitResult blockTarget = (BlockHitResult) target;
			BlockState state = world.getBlockState(blockTarget.getBlockPos());
			BlockEntity tileEntity = world.getBlockEntity(blockTarget.getBlockPos());
			accessor = new BlockAccessor(state, tileEntity, world, player, ObjectDataCenter.getServerData(), blockTarget, ObjectDataCenter.serverConnected);
		} else if (target instanceof EntityHitResult) {
			EntityHitResult entityTarget = (EntityHitResult) target;
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

		boolean showDetails = WailaClient.showDetails.isDown();
		if (accessor instanceof BlockAccessor) {
			if (!config.getDisplayBlocks()) {
				tooltipRenderer = null;
				return;
			}
			BlockEntity tileEntity = ((BlockAccessor) accessor).getBlockEntity();
			if (accessor.isServerConnected() && tileEntity != null && config.shouldDisplayTooltip()) {
				if (ObjectDataCenter.isTimeElapsed(ObjectDataCenter.rateLimiter)) {
					ObjectDataCenter.resetTimer();
					if (!WailaRegistrar.INSTANCE.getBlockNBTProviders(tileEntity).isEmpty())
						Waila.NETWORK.sendToServer(new RequestTilePacket(tileEntity, showDetails));
				}
				if (ObjectDataCenter.getServerData() == null) {
					if (!WailaRegistrar.INSTANCE.getBlockNBTProviders(tileEntity).isEmpty())
						return;
				}
			}
		} else if (accessor instanceof EntityAccessor) {
			if (!config.getDisplayEntities()) {
				tooltipRenderer = null;
				return;
			}
			Entity entity = ((EntityAccessor) accessor).getEntity();
			if (accessor.isServerConnected() && entity != null && Waila.CONFIG.get().getGeneral().shouldDisplayTooltip()) {
				if (ObjectDataCenter.isTimeElapsed(ObjectDataCenter.rateLimiter)) {
					ObjectDataCenter.resetTimer();
					if (!WailaRegistrar.INSTANCE.getEntityNBTProviders(entity).isEmpty())
						Waila.NETWORK.sendToServer(new RequestEntityPacket(entity, showDetails));
				}
				if (ObjectDataCenter.getServerData() == null) {
					if (!WailaRegistrar.INSTANCE.getEntityNBTProviders(entity).isEmpty())
						return;
				}
			}
		}

		instance().gatherComponents(accessor, currentTip, TooltipPosition.HEAD);
		instance().gatherComponents(accessor, currentTipBody, TooltipPosition.BODY);
		if (config.getDisplayMode() == DisplayMode.LITE && !currentTipBody.isEmpty() && !showDetails) {
			currentTip.sneakyDetails = true;
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

		if (Minecraft.getInstance().screen != null && Minecraft.getInstance().options.chatVisibility != ChatVisiblity.HIDDEN)
			return;

		if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 5 > 0) {
			return;
		}

		List<IElement> elements = event.getTooltip().get(CorePlugin.TAG_OBJECT_NAME);
		for (IElement element : elements) {
			if (element instanceof TextElement) {
				String narrate = StringDecomposer.getPlainText(((TextElement) element).component);
				if (lastNarration.equalsIgnoreCase(narrate))
					return;
				getNarrator().clear();
				getNarrator().say(narrate, true);
				lastNarration = narrate;
			}
		}

	}
}
