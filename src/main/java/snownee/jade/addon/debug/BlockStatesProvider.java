package snownee.jade.addon.debug;

import java.util.Collection;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;

public class BlockStatesProvider implements IBlockComponentProvider {
	public static final BlockStatesProvider INSTANCE = new BlockStatesProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		Collection<Property<?>> properties = state.getProperties();
		if (properties.isEmpty()) {
			return;
		}
		IThemeHelper t = IThemeHelper.get();
		ITooltip box = JadeUI.tooltip();
		properties.forEach(p -> {
			Comparable<?> value = state.getValue(p);
			MutableComponent valueText = Component.literal(" " + value).withStyle();
			if (p instanceof BooleanProperty) {
				valueText = value == Boolean.TRUE ? t.success(valueText) : t.danger(valueText);
			}
			box.add(Component.literal(p.getName() + ":").append(valueText));
		});
		tooltip.add(JadeUI.box(box, BoxStyle.nestedBox()).flexGrow(1));
	}

	@Override
	public Identifier getUid() {
		return JadeIds.DEBUG_BLOCK_STATES;
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
