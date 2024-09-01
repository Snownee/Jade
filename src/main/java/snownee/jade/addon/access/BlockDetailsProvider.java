package snownee.jade.addon.access;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BarrelBlock;
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
		if (blockState.hasProperty(BlockStateProperties.OPEN) && !(blockState.getBlock() instanceof BarrelBlock)) {
			AccessibilityPlugin.replaceTitle(tooltip, "block.door_" + (blockState.getValue(BlockStateProperties.OPEN) ? "open" : "closed"));
		}
		if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)) {
			AccessibilityPlugin.replaceTitle(tooltip, "block.waterlogged");
		}
		if (blockState.hasProperty(BlockStateProperties.LIT) && blockState.getValue(BlockStateProperties.LIT)) {
			AccessibilityPlugin.replaceTitle(tooltip, "block.lit");
		}
		if (blockState.hasProperty(BlockStateProperties.INVERTED) && blockState.getValue(BlockStateProperties.INVERTED)) {
			AccessibilityPlugin.replaceTitle(tooltip, "block.inverted");
		}
		if (blockState.hasProperty(BlockStateProperties.EYE) && blockState.getValue(BlockStateProperties.EYE)) {
			AccessibilityPlugin.replaceTitle(tooltip, "block.eye");
		}
		if (blockState.hasProperty(BlockStateProperties.OMINOUS) && blockState.getValue(BlockStateProperties.OMINOUS)) {
			AccessibilityPlugin.replaceTitle(tooltip, "block.ominous");
		}
		if (blockState.hasProperty(BlockStateProperties.MOISTURE) && blockState.getValue(BlockStateProperties.MOISTURE) == 7) {
			AccessibilityPlugin.replaceTitle(tooltip, "block.hydrated");
		}
		boolean active = false;
		if (blockState.hasProperty(BlockStateProperties.VAULT_STATE) &&
				blockState.getValue(BlockStateProperties.VAULT_STATE) == VaultState.ACTIVE) {
			active = true;
		} else if (blockState.hasProperty(BlockStateProperties.TRIAL_SPAWNER_STATE) &&
				blockState.getValue(BlockStateProperties.TRIAL_SPAWNER_STATE) == TrialSpawnerState.ACTIVE) {
			active = true;
		}
		if (active) {
			AccessibilityPlugin.replaceTitle(tooltip, "block.active");
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
