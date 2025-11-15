package snownee.jade.addon.core;

import java.util.List;

import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

public class BlockFaceProvider implements IBlockComponentProvider {
	public static final BlockFaceProvider INSTANCE = new BlockFaceProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		tooltip.replace(
				JadeIds.CORE_OBJECT_NAME, lists -> {
					List<LayoutElement> lastList = lists.getLast();
					lastList.add(JadeUI.text(Component.translatable("jade.blockFace", directionName(accessor.getSide()))));
					return lists;
				});
	}

	@Override
	public Identifier getUid() {
		return JadeIds.CORE_BLOCK_FACE;
	}

	@Override
	public int getDefaultPriority() {
		return ObjectNameProvider.ForBlock.INSTANCE.getDefaultPriority() + 30;
	}

	@Override
	public boolean enabledByDefault() {
		return false;
	}

	public static MutableComponent directionName(Direction direction) {
		return Component.translatable("jade." + direction.getSerializedName());
	}

}
