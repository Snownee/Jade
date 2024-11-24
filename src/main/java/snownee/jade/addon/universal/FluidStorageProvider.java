package snownee.jade.addon.universal;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
import snownee.jade.api.JadeIds;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.DisplayStyle;
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
import snownee.jade.util.WailaExceptionHandler;

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
		if ((!accessor.showDetails() && config.get(JadeIds.UNIVERSAL_FLUID_STORAGE_DETAILED))) {
			return;
		}

		if (!accessor.getServerData().contains("JadeFluidStorage")) {
			return;
		}

		var provider = Optional.ofNullable(ResourceLocation.tryParse(accessor.getServerData().getString("JadeFluidStorageUid"))).map(
				WailaClientRegistration.instance().fluidStorageProviders::get).orElse(null);
		if (provider == null) {
			return;
		}

		List<ClientViewGroup<FluidView>> groups;
		try {
			groups = provider.getClientGroups(
					accessor,
					ViewGroup.readList(accessor.getServerData(), "JadeFluidStorage", Function.identity()));
		} catch (Exception e) {
			WailaExceptionHandler.handleErr(e, provider, tooltip::add);
			return;
		}

		if (groups.isEmpty()) {
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
				DisplayStyle style = config.getEnum(JadeIds.UNIVERSAL_FLUID_STORAGE_STYLE);

				if (view.overrideText != null) {
					text = view.overrideText;
				} else if (view.fluidName == null) {
					text = Component.literal(view.current);
				} else if (accessor.showDetails() || style != DisplayStyle.PROGRESSBAR) {
					text = Component.translatable(
							"jade.fluid2",
							IDisplayHelper.get().stripColor(view.fluidName).withStyle(ChatFormatting.WHITE),
							Component.literal(view.current).withStyle(ChatFormatting.WHITE),
							view.max).withStyle(ChatFormatting.GRAY);
				} else {
					text = Component.translatable("jade.fluid", IDisplayHelper.get().stripColor(view.fluidName), view.current);
				}

				switch(style) {
					case TEXT -> theTooltip.add(Component.translatable("jade.fluid.text").append(text));
					case SYMBOL -> {
						ResourceLocation location = JadeIds.JADE("fluid");
						theTooltip.add(helper.smallItem(new ItemStack(Items.BUCKET)));
						theTooltip.append(text);
					}
					default -> {
						ProgressStyle progressStyle = helper.progressStyle().overlay(view.overlay);
						theTooltip.add(helper.progress(view.ratio, text, progressStyle, BoxStyle.getNestedBox(), true));
					}
				}
			}
		});
	}

	public static void putData(Accessor<?> accessor) {
		Map.Entry<ResourceLocation, List<ViewGroup<CompoundTag>>> entry = CommonProxy.getServerExtensionData(
				accessor,
				WailaCommonRegistration.instance().fluidStorageProviders);
		if (entry == null) {
			return;
		}
		CompoundTag tag = accessor.getServerData();
		ViewGroup.saveList(tag, "JadeFluidStorage", entry.getValue(), Function.identity());
		tag.putString("JadeFluidStorageUid", entry.getKey().toString());
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
		append(tooltip, accessor, config);
	}

	@Override
	public void appendServerData(CompoundTag data, T accessor) {
		putData(accessor);
	}

	@Override
	public boolean shouldRequestData(T accessor) {
		if (!accessor.showDetails() && IWailaConfig.get().getPlugin().get(JadeIds.UNIVERSAL_FLUID_STORAGE_DETAILED)) {
			return false;
		}
		return WailaCommonRegistration.instance().fluidStorageProviders.hitsAny(accessor, IServerExtensionProvider::shouldRequestData);
	}

	public enum Extension implements IServerExtensionProvider<CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {
		INSTANCE;

		@Override
		public ResourceLocation getUid() {
			return JadeIds.UNIVERSAL_FLUID_STORAGE_DEFAULT;
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
			return 9999;
		}
	}

}
