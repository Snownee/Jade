package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import snownee.jade.JadePlugin;

public class CommandBlockProvider implements IComponentProvider, IServerDataProvider<TileEntity> {

	public static final CommandBlockProvider INSTANCE = new CommandBlockProvider();

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.COMMAND_BLOCK) || !accessor.getServerData().contains("Command")) {
			return;
		}
		tooltip.add(new StringTextComponent("> " + accessor.getServerData().getString("Command")));
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te) {
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
