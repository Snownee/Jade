package snownee.jade.addon.vanilla;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

public enum CropProgressProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
		if (accessor.getBlock() == Blocks.WHEAT)
			return VanillaPlugin.getElementHelper().item(new ItemStack(Items.WHEAT));

		if (accessor.getBlock() == Blocks.BEETROOTS)
			return VanillaPlugin.getElementHelper().item(new ItemStack(Items.BEETROOT));

		return null;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		Block block = state.getBlock();

		if (block instanceof CropBlock) {
			CropBlock crop = (CropBlock) block;
			addMaturityTooltip(tooltip, state.getValue(crop.getAgeProperty()) / (float) crop.getMaxAge());
		} else if (state.hasProperty(BlockStateProperties.AGE_7)) {
			addMaturityTooltip(tooltip, state.getValue(BlockStateProperties.AGE_7) / 7F);
		} else if (state.hasProperty(BlockStateProperties.AGE_2)) {
			addMaturityTooltip(tooltip, state.getValue(BlockStateProperties.AGE_2) / 2.0F);
		}
	}

	private static void addMaturityTooltip(ITooltip tooltip, float growthValue) {
		growthValue *= 100.0F;
		if (growthValue < 100.0F)
			tooltip.add(new TranslatableComponent("tooltip.jade.crop_growth", String.format("%.0f%%", growthValue)));
		else
			tooltip.add(new TranslatableComponent("tooltip.jade.crop_growth", new TranslatableComponent("tooltip.jade.crop_mature").withStyle(ChatFormatting.GREEN)));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_CROP_PROGRESS;
	}

}
