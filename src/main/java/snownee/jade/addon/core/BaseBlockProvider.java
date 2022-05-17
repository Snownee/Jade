package snownee.jade.addon.core;

import java.util.Collection;

import com.google.common.base.Strings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.JadeCommonConfig;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.util.ModIdentification;

public class BaseBlockProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {

	public static final BaseBlockProvider INSTANCE = new BaseBlockProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
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

	@OnlyIn(Dist.CLIENT)
	public void appendHead(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Component name = null;
		if (accessor.getServerData().contains("givenName", Tag.TAG_STRING)) {
			name = Component.Serializer.fromJson(accessor.getServerData().getString("givenName"));
		}
		if (name == null && accessor.isFakeBlock()) {
			name = accessor.getFakeBlock().getHoverName();
		}
		if (name == null && WailaClientRegistration.INSTANCE.shouldPick(accessor.getBlockState())) {
			ItemStack pick = accessor.getPickedResult();
			if (!pick.isEmpty())
				name = pick.getHoverName();
		}
		if (name == null) {
			String key = accessor.getBlock().getDescriptionId();
			if (I18n.exists(key)) {
				name = accessor.getBlock().getName();
			} else {
				ItemStack stack = accessor.getBlockState().getCloneItemStack(accessor.getHitResult(), accessor.getLevel(), accessor.getPosition(), accessor.getPlayer());
				if (stack != null && !stack.isEmpty()) {
					name = stack.getHoverName();
				} else {
					name = new TextComponent(key);
				}
			}
		}
		if (name != null) {
			IWailaConfig wailaConfig = config.getWailaConfig();
			tooltip.add(wailaConfig.getFormatting().title(name), CorePlugin.TAG_OBJECT_NAME);
		}
		if (config.get(CorePlugin.CONFIG_REGISTRY_NAME))
			tooltip.add(new TextComponent(accessor.getBlock().getRegistryName().toString()).withStyle(ChatFormatting.GRAY), CorePlugin.TAG_REGISTRY_NAME);
	}

	@OnlyIn(Dist.CLIENT)
	public void appendBody(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (config.get(CorePlugin.CONFIG_BLOCK_STATES)) {
			BlockState state = accessor.getBlockState();
			Collection<Property<?>> properties = state.getProperties();
			if (properties.isEmpty())
				return;
			IElementHelper helper = tooltip.getElementHelper();
			ITooltip box = helper.tooltip();
			properties.forEach(p -> {
				Comparable<?> value = state.getValue(p);
				MutableComponent valueText = new TextComponent(" " + value.toString()).withStyle();
				if (p instanceof BooleanProperty)
					valueText = valueText.withStyle(value == Boolean.TRUE ? ChatFormatting.GREEN : ChatFormatting.RED);
				box.add(new TextComponent(p.getName() + ":").append(valueText));
			});
			tooltip.add(helper.box(box));
		}
		//		tooltip.add(new TextComponent("TestTest"));
		//		tooltip.append(tooltip.getElementHelper().text(new TextComponent("TestTest")).align(Align.RIGHT));
	}

	@OnlyIn(Dist.CLIENT)
	public void appendTail(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!config.get(CorePlugin.CONFIG_MOD_NAME))
			return;
		String modName = null;
		if (accessor.isFakeBlock()) {
			ItemStack fakeBlock = accessor.getFakeBlock();
			if (fakeBlock.hasTag() && fakeBlock.getTag().contains("id")) {
				ResourceLocation id = ResourceLocation.tryParse(fakeBlock.getTag().getString("id"));
				if (id != null) {
					modName = ModIdentification.getModName(id);
				}
			}
		}
		if (modName == null && WailaClientRegistration.INSTANCE.shouldPick(accessor.getBlockState())) {
			ItemStack pick = accessor.getPickedResult();
			if (!pick.isEmpty())
				modName = ModIdentification.getModName(pick);
		}
		if (modName == null)
			modName = ModIdentification.getModName(accessor.getBlock());

		if (!Strings.isNullOrEmpty(modName)) {
			modName = String.format(config.getWailaConfig().getFormatting().getModName(), modName);
			IElementHelper helper = tooltip.getElementHelper();
			tooltip.add(helper.text(new TextComponent(modName)).tag(CorePlugin.TAG_MOD_NAME));
		}
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity t, boolean showDetails) {
		if (t instanceof Nameable && JadeCommonConfig.shouldShowCustomName(t)) {
			Nameable nameable = (Nameable) t;
			if (nameable.hasCustomName()) {
				data.putString("givenName", Component.Serializer.toJson(nameable.getCustomName()));
			}
		}
	}
}
