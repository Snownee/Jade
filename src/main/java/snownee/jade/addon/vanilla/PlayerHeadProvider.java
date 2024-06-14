package snownee.jade.addon.vanilla;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum PlayerHeadProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getBlockEntity() instanceof SkullBlockEntity tile) {
			ResolvableProfile profile = tile.getOwnerProfile();
			if (profile == null || !profile.isResolved()) {
				return;
			}
			String name = profile.name().orElse(null);
			if (StringUtils.isBlank(name)) {
				return;
			}
			if (!name.contains(" ") && !name.contains("§")) {
				name = I18n.get(Items.PLAYER_HEAD.getDescriptionId() + ".named", name);
			}
			tooltip.replace(JadeIds.CORE_OBJECT_NAME, IThemeHelper.get().title(name));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_PLAYER_HEAD;
	}

}
