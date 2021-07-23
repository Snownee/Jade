package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import snownee.jade.VanillaPlugin;

public class CommandBlockProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {

	public static final CommandBlockProvider INSTANCE = new CommandBlockProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.COMMAND_BLOCK) || !accessor.getServerData().contains("Command")) {
			return;
		}
		tooltip.add(new TextComponent("> " + accessor.getServerData().getString("Command")));
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, BlockEntity te, boolean showDetails) {
		if (te == null || !player.canUseGameMasterBlocks()) {
			return;
		}
		BaseCommandBlock logic = ((CommandBlockEntity) te).getCommandBlock();
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
