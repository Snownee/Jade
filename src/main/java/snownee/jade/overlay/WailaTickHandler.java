package snownee.jade.overlay;

import com.mojang.text2speech.Narrator;

import net.minecraft.client.Minecraft;
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
import snownee.jade.Waila;
import snownee.jade.WailaClient;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.IConfigGeneral;
import snownee.jade.api.event.WailaRayTraceEvent;
import snownee.jade.api.event.WailaTooltipEvent;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.impl.EntityAccessorImpl;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.Tooltip;

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

		IConfigGeneral config = WailaClient.CONFIG.get().getGeneral();
		if (!config.shouldDisplayTooltip()) {
			tooltipRenderer = null;
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (!(client.screen instanceof BaseOptionsScreen)) {
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
			accessor = new BlockAccessorImpl(state, tileEntity, world, player, ObjectDataCenter.getServerData(), blockTarget, ObjectDataCenter.serverConnected, DatapackBlockManager.getFakeBlock(world, blockTarget.getBlockPos()));
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

		if (!getNarrator().active() || !WailaClient.CONFIG.get().getGeneral().shouldEnableTextToSpeech())
			return;

		if (Minecraft.getInstance().screen != null && Minecraft.getInstance().options.chatVisibility != ChatVisiblity.HIDDEN)
			return;

		if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 5 > 0) {
			return;
		}
	}

	public static void narrate(ITooltip tooltip, boolean dedupe) {
		if (!getNarrator().active() || tooltip.isEmpty())
			return;
		String narration = tooltip.getMessage();
		if (dedupe && narration.equals(lastNarration))
			return;
		Narrator narrator = getNarrator();
		narrator.say(narration, true);
		lastNarration = narration;
	}
}
