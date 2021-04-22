package snownee.jade.addon.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.TNTBlock;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.jade.JadePlugin;

public class TNTProvider implements IComponentProvider {

	public static final TNTProvider INSTANCE = new TNTProvider();

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.TNT_STABILITY)) {
			return;
		}
		BlockState state = accessor.getBlockState();
		if (state.get(TNTBlock.UNSTABLE)) {
			tooltip.add(new TranslationTextComponent("jade.tnt.unstable").mergeStyle(TextFormatting.RED));
		}
	}

}
