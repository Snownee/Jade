package snownee.jade.addon.vanilla;

import java.util.List;

import com.mojang.authlib.GameProfile;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITaggableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Items;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import snownee.jade.JadePlugin;

public class PlayerHeadProvider implements IComponentProvider {

	public static final PlayerHeadProvider INSTANCE = new PlayerHeadProvider();
	static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation(Waila.MODID, "object_name");

	@Override
	public void appendHead(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		if (!config.get(JadePlugin.PLAYER_HEAD)) {
			return;
		}
		if (accessor.getTileEntity() instanceof SkullTileEntity) {
			SkullTileEntity tile = (SkullTileEntity) accessor.getTileEntity();
			GameProfile profile = tile.getPlayerProfile();
			if (profile != null) {
				((ITaggableList<ResourceLocation, ITextComponent>) tooltip).setTag(OBJECT_NAME_TAG, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getBlockName(), I18n.format(Items.PLAYER_HEAD.getTranslationKey() + ".named", profile.getName()))));
			}
		}
	}

}
