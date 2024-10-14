package snownee.jade.addon.universal;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;

public abstract class FluidStorageProvider<T extends Accessor<?>> implements IComponentProvider<T>, StreamServerDataProvider<T, Map.Entry<ResourceLocation, List<ViewGroup<FluidView.Data>>>> {

	private static final StreamCodec<RegistryFriendlyByteBuf, Map.Entry<ResourceLocation, List<ViewGroup<FluidView.Data>>>> STREAM_CODEC = ViewGroup.listCodec(
			FluidView.Data.STREAM_CODEC);

	public static ForBlock getBlock() {
		return ForBlock.INSTANCE;
	}

	public static ForEntity getEntity() {
		return ForEntity.INSTANCE;
	}

	public static class ForBlock extends FluidStorageProvider<BlockAccessor> {
		private static final ForBlock INSTANCE = new ForBlock();
	}

	public static class ForEntity extends FluidStorageProvider<EntityAccessor> {
		private static final ForEntity INSTANCE = new ForEntity();
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.UNIVERSAL_FLUID_STORAGE;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.BODY + 1000;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, T accessor, IPluginConfig config) {
		if ((!accessor.showDetails() && config.get(JadeIds.UNIVERSAL_FLUID_STORAGE_DETAILED))) {
			return;
		}

		List<ClientViewGroup<FluidView>> groups = ClientProxy.mapToClientGroups(
				accessor,
				JadeIds.UNIVERSAL_FLUID_STORAGE,
				STREAM_CODEC,
				WailaClientRegistration.instance().fluidStorageProviders::get,
				tooltip);
		if (groups == null || groups.isEmpty()) {
			return;
		}

		IElementHelper helper = IElementHelper.get();
		boolean renderGroup = groups.size() > 1 || groups.getFirst().shouldRenderGroup();
		ClientViewGroup.tooltip(tooltip, groups, renderGroup, (theTooltip, group) -> {
			if (renderGroup) {
				group.renderHeader(theTooltip);
			}
			for (var view : group.views) {
				Component text;
				if (view.overrideText != null) {
					text = view.overrideText;
				} else if (view.fluidName == null) {
					text = Component.literal(view.current);
				} else if (accessor.showDetails()) {
					text = Component.translatable(
							"jade.fluid2",
							IDisplayHelper.get().stripColor(view.fluidName).withStyle(ChatFormatting.WHITE),
							Component.literal(view.current).withStyle(ChatFormatting.WHITE),
							view.max).withStyle(ChatFormatting.GRAY);
				} else {
					text = Component.translatable("jade.fluid", IDisplayHelper.get().stripColor(view.fluidName), view.current);
				}
				ProgressStyle progressStyle = helper.progressStyle().overlay(view.overlay);
				theTooltip.add(helper.progress(view.ratio, text, progressStyle, BoxStyle.getNestedBox(), true));
			}
		});
	}

	@Override
	public @Nullable Map.Entry<ResourceLocation, List<ViewGroup<FluidView.Data>>> streamData(T accessor) {
		return CommonProxy.getServerExtensionData(accessor, WailaCommonRegistration.instance().fluidStorageProviders);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Map.Entry<ResourceLocation, List<ViewGroup<FluidView.Data>>>> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public boolean shouldRequestData(T accessor) {
		if (!accessor.showDetails() && IWailaConfig.get().plugin().get(JadeIds.UNIVERSAL_FLUID_STORAGE_DETAILED)) {
			return false;
		}
		return WailaCommonRegistration.instance().fluidStorageProviders.hitsAny(accessor, IServerExtensionProvider::shouldRequestData);
	}

	public enum Extension implements IServerExtensionProvider<FluidView.Data>, IClientExtensionProvider<FluidView.Data, FluidView> {
		INSTANCE;

		@Override
		public ResourceLocation getUid() {
			return JadeIds.UNIVERSAL_FLUID_STORAGE_DEFAULT;
		}

		@Override
		public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<FluidView.Data>> groups) {
			return ClientViewGroup.map(groups, FluidView::readDefault, null);
		}

		@Nullable
		@Override
		public List<ViewGroup<FluidView.Data>> getGroups(Accessor<?> accessor) {
			return CommonProxy.wrapFluidStorage(accessor);
		}

		@Override
		public boolean shouldRequestData(Accessor<?> accessor) {
			return CommonProxy.hasDefaultFluidStorage(accessor);
		}

		@Override
		public int getDefaultPriority() {
			return 9999;
		}
	}

}
