package snownee.jade.addon.vanilla;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public class TNTStabilityProvider implements IBlockComponentProvider {
	public static final TNTStabilityProvider INSTANCE = new TNTStabilityProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		if (state.getValue(TntBlock.UNSTABLE)) {
			tooltip.add(IThemeHelper.get().danger(Component.translatable("jade.tnt.unstable")));
		}
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_TNT_STABILITY;
	}

}
