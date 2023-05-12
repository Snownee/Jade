package snownee.jade.addon.vanilla;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display.ItemDisplay;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum ItemDisplayProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public @Nullable IElement getIcon(EntityAccessor accessor, IPluginConfig config, IElement currentIcon) {
		ItemDisplay itemDisplay = (ItemDisplay) accessor.getEntity();
		if (itemDisplay.getSlot(0).get().isEmpty())
			return null;
		return IElementHelper.get().item(itemDisplay.getSlot(0).get());
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_ITEM_DISPLAY;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
	}

	@Override
	public boolean isRequired() {
		return true;
	}

}
