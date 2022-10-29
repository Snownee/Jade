package snownee.jade.overlay;

import com.mojang.text2speech.Narrator;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import snownee.jade.Jade;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.IConfigGeneral;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.util.ClientPlatformProxy;

public class WailaTickHandler {

	private static WailaTickHandler INSTANCE = new WailaTickHandler();
	private static Narrator narrator;
	private static String lastNarration = "";
	public TooltipRenderer tooltipRenderer = null;
	public ProgressTracker progressTracker = new ProgressTracker();

	@SuppressWarnings("deprecation")
	public void tickClient() {
		progressTracker.tick();

		IConfigGeneral config = Jade.CONFIG.get().getGeneral();
		if (!config.shouldDisplayTooltip()) {
			tooltipRenderer = null;
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (!ClientPlatformProxy.shouldShowWithOverlay(client, client.screen)) {
			return;
		}
		if (client.keyboardHandler == null) {
			return;
		}

		Level world = client.level;
		Player player = client.player;
		if (world == null || player == null) {
			tooltipRenderer = null;
			return;
		}

		RayTracing.INSTANCE.fire();
		HitResult target = RayTracing.INSTANCE.getTarget();

		Tooltip tooltip = new Tooltip();

		if (target == null) {
			tooltipRenderer = null;
			return;
		}

		Accessor<?> accessor = null;
		if (target instanceof BlockHitResult blockTarget && blockTarget.getType() != HitResult.Type.MISS) {
			BlockState state = world.getBlockState(blockTarget.getBlockPos());
			BlockEntity tileEntity = world.getBlockEntity(blockTarget.getBlockPos());
			/* off */
			accessor = WailaClientRegistration.INSTANCE.blockAccessor()
					.blockState(state)
					.blockEntity(tileEntity)
					.level(world)
					.player(player)
					.serverData(ObjectDataCenter.getServerData())
					.serverConnected(ObjectDataCenter.serverConnected)
					.hit(blockTarget)
					.fakeBlock(DatapackBlockManager.getFakeBlock(world, blockTarget.getBlockPos()))
					.build();
			/* on */
		} else if (target instanceof EntityHitResult entityTarget) {
			/* off */
			accessor = WailaClientRegistration.INSTANCE.entityAccessor()
					.level(world)
					.player(player)
					.serverData(ObjectDataCenter.getServerData())
					.serverConnected(ObjectDataCenter.serverConnected)
					.hit(entityTarget)
					.entity(entityTarget.getEntity())
					.build();
			/* on */
		}

		Accessor<?> originalAccessor = accessor;
		for (JadeRayTraceCallback callback : WailaClientRegistration.INSTANCE.rayTraceCallbacks) {
			accessor = callback.onRayTrace(target, accessor, originalAccessor);
		}
		ObjectDataCenter.set(accessor);
		if (accessor == null || accessor.getHitResult() == null) {
			tooltipRenderer = null;
			return;
		}

		if (!accessor.shouldDisplay()) {
			tooltipRenderer = null;
			return;
		}
		boolean showDetails = ClientPlatformProxy.isShowDetailsPressed();
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

		if (config.getDisplayMode() == DisplayMode.LITE && !showDetails) {
			Tooltip dummyTooltip = new Tooltip();
			accessor._gatherComponents($ -> {
				if (Math.abs(WailaCommonRegistration.INSTANCE.priorities.get($)) > 5000) {
					return tooltip;
				} else {
					return dummyTooltip;
				}
			});
			if (!dummyTooltip.isEmpty()) {
				tooltip.sneakyDetails = true;
			}
		} else {
			accessor._gatherComponents($ -> tooltip);
		}

		for (JadeTooltipCollectedCallback callback : WailaClientRegistration.INSTANCE.tooltipCollectedCallbacks) {
			callback.onTooltipCollected(tooltip, accessor);
		}
		tooltipRenderer = new TooltipRenderer(tooltip, true);
	}

	private static Narrator getNarrator() {
		return narrator == null ? narrator = Narrator.getNarrator() : narrator;
	}

	public static WailaTickHandler instance() {
		if (INSTANCE == null)
			INSTANCE = new WailaTickHandler();
		return INSTANCE;
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
