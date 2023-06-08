package snownee.jade.addon.vanilla;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum TNTStabilityProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		if (state.getValue(TntBlock.UNSTABLE)) {
			tooltip.add(new TranslatableComponent("jade.tnt.unstable").withStyle(ChatFormatting.RED));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_TNT_STABILITY;
	}

}
