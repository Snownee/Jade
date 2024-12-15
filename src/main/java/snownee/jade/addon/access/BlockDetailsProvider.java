package snownee.jade.addon.access;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class BlockDetailsProvider implements IBlockComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState blockState = accessor.getBlockState();
		Block block = blockState.getBlock();
		String objectName = tooltip.getMessage(JadeIds.CORE_OBJECT_NAME);
		if (blockState.hasProperty(BlockStateProperties.OPEN) && !(block instanceof BarrelBlock)) {
			AccessibilityPlugin.replaceTitle(
					tooltip,
					objectName,
					"block.door_" + (blockState.getValue(BlockStateProperties.OPEN) ? "open" : "closed"));
		}
		if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.waterlogged");
		}
		if (blockState.hasProperty(BlockStateProperties.LIT) && blockState.getValue(BlockStateProperties.LIT)) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.lit");
		}
		if (blockState.hasProperty(BlockStateProperties.INVERTED) && blockState.getValue(BlockStateProperties.INVERTED)) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.inverted");
		}
		if (blockState.hasProperty(BlockStateProperties.EYE) && blockState.getValue(BlockStateProperties.EYE)) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.eye");
		}
		if (blockState.hasProperty(BlockStateProperties.OMINOUS) && blockState.getValue(BlockStateProperties.OMINOUS)) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.ominous");
		}
		if (blockState.hasProperty(BlockStateProperties.MOISTURE) && blockState.getValue(BlockStateProperties.MOISTURE) == 7) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.hydrated");
		}
		if (blockState.hasProperty(BlockStateProperties.LOCKED) && blockState.getValue(BlockStateProperties.LOCKED)) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.locked");
		}
		if (blockState.hasProperty(BlockStateProperties.EXTENDED) && blockState.getValue(BlockStateProperties.EXTENDED)) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.extended");
		}
		if (blockState.hasProperty(BlockStateProperties.CAN_SUMMON) && blockState.getValue(BlockStateProperties.CAN_SUMMON)) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.summonable");
		}
		if (blockState.hasProperty(BlockStateProperties.HATCH)) {
			int i = blockState.getValue(BlockStateProperties.HATCH);
			if (i == 1) {
				AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.hatch.1");
			} else if (i == 2) {
				AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.hatch.2");
			}
		}
		if (blockState.hasProperty(BlockStateProperties.POWERED) && blockState.getValue(BlockStateProperties.POWERED)) {
			if (block instanceof RepeaterBlock || block instanceof BaseRailBlock) {
				AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.powered");
			}
		}
		boolean active = false;
		if (blockState.hasProperty(BlockStateProperties.VAULT_STATE) &&
				blockState.getValue(BlockStateProperties.VAULT_STATE) == VaultState.ACTIVE) {
			active = true;
		} else if (blockState.hasProperty(BlockStateProperties.TRIAL_SPAWNER_STATE) &&
				blockState.getValue(BlockStateProperties.TRIAL_SPAWNER_STATE) == TrialSpawnerState.ACTIVE) {
			active = true;
		} else if (blockState.getBlock() instanceof CreakingHeartBlock && blockState.getValue(BlockStateProperties.ACTIVE)) {
			active = true;
		}
		if (active) {
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "block.active");
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_BLOCK_DETAILS;
	}

	@Override
	public int getDefaultPriority() {
		return ObjectNameProvider.getBlock().getDefaultPriority() + 10;
	}
}
