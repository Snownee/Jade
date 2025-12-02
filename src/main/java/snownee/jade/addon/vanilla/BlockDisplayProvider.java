package snownee.jade.addon.vanilla;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.util.ClientProxy;

public class BlockDisplayProvider implements IEntityComponentProvider {
	public static final BlockDisplayProvider INSTANCE = new BlockDisplayProvider();

	@Override
	public @Nullable Element getIcon(EntityAccessor accessor, IPluginConfig config, @Nullable Element currentIcon) {
		BlockDisplay itemDisplay = (BlockDisplay) accessor.getEntity();
		Block block = itemDisplay.getBlockState().getBlock();
		if (block.asItem() == Items.AIR) {
			return null;
		}
		Element icon = JadeUI.item(new ItemStack(block));
		if (JadeUI.isEmptyElement(icon) && block instanceof LiquidBlock) {
			icon = ClientProxy.elementFromLiquid(itemDisplay.getBlockState());
		}
		return icon;
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_BLOCK_DISPLAY;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
	}

	@Override
	public boolean isRequired() {
		return true;
	}

}
