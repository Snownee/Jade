package snownee.jade.addon.core;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum BlockFaceProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		tooltip.replace(JadeIds.CORE_OBJECT_NAME, lists -> {
			List<IElement> lastList = lists.getLast();
			lastList.add(IElementHelper.get().text(Component.translatable("jade.blockFace", directionName(accessor.getSide()))));
			return lists;
		});
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.CORE_BLOCK_FACE;
	}

	@Override
	public int getDefaultPriority() {
		return ObjectNameProvider.getBlock().getDefaultPriority() + 30;
	}

	@Override
	public boolean enabledByDefault() {
		return false;
	}

	public static MutableComponent directionName(Direction direction) {
		return Component.translatable("jade." + direction.getSerializedName());
	}

}
