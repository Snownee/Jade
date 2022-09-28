package snownee.jade.addon.vanilla;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

public enum PlayerHeadProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
		if (accessor.getBlockEntity() instanceof SkullBlockEntity) {
			ItemStack stack = accessor.getPickedResult();
			Minecraft.getInstance().addCustomNbtData(stack, accessor.getBlockEntity());
			return VanillaPlugin.getElementHelper().item(stack);
		}
		return null;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getBlockEntity() instanceof SkullBlockEntity tile) {
			GameProfile profile = tile.getOwnerProfile();
			if (profile == null)
				return;
			String name = profile.getName();
			if (name == null || StringUtils.isBlank(name))
				return;
			tooltip.remove(Identifiers.CORE_OBJECT_NAME);
			tooltip.add(0, config.getWailaConfig().getFormatting().title(I18n.get(Items.PLAYER_HEAD.getDescriptionId() + ".named", name)), Identifiers.CORE_OBJECT_NAME);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_PLAYER_HEAD;
	}

}
