package snownee.jade.addon.vanilla;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.Waila;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

public class FallingBlockProvider implements IEntityComponentProvider {
	public static final FallingBlockProvider INSTANCE = new FallingBlockProvider();
	static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation(Waila.MODID, "object_name");

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		FallingBlockEntity entity = (FallingBlockEntity) accessor.getEntity();
		tooltip.remove(OBJECT_NAME_TAG);
		tooltip.add(0, config.getWailaConfig().getFormatting().title(I18n.get(entity.getBlockState().getBlock().getDescriptionId())), OBJECT_NAME_TAG);
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
