package snownee.jade.addon.vanilla;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum FallingBlockProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		FallingBlockEntity entity = (FallingBlockEntity) accessor.getEntity();
		tooltip.remove(Identifiers.CORE_OBJECT_NAME);
		tooltip.add(0, config.getWailaConfig().getFormatting().title(I18n.get(entity.getBlockState().getBlock().getDescriptionId())), Identifiers.CORE_OBJECT_NAME);
	}

	@Override
	public IElement getIcon(EntityAccessor accessor, IPluginConfig config, IElement currentIcon) {
		FallingBlockEntity entity = (FallingBlockEntity) accessor.getEntity();
		ItemStack stack = new ItemStack(entity.getBlockState().getBlock());
		if (stack.isEmpty()) {
			return currentIcon;
		}
		return IElementHelper.get().item(stack);
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_FALLING_BLOCK;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

}
