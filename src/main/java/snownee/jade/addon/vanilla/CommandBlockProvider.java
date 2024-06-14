package snownee.jade.addon.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public enum CommandBlockProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("Command")) {
			return;
		}
		tooltip.add(Component.literal("> " + accessor.getServerData().getString("Command")));
	}

	@Override
	public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
		Player player = accessor.getPlayer();
		if (!player.canUseGameMasterBlocks()) {
			return;
		}
		if (accessor.getBlockEntity() instanceof CommandBlockEntity commandBlock) {
			BaseCommandBlock logic = commandBlock.getCommandBlock();
			String command = logic.getCommand();
			if (command.isEmpty()) {
				return;
			}
			if (command.length() > 40) {
				command = command.substring(0, 37) + "...";
			}
			tag.putString("Command", command);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_COMMAND_BLOCK;
	}

}
