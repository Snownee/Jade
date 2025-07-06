package snownee.jade.addon.universal;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snownee.jade.JadeClient;
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
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.NarratableComponent;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;

public class FluidStorageProvider<T extends Accessor<?>> implements StreamServerDataProvider<T, Map.Entry<ResourceLocation, List<ViewGroup<FluidView.Data>>>> {

	private static final StreamCodec<RegistryFriendlyByteBuf, Map.Entry<ResourceLocation, List<ViewGroup<FluidView.Data>>>> STREAM_CODEC = ViewGroup.listCodec(
			FluidView.Data.STREAM_CODEC);

	public static final FluidStorageProvider<BlockAccessor> BLOCK = new FluidStorageProvider<>();
	public static final FluidStorageProvider<EntityAccessor> ENTITY = new FluidStorageProvider<>();

	public static class Client<T extends Accessor<?>> extends FluidStorageProvider<T> implements IComponentProvider<T> {
		public static final Client<BlockAccessor> BLOCK = new Client<>();
		public static final Client<EntityAccessor> ENTITY = new Client<>();

		@Override
		public void appendTooltip(ITooltip tooltip, T accessor, IPluginConfig config) {
			if (!accessor.showDetails() && config.get(JadeIds.UNIVERSAL_FLUID_STORAGE_DETAILED)) {
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

			boolean renderGroup = groups.size() > 1 || groups.getFirst().shouldRenderGroup();
			ClientViewGroup.tooltip(
					tooltip, groups, renderGroup, (theTooltip, group) -> {
						if (renderGroup) {
							group.renderHeader(theTooltip);
						}
						for (var view : group.views) {
							Component text;
							IWailaConfig.HandlerDisplayStyle style = config.getEnum(JadeIds.UNIVERSAL_FLUID_STORAGE_STYLE);

							if (view.overrideText != null) {
								text = view.overrideText;
							} else if (view.fluidName == null) {
								// when do we reach here?
								text = NarratableComponent.attach(IThemeHelper.get().info(view.current), view.current);
							} else {
								Component fluidAmount;
								Component fluidName = IThemeHelper.get().info(IDisplayHelper.get().stripColor(view.fluidName));
								if (accessor.showDetails() || style != IWailaConfig.HandlerDisplayStyle.PROGRESS_BAR) {
									fluidAmount = new NarratableComponent(
											Component.translatable(
													"jade.fluid.with_capacity",
													IThemeHelper.get().info(view.current),
													view.max), () -> JadeClient.formatString(
											"narration.jade.withCapacity",
											NarratableComponent.getNarration(view.current),
											NarratableComponent.getNarration(view.max)));
								} else {
									fluidAmount = NarratableComponent.attach(IThemeHelper.get().info(view.current), view.current);
								}
								String key = style == IWailaConfig.HandlerDisplayStyle.PLAIN_TEXT ? "jade.fluid.text" : "jade.fluid";
								text = NarratableComponent.translatable(key, fluidName, fluidAmount);
							}

							switch (style) {
								case PLAIN_TEXT -> theTooltip.add(text);
								case ICON -> {
									theTooltip.add(JadeUI.smallItem(new ItemStack(Items.BUCKET)));
									theTooltip.append(text);
								}
								case PROGRESS_BAR -> {
									ProgressView progressView = new ProgressView(
											ProgressView.Part.of(view.ratio, view.overlay),
											text,
											JadeUI.progressStyle().canDecrease(true),
											BoxStyle.nestedBox());
									theTooltip.add(JadeUI.progress(progressView));
								}
							}
						}
						if (group.extraData != null) {
							int extra = group.extraData.getIntOr("+", 0);
							if (extra > 0) {
								theTooltip.add(Component.translatable("jade.fluid.more_tanks", extra));
							}
						}
					});
		}
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

	public static class Extension implements IServerExtensionProvider<FluidView.Data>, IClientExtensionProvider<FluidView.Data, FluidView> {
		public static final Extension INSTANCE = new Extension();

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
