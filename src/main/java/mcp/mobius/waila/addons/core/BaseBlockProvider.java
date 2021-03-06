package mcp.mobius.waila.addons.core;

import java.util.Collection;

import com.google.common.base.Strings;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.impl.WailaRegistrar;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import snownee.jade.JadeCommonConfig;

public class BaseBlockProvider implements IComponentProvider, IServerDataProvider<TileEntity> {

	public static final BaseBlockProvider INSTANCE = new BaseBlockProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		TooltipPosition position = accessor.getTooltipPosition();
		if (position == TooltipPosition.HEAD) {
			appendHead(tooltip, accessor, config);
		} else if (position == TooltipPosition.BODY) {
			appendBody(tooltip, accessor, config);
		} else if (position == TooltipPosition.TAIL) {
			appendTail(tooltip, accessor, config);
		}
	}

	public void appendHead(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		String name = null;
		if (accessor.getServerData().contains("givenName", Constants.NBT.TAG_STRING)) {
			ITextComponent component = ITextComponent.Serializer.getComponentFromJson(accessor.getServerData().getString("givenName"));
			name = component.getString();
		} else {
			if (WailaRegistrar.INSTANCE.shouldPick(accessor.getBlockState())) {
				ItemStack pick = accessor.getPickedResult();
				if (!pick.isEmpty())
					name = pick.getDisplayName().getString();
			}
			if (name == null) {
				String key = accessor.getBlock().getTranslationKey();
				if (I18n.hasKey(key)) {
					name = I18n.format(key);
				} else {
					ItemStack stack = accessor.getBlockState().getPickBlock(accessor.getHitResult(), accessor.getWorld(), accessor.getPosition(), accessor.getPlayer());
					if (stack != null && !stack.isEmpty()) {
						name = stack.getDisplayName().getString();
					} else {
						name = key;
					}
				}
			}
		}
		if (name != null)
			tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getBlockName(), name)), CorePlugin.TAG_OBJECT_NAME);
		if (config.get(CorePlugin.CONFIG_REGISTRY_NAME))
			tooltip.add(new StringTextComponent(accessor.getBlock().getRegistryName().toString()).mergeStyle(TextFormatting.GRAY), CorePlugin.TAG_REGISTRY_NAME);
	}

	public void appendBody(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (config.get(CorePlugin.CONFIG_BLOCK_STATES)) {
			BlockState state = accessor.getBlockState();
			Collection<Property<?>> properties = state.getProperties();
			if (properties.isEmpty())
				return;
			IElementHelper helper = tooltip.getElementHelper();
			ITooltip box = helper.tooltip();
			properties.forEach(p -> {
				Comparable<?> value = state.get(p);
				ITextComponent valueText = new StringTextComponent(" " + value.toString()).mergeStyle(p instanceof BooleanProperty ? value == Boolean.TRUE ? TextFormatting.GREEN : TextFormatting.RED : TextFormatting.WHITE);
				box.add(new StringTextComponent(p.getName() + ":").appendSibling(valueText));
			});
			tooltip.add(helper.box(box));
		}
	}

	public void appendTail(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(CorePlugin.CONFIG_MOD_NAME))
			return;
		String modName = null;
		if (WailaRegistrar.INSTANCE.shouldPick(accessor.getBlockState())) {
			ItemStack pick = accessor.getPickedResult();
			if (!pick.isEmpty())
				modName = ModIdentification.getModName(pick);
		}
		if (modName == null)
			modName = ModIdentification.getModName(accessor.getBlock());

		if (!Strings.isNullOrEmpty(modName)) {
			modName = String.format(Waila.CONFIG.get().getFormatting().getModName(), modName);
			IElementHelper helper = tooltip.getElementHelper();
			tooltip.add(helper.text(new StringTextComponent(modName)).tag(CorePlugin.TAG_MOD_NAME));
		}
	}

	@Override
	public void appendServerData(CompoundNBT data, ServerPlayerEntity player, World world, TileEntity t) {
		if (t instanceof INameable && JadeCommonConfig.shouldShowCustomName(t)) {
			INameable nameable = (INameable) t;
			if (nameable.hasCustomName()) {
				data.putString("givenName", ITextComponent.Serializer.toJson(nameable.getCustomName()));
			}
		}
	}
}
