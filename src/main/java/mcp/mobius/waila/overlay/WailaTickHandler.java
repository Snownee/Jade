package mcp.mobius.waila.overlay;

import java.util.List;

import com.mojang.text2speech.Narrator;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.WailaClient;
import mcp.mobius.waila.addons.core.CorePlugin;
import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.WailaConfig.ConfigGeneral;
import mcp.mobius.waila.api.config.WailaConfig.DisplayMode;
import mcp.mobius.waila.api.event.WailaRayTraceEvent;
import mcp.mobius.waila.api.event.WailaTooltipEvent;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.gui.OptionsScreen;
import mcp.mobius.waila.impl.BlockAccessorImpl;
import mcp.mobius.waila.impl.EntityAccessorImpl;
import mcp.mobius.waila.impl.ObjectDataCenter;
import mcp.mobius.waila.impl.Tooltip;
import mcp.mobius.waila.impl.ui.TextElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

	@SuppressWarnings("deprecation")
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

		Accessor<?> accessor = null;
		if (target instanceof BlockHitResult) {
			BlockHitResult blockTarget = (BlockHitResult) target;
			BlockState state = world.getBlockState(blockTarget.getBlockPos());
			BlockEntity tileEntity = world.getBlockEntity(blockTarget.getBlockPos());
			accessor = new BlockAccessorImpl(state, tileEntity, world, player, ObjectDataCenter.getServerData(), blockTarget, ObjectDataCenter.serverConnected);
		} else if (target instanceof EntityHitResult) {
			EntityHitResult entityTarget = (EntityHitResult) target;
			accessor = new EntityAccessorImpl(entityTarget.getEntity(), world, player, ObjectDataCenter.getServerData(), entityTarget, ObjectDataCenter.serverConnected);
		}

		WailaRayTraceEvent event = new WailaRayTraceEvent(accessor, target);
		MinecraftForge.EVENT_BUS.post(event);
		ObjectDataCenter.set(accessor = event.getAccessor());
		if (accessor == null || accessor.getHitResult() == null) {
			tooltipRenderer = null;
			return;
		}

		if (!accessor.shouldDisplay()) {
			tooltipRenderer = null;
			return;
		}
		boolean showDetails = WailaClient.showDetails.isDown();
		if (accessor.isServerConnected()) {
			boolean request = accessor.shouldRequestData();
			if (ObjectDataCenter.isTimeElapsed(ObjectDataCenter.rateLimiter)) {
				ObjectDataCenter.resetTimer();
				if (request)
					accessor._requestData(showDetails);
			}
			if (request && ObjectDataCenter.getServerData() == null) {
				return;
			}
		}

		gatherComponents(accessor, currentTip, TooltipPosition.HEAD);
		gatherComponents(accessor, currentTipBody, TooltipPosition.BODY);
		if (config.getDisplayMode() == DisplayMode.LITE && !currentTipBody.isEmpty() && !showDetails) {
			currentTip.sneakyDetails = true;
		} else {
			currentTip.lines.addAll(currentTipBody.lines);
		}
		gatherComponents(accessor, currentTip, TooltipPosition.TAIL);

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

	@SuppressWarnings("deprecation")
	public static void gatherComponents(Accessor<?> accessor, Tooltip tooltip, TooltipPosition position) {
		accessor._setTooltipPosition(position);
		accessor._gatherComponents(tooltip);
		accessor._setTooltipPosition(null);
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
