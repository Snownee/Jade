package snownee.jade.addon.vanilla;

import java.util.Collection;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum BlockStatesProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		Collection<Property<?>> properties = state.getProperties();
		if (properties.isEmpty())
			return;
		IElementHelper helper = tooltip.getElementHelper();
		ITooltip box = helper.tooltip();
		properties.forEach(p -> {
			Comparable<?> value = state.getValue(p);
			MutableComponent valueText = Component.literal(" " + value.toString()).withStyle();
			if (p instanceof BooleanProperty)
				valueText = valueText.withStyle(value == Boolean.TRUE ? ChatFormatting.GREEN : ChatFormatting.RED);
			box.add(Component.literal(p.getName() + ":").append(valueText));
		});
		tooltip.add(helper.box(box));
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_BLOCK_STATES;
	}

	@Override
	public int getDefaultPriority() {
		return -4500;
	}

	@Override
	public boolean enabledByDefault() {
		return false;
	}
}
