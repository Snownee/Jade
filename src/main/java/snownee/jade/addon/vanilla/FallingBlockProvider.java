package snownee.jade.addon.vanilla;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;

public class FallingBlockProvider implements IEntityComponentProvider {
	public static final FallingBlockProvider INSTANCE = new FallingBlockProvider();
	static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation(Waila.MODID, "object_name");

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		FallingBlockEntity entity = (FallingBlockEntity) accessor.getEntity();
		tooltip.remove(OBJECT_NAME_TAG);
		tooltip.add(0, new TextComponent(String.format(config.getWailaConfig().getFormatting().getBlockName(), I18n.get(entity.getBlockState().getBlock().getDescriptionId()))).withStyle(Waila.CONFIG.get().getOverlay().getColor().getTitle()), OBJECT_NAME_TAG);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IElement getIcon(EntityAccessor accessor, IPluginConfig config, IElement currentIcon) {
		FallingBlockEntity entity = (FallingBlockEntity) accessor.getEntity();
		ItemStack stack = new ItemStack(entity.getBlockState().getBlock());
		if (stack.isEmpty()) {
			return currentIcon;
		}
		return VanillaPlugin.getElementHelper().item(stack);
	}

}
