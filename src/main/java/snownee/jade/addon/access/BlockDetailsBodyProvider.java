package snownee.jade.addon.access;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.BigDripleafStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import snownee.jade.addon.core.BlockFaceProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class BlockDetailsBodyProvider implements IBlockComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState blockState = accessor.getBlockState();
		Block block = blockState.getBlock();
		if (block instanceof RedStoneWireBlock) {
			List<Component> list = Lists.newArrayListWithExpectedSize(4);
			for (Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : RedStoneWireBlock.PROPERTY_BY_DIRECTION.entrySet()) {
				RedstoneSide side = blockState.getValue(entry.getValue());
				if (side != RedstoneSide.NONE) {
					list.add(BlockFaceProvider.directionName(entry.getKey()));
				}
			}
			if (list.isEmpty()) {
				tooltip.add(Component.translatable("jade.access.block.redstone_wire.dot"));
			} else {
				tooltip.add(Component.translatable(
						"jade.access.block.redstone_wire",
						ComponentUtils.formatList(list, ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR)));
			}
			return;
		}
		//TODO client tags?
		if (block instanceof PinkPetalsBlock || block instanceof CampfireBlock || block instanceof DecoratedPotBlock ||
				block instanceof SmallDripleafBlock || block instanceof BigDripleafBlock || block instanceof BigDripleafStemBlock) {
			return;
		}
		Direction facing = null;
		if (blockState.hasProperty(BlockStateProperties.FACING)) {
			facing = blockState.getValue(BlockStateProperties.FACING);
		} else if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		} else if (blockState.hasProperty(BlockStateProperties.FACING_HOPPER)) {
			facing = blockState.getValue(BlockStateProperties.FACING_HOPPER);
		}
		if (facing != null) {
			tooltip.add(Component.translatable("jade.access.block.facing", BlockFaceProvider.directionName(facing)));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_BLOCK_DETAILS_BODY;
	}

	@Override
	public boolean isRequired() {
		return true;
	}
}
