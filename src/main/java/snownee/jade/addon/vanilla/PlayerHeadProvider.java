package snownee.jade.addon.vanilla;

import com.mojang.authlib.GameProfile;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Items;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import snownee.jade.VanillaPlugin;

public class PlayerHeadProvider implements IComponentProvider {

	public static final PlayerHeadProvider INSTANCE = new PlayerHeadProvider();
	static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation(Waila.MODID, "object_name");

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.PLAYER_HEAD)) {
			return;
		}
		if (accessor.getTileEntity() instanceof SkullTileEntity) {
			SkullTileEntity tile = (SkullTileEntity) accessor.getTileEntity();
			GameProfile profile = tile.getPlayerProfile();
			if (profile == null)
				return;
			tooltip.remove(OBJECT_NAME_TAG);
			tooltip.add(0, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getBlockName(), I18n.format(Items.PLAYER_HEAD.getTranslationKey() + ".named", profile.getName()))), OBJECT_NAME_TAG);
		}
	}

}
