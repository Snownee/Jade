package snownee.jade.addon.vanilla;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import snownee.jade.addon.access.AccessibilityPlugin;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.impl.ui.CompoundElement;

public class WaxedProvider implements IBlockComponentProvider {
	public static final WaxedProvider INSTANCE = new WaxedProvider();

	@Override
	public @Nullable Element getIcon(BlockAccessor accessor, IPluginConfig config, Element currentIcon) {
		if (accessor.getPickedResult().isEmpty()) {
			return currentIcon;
		}
		Element largeIcon = JadeUI.item(accessor.getPickedResult());
		if (accessor.getBlockEntity() instanceof SignBlockEntity sign) {
			if (sign.isWaxed()) {
				return new CompoundElement(largeIcon, JadeUI.item(Items.HONEYCOMB.getDefaultInstance(), 0.5f));
			} else {
				return largeIcon;
			}
		}
		return currentIcon;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (IWailaConfig.get().accessibility().getEnableAccessibilityPlugin() &&
				accessor.getBlockEntity() instanceof SignBlockEntity sign &&
				sign.isWaxed()) {
			String objectName = tooltip.getString(JadeIds.CORE_OBJECT_NAME);
			AccessibilityPlugin.replaceTitle(tooltip, objectName, "waxed");
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_WAXED;
	}

	@Override
	public int getDefaultPriority() {
		return ObjectNameProvider.ForBlock.INSTANCE.getDefaultPriority() + 10;
	}
}
