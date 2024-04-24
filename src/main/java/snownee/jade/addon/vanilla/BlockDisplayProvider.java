package snownee.jade.addon.vanilla;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.overlay.RayTracing;
import snownee.jade.util.ClientProxy;

public enum BlockDisplayProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public @Nullable IElement getIcon(EntityAccessor accessor, IPluginConfig config, IElement currentIcon) {
		BlockDisplay itemDisplay = (BlockDisplay) accessor.getEntity();
		Block block = itemDisplay.getBlockState().getBlock();
		if (block.asItem() == Items.AIR) {
			return null;
		}
		IElement icon = ItemStackElement.of(new ItemStack(block));
		if (RayTracing.isEmptyElement(icon) && block instanceof LiquidBlock) {
			icon = ClientProxy.elementFromLiquid(itemDisplay.getBlockState());
		}
		return icon;
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_BLOCK_DISPLAY;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
	}

	@Override
	public boolean isRequired() {
		return true;
	}

}
