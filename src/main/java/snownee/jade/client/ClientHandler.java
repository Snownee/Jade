package snownee.jade.client;

import java.awt.Rectangle;
import java.util.Map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.event.WailaRayTraceEvent;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import mcp.mobius.waila.impl.config.PluginConfig;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.VanillaPlugin;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public final class ClientHandler {

	@SubscribeEvent
	public static void post(WailaRenderEvent.Post event) {
		if (!PluginConfig.INSTANCE.get(VanillaPlugin.BREAKING_PROGRESS)) {
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
		int height = rect.height;
		int width = rect.width;
		if (!Waila.CONFIG.get().getOverlay().getSquare()) {
			height -= 1;
			width -= 2;
		}
		float progress = state.getPlayerRelativeBlockHardness(mc.player, mc.player.world, playerController.currentBlock);
		progress = playerController.curBlockDamageMP + mc.getRenderPartialTicks() * progress;
		progress = MathHelper.clamp(progress, 0, 1);
		DisplayHelper.fill(event.getMatrixStack(), 0, height - 1, width * progress, height, color);
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
						return copyProperties(state, block.getDefaultState());
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
				newState = newState.with(property, property.getValueClass().cast(entry.getValue()));
		}
		return newState;
	}

	@SubscribeEvent
	public static void override(WailaRayTraceEvent event) {
		PlayerEntity player = event.getTarget().getPlayer();
		if (player.isCreative() || player.isSpectator())
			return;
		if (event.getTarget() instanceof BlockAccessor) {
			BlockAccessor target = (BlockAccessor) event.getTarget();
			if (target.getBlock() instanceof TrappedChestBlock) {
				BlockState state = getCorrespondingNormalChest(target.getBlockState());
				if (state != target.getBlockState()) {
					event.setTarget(new BlockAccessor(state, target.getTileEntity(), target.getWorld(), player, target.getServerData(), target.getHitResult(), target.isServerConnected()));
				}
			} else if (target.getBlock() instanceof SilverfishBlock) {
				Block block = ((SilverfishBlock) target.getBlock()).getMimickedBlock();
				event.setTarget(new BlockAccessor(block.getDefaultState(), target.getTileEntity(), target.getWorld(), player, target.getServerData(), target.getHitResult(), target.isServerConnected()));
			}
		}
	}
}
