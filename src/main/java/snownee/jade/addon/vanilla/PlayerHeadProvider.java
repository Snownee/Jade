package snownee.jade.addon.vanilla;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum PlayerHeadProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getBlockEntity() instanceof SkullBlockEntity) {
			SkullBlockEntity tile = (SkullBlockEntity) accessor.getBlockEntity();
			GameProfile profile = tile.getOwnerProfile();
			if (profile == null)
				return;
			tooltip.remove(Identifiers.CORE_OBJECT_NAME);
			tooltip.add(0, config.getWailaConfig().getFormatting().title(I18n.get(Items.PLAYER_HEAD.getDescriptionId() + ".named", profile.getName())), Identifiers.CORE_OBJECT_NAME);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_PLAYER_HEAD;
	}

}
