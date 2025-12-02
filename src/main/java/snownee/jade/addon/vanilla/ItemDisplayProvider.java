package snownee.jade.addon.vanilla;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display.ItemDisplay;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

public class ItemDisplayProvider implements IEntityComponentProvider {
	public static final ItemDisplayProvider INSTANCE = new ItemDisplayProvider();

	@Override
	public @Nullable Element getIcon(EntityAccessor accessor, IPluginConfig config, @Nullable Element currentIcon) {
		ItemDisplay itemDisplay = (ItemDisplay) accessor.getEntity();
		if (itemDisplay.getItemStack().isEmpty()) {
			return null;
		}
		return JadeUI.item(itemDisplay.getItemStack());
	}

	@Override
	public Identifier getUid() {
		return JadeIds.MC_ITEM_DISPLAY;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
	}

	@Override
	public boolean isRequired() {
		return true;
	}

}
