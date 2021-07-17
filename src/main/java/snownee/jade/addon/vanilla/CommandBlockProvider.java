package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import snownee.jade.VanillaPlugin;

public class CommandBlockProvider implements IComponentProvider, IServerDataProvider<TileEntity> {

	public static final CommandBlockProvider INSTANCE = new CommandBlockProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.COMMAND_BLOCK) || !accessor.getServerData().contains("Command")) {
			return;
		}
		tooltip.add(new StringTextComponent("> " + accessor.getServerData().getString("Command")));
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te, boolean showDetails) {
		if (te == null || !player.canUseCommandBlock()) {
			return;
		}
		CommandBlockLogic logic = ((CommandBlockTileEntity) te).getCommandBlockLogic();
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
