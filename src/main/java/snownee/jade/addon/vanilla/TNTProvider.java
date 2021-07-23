package snownee.jade.addon.vanilla;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.VanillaPlugin;

public class TNTProvider implements IComponentProvider {

	public static final TNTProvider INSTANCE = new TNTProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.TNT_STABILITY)) {
			return;
		}
		BlockState state = accessor.getBlockState();
		if (state.getValue(TntBlock.UNSTABLE)) {
			tooltip.add(new TranslatableComponent("jade.tnt.unstable").withStyle(ChatFormatting.RED));
		}
	}

}
