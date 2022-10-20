package snownee.jade.addon.vanilla;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.VanillaPlugin;

public class PlayerHeadProvider implements IComponentProvider {

	public static final PlayerHeadProvider INSTANCE = new PlayerHeadProvider();
	static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation(Waila.MODID, "object_name");

	@OnlyIn(Dist.CLIENT)
	public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
		if (accessor.getBlockEntity() instanceof SkullBlockEntity) {
			ItemStack stack = accessor.getPickedResult();
			Minecraft.getInstance().addCustomNbtData(stack, accessor.getBlockEntity());
			return VanillaPlugin.getElementHelper().item(stack);
		}
		return null;
	}

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
			String name = profile.getName();
			if (name == null || StringUtils.isBlank(name))
				return;
			tooltip.remove(OBJECT_NAME_TAG);
			tooltip.add(0, new TextComponent(String.format(config.getWailaConfig().getFormatting().getBlockName(), I18n.get(Items.PLAYER_HEAD.getDescriptionId() + ".named", name))).withStyle(Waila.CONFIG.get().getOverlay().getColor().getTitle()), OBJECT_NAME_TAG);
		}
	}

}
