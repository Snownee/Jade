package snownee.jade.addon.vanilla;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueOutput;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

public class ItemBERProvider implements IBlockComponentProvider {
	public static final ItemBERProvider INSTANCE = new ItemBERProvider();

	@Override
	public @Nullable Element getIcon(BlockAccessor accessor, IPluginConfig config, @Nullable Element currentIcon) {
		BlockEntity blockEntity = accessor.getBlockEntity();
		if (blockEntity != null) {
			ItemStack itemStack = accessor.getPickedResult();
			TagValueOutput tagValueOutput = TagValueOutput.createWithContext(
					ProblemReporter.ScopedCollector.DISCARDING,
					accessor.getLevel().registryAccess());
			//noinspection deprecation
			blockEntity.removeComponentsFromTag(tagValueOutput);
			BlockItem.setBlockEntityData(itemStack, blockEntity.getType(), tagValueOutput);
			itemStack.applyComponents(blockEntity.collectComponents());
			return JadeUI.item(itemStack);
		}
		return null;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_ITEM_BER;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

}
