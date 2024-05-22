package snownee.jade.addon.universal;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
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
import snownee.jade.util.CommonProxy;

public abstract class FluidStorageProvider<T extends Accessor<?>> implements IComponentProvider<T>, IServerDataProvider<T> {

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

	public static void append(ITooltip tooltip, Accessor<?> accessor, IPluginConfig config) {
		if ((!accessor.showDetails() && config.get(Identifiers.UNIVERSAL_FLUID_STORAGE_DETAILED))) {
			return;
		}
		if (accessor.getServerData().contains("JadeFluidStorage")) {
			var provider = Optional.ofNullable(ResourceLocation.tryParse(accessor.getServerData().getString("JadeFluidStorageUid"))).map(
					WailaClientRegistration.instance().fluidStorageProviders::get);
			if (provider.isPresent()) {
				var groups = provider.get().getClientGroups(
						accessor,
						ViewGroup.readList(accessor.getServerData(), "JadeFluidStorage", Function.identity()));
				if (groups.isEmpty()) {
					return;
				}

				IElementHelper helper = IElementHelper.get();
				boolean renderGroup = groups.size() > 1 || groups.get(0).shouldRenderGroup();
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
		}
	}

	public static void putData(Accessor<?> accessor) {
		CompoundTag tag = accessor.getServerData();
		Object target = accessor.getTarget();
		for (var provider : WailaCommonRegistration.instance().fluidStorageProviders.get(target)) {
			var groups = provider.getGroups(accessor);
			if (groups != null) {
				if (ViewGroup.saveList(tag, "JadeFluidStorage", groups, Function.identity())) {
					tag.putString("JadeFluidStorageUid", provider.getUid().toString());
				}
				return;
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.UNIVERSAL_FLUID_STORAGE;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.BODY + 1000;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, T accessor, IPluginConfig config) {
		append(tooltip, accessor, config);
	}

	@Override
	public void appendServerData(CompoundTag data, T accessor) {
		putData(accessor);
	}

	@Override
	public boolean shouldRequestData(T accessor) {
		if (!accessor.showDetails() && IWailaConfig.get().getPlugin().get(Identifiers.UNIVERSAL_FLUID_STORAGE_DETAILED)) {
			return false;
		}
		for (var provider : WailaCommonRegistration.instance().fluidStorageProviders.get(accessor)) {
			if (provider.shouldRequestData(accessor)) {
				return true;
			}
		}
		return false;
	}

	public enum Extension implements IServerExtensionProvider<CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {
		INSTANCE;

		@Override
		public ResourceLocation getUid() {
			return Identifiers.UNIVERSAL_FLUID_STORAGE;
		}

		@Override
		public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
			return ClientViewGroup.map(groups, FluidView::readDefault, null);
		}

		@Nullable
		@Override
		public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
			return CommonProxy.wrapFluidStorage(accessor);
		}

		@Override
		public boolean shouldRequestData(Accessor<?> accessor) {
			return CommonProxy.hasDefaultFluidStorage(accessor);
		}

		@Override
		public int getDefaultPriority() {
			return TooltipPosition.TAIL;
		}
	}

}
