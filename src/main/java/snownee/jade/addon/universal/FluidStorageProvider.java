package snownee.jade.addon.universal;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.ui.HorizontalLineElement;
import snownee.jade.impl.ui.ScaledTextElement;
import snownee.jade.util.PlatformProxy;

public enum FluidStorageProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity>,
		IServerExtensionProvider<Object, CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		append(tooltip, accessor, config);
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity tile, boolean showDetails) {
		putData(data, player, tile, showDetails);
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.UNIVERSAL_FLUID_STORAGE;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.BODY + 1000;
	}

	public static void append(ITooltip tooltip, Accessor<?> accessor, IPluginConfig config) {
		if (accessor.getServerData().contains("JadeFluidStorage")) {
			var provider = Optional.ofNullable(ResourceLocation.tryParse(accessor.getServerData().getString("JadeFluidStorageUid"))).map(WailaClientRegistration.INSTANCE.fluidStorageProviders::get);
			if (provider.isPresent()) {
				var groups = provider.get().getClientGroups(accessor, ViewGroup.readList(accessor.getServerData(), "JadeFluidStorage", Function.identity()));
				if (groups.isEmpty()) {
					return;
				}

				IElementHelper helper = IElementHelper.get();
				boolean renderGroup = groups.size() > 1 || groups.get(0).shouldRenderGroup();
				ClientViewGroup.tooltip(tooltip, groups, renderGroup, (theTooltip, group) -> {
					if (renderGroup) {
						if (group.title != null) {
							theTooltip.add(new HorizontalLineElement());
							theTooltip.append(new ScaledTextElement(group.title, 0.5F));
							theTooltip.append(new HorizontalLineElement());
						} else if (group.bgColor == 0) {
							theTooltip.add(new HorizontalLineElement());
						}
					}
					for (var view : group.views) {
						Component text;
						if (view.overrideText != null) {
							text = view.overrideText;
						} else if (view.fluidName == null) {
							text = Component.literal(view.current);
						} else {
							text = Component.translatable("jade.fluid", IDisplayHelper.get().stripColor(view.fluidName), view.current);
						}
						IProgressStyle progressStyle = helper.progressStyle().overlay(view.overlay);
						theTooltip.add(helper.progress(view.ratio, text, progressStyle, BoxStyle.DEFAULT, true));
					}
				});
			}
		}
	}

	public static void putData(CompoundTag tag, ServerPlayer player, Object target, boolean showDetails) {
		var list = WailaCommonRegistration.INSTANCE.fluidStorageProviders.get(target);
		for (var provider : list) {
			var groups = provider.getGroups(player, player.getLevel(), target, showDetails);
			if (groups != null) {
				if (ViewGroup.saveList(tag, "JadeFluidStorage", groups, Function.identity()))
					tag.putString("JadeFluidStorageUid", provider.getUid().toString());
				return;
			}
		}
	}

	@Override
	public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
		return ClientViewGroup.map(groups, FluidView::read, null);
	}

	@Override
	public List<ViewGroup<CompoundTag>> getGroups(ServerPlayer player, ServerLevel world, Object target, boolean showDetails) {
		return PlatformProxy.wrapFluidStorage(target, player);
	}

}
