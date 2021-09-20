package snownee.jade.client;

import java.awt.Rectangle;
import java.util.Map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.config.WailaConfig.ConfigOverlay.ConfigOverlayColor;
import mcp.mobius.waila.api.event.WailaRayTraceEvent;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.VanillaPlugin;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public final class ClientHandler {
	private static float savedProgress;
	private static float progressAlpha;
	private static boolean canHarvest;

	@SubscribeEvent
	public static void post(WailaRenderEvent.Post event) {
		if (!PluginConfig.INSTANCE.get(VanillaPlugin.BREAKING_PROGRESS)) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		MultiPlayerGameMode playerController = mc.gameMode;
		if (playerController == null || playerController.destroyBlockPos == null) {
			return;
		}
		BlockState state = mc.level.getBlockState(playerController.destroyBlockPos);
		if (playerController.isDestroying())
			canHarvest = ForgeHooks.isCorrectToolForDrops(state, mc.player);
		int color = canHarvest ? 0xFFFFFF : 0xFF4444;
		Rectangle rect = event.getPosition();
		int height = rect.height;
		int width = rect.width;
		if (!Waila.CONFIG.get().getOverlay().getSquare()) {
			height -= 1;
			width -= 2;
		}
		progressAlpha += mc.getDeltaFrameTime() * (playerController.isDestroying() ? 0.1F : -0.1F);
		if (playerController.isDestroying()) {
			progressAlpha = Math.min(progressAlpha, 0.53F); //0x88 = 0.53 * 255
			float progress = state.getDestroyProgress(mc.player, mc.player.level, playerController.destroyBlockPos);
			progress = playerController.destroyProgress + mc.getFrameTime() * progress;
			progress = Mth.clamp(progress, 0, 1);
			savedProgress = progress;
		} else {
			progressAlpha = Math.max(progressAlpha, 0);
		}
		color = ConfigOverlayColor.applyAlpha(color, progressAlpha);
		DisplayHelper.fill(event.getPoseStack(), 0, height - 1, width * savedProgress, height, color);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void breakBlock(BreakEvent event) {
		progressAlpha = 1;
	}

	private static final Cache<BlockState, BlockState> CHEST_CACHE = CacheBuilder.newBuilder().build();

	private static BlockState getCorrespondingNormalChest(BlockState state) {
		try {
			return CHEST_CACHE.get(state, () -> {
				ResourceLocation trappedName = state.getBlock().getRegistryName();
				if (trappedName.getPath().startsWith("trapped_")) {
					ResourceLocation chestName = new ResourceLocation(trappedName.getNamespace(), trappedName.getPath().substring(8));
					Block block = ForgeRegistries.BLOCKS.getValue(chestName);
					if (block != null) {
						return copyProperties(state, block.defaultBlockState());
					}
				}
				return state;
			});
		} catch (Exception e) {
			return state;
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> BlockState copyProperties(BlockState oldState, BlockState newState) {
		for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
			Property<T> property = (Property<T>) entry.getKey();
			if (newState.hasProperty(property))
				newState = newState.setValue(property, property.getValueClass().cast(entry.getValue()));
		}
		return newState;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void override(WailaRayTraceEvent event) {
		Player player = event.getTarget().getPlayer();
		if (player.isCreative() || player.isSpectator())
			return;
		if (event.getTarget() instanceof BlockAccessor) {
			BlockAccessor target = (BlockAccessor) event.getTarget();
			if (target.getBlock() instanceof TrappedChestBlock) {
				BlockState state = getCorrespondingNormalChest(target.getBlockState());
				if (state != target.getBlockState()) {
					event.setTarget(new BlockAccessor(state, target.getBlockEntity(), target.getLevel(), player, target.getServerData(), target.getHitResult(), target.isServerConnected()));
				}
			} else if (target.getBlock() instanceof InfestedBlock) {
				Block block = ((InfestedBlock) target.getBlock()).getHostBlock();
				event.setTarget(new BlockAccessor(block.defaultBlockState(), target.getBlockEntity(), target.getLevel(), player, target.getServerData(), target.getHitResult(), target.isServerConnected()));
			} else if (target.getBlock() == Blocks.POWDER_SNOW) {
				Block block = Blocks.SNOW_BLOCK;
				event.setTarget(new BlockAccessor(block.defaultBlockState(), null, target.getLevel(), player, target.getServerData(), target.getHitResult(), target.isServerConnected()));
			}
		}
	}
}
