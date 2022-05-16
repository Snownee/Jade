package snownee.jade.addon.vanilla;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;
import snownee.jade.Waila;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class PlayerHeadProvider implements IComponentProvider {

	public static final PlayerHeadProvider INSTANCE = new PlayerHeadProvider();
	static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation(Waila.MODID, "object_name");

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.PLAYER_HEAD)) {
			return;
		}
		if (accessor.getBlockEntity() instanceof SkullBlockEntity) {
			SkullBlockEntity tile = (SkullBlockEntity) accessor.getBlockEntity();
			GameProfile profile = tile.getOwnerProfile();
			if (profile == null)
				return;
			tooltip.remove(OBJECT_NAME_TAG);
			tooltip.add(0, new TextComponent(String.format(config.getWailaConfig().getFormatting().getBlockName(), I18n.get(Items.PLAYER_HEAD.getDescriptionId() + ".named", profile.getName()))).withStyle(Waila.CONFIG.get().getOverlay().getColor().getTitle()), OBJECT_NAME_TAG);
		}
	}

}
