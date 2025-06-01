package snownee.jade.addon.universal;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;

public class EnergyStorageProvider<T extends Accessor<?>> implements StreamServerDataProvider<T, Map.Entry<ResourceLocation, List<ViewGroup<EnergyView.Data>>>> {

	private static final StreamCodec<RegistryFriendlyByteBuf, Map.Entry<ResourceLocation, List<ViewGroup<EnergyView.Data>>>> STREAM_CODEC = ViewGroup.listCodec(
			EnergyView.Data.STREAM_CODEC).cast();

	public static final EnergyStorageProvider<BlockAccessor> BLOCK = new EnergyStorageProvider<>();
	public static final EnergyStorageProvider<EntityAccessor> ENTITY = new EnergyStorageProvider<>();

	public static class Client<T extends Accessor<?>> extends EnergyStorageProvider<T> implements IComponentProvider<T> {
		public static final Client<BlockAccessor> BLOCK = new Client<>();
		public static final Client<EntityAccessor> ENTITY = new Client<>();

		@Override
		public void appendTooltip(ITooltip tooltip, T accessor, IPluginConfig config) {
			if ((!accessor.showDetails() && config.get(JadeIds.UNIVERSAL_ENERGY_STORAGE_DETAILED))) {
				return;
			}

			List<ClientViewGroup<EnergyView>> groups = ClientProxy.mapToClientGroups(
					accessor,
					JadeIds.UNIVERSAL_ENERGY_STORAGE,
					STREAM_CODEC,
					WailaClientRegistration.instance().energyStorageProviders::get,
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
							IWailaConfig.HandlerDisplayStyle style = config.getEnum(JadeIds.UNIVERSAL_ENERGY_STORAGE_STYLE);
							Component text;
							if (view.overrideText != null) {
								text = view.overrideText;
							} else {
								String current = view.current;
								if (style == IWailaConfig.HandlerDisplayStyle.PROGRESS_BAR) {
									current = ChatFormatting.WHITE + current;
								}
								text = Component.translatable("jade.fe", current, view.max);
							}

							switch (style) {
								case PLAIN_TEXT -> theTooltip.add(Component.translatable("jade.energy.text", text));
								case ICON -> {
									theTooltip.add(JadeUI.sprite(JadeIds.JADE("energy"), 10, 10)
											.size(10, 9)
											.offset(0, -1));
									theTooltip.append(text);
								}
								case PROGRESS_BAR -> {
									ProgressView progressView = new ProgressView(
											ProgressView.Part.of(view.ratio),
											text,
											JadeUI.progressStyle(),
											BoxStyle.nestedBox());
									theTooltip.add(JadeUI.progress(progressView));
								}
							}
						}
					});
		}
	}

	@Override
	public @Nullable Map.Entry<ResourceLocation, List<ViewGroup<EnergyView.Data>>> streamData(T accessor) {
		return CommonProxy.getServerExtensionData(accessor, WailaCommonRegistration.instance().energyStorageProviders);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Map.Entry<ResourceLocation, List<ViewGroup<EnergyView.Data>>>> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.UNIVERSAL_ENERGY_STORAGE;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.BODY + 1000;
	}

	@Override
	public boolean shouldRequestData(T accessor) {
		if (!accessor.showDetails() && IWailaConfig.get().plugin().get(JadeIds.UNIVERSAL_ENERGY_STORAGE_DETAILED)) {
			return false;
		}
		return WailaCommonRegistration.instance().energyStorageProviders.hitsAny(accessor, IServerExtensionProvider::shouldRequestData);
	}

	public static class Extension implements IServerExtensionProvider<EnergyView.Data>, IClientExtensionProvider<EnergyView.Data, EnergyView> {
		public static final Extension INSTANCE = new Extension();

		@Override
		public ResourceLocation getUid() {
			return JadeIds.UNIVERSAL_ENERGY_STORAGE_DEFAULT;
		}

		@Override
		public List<ClientViewGroup<EnergyView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<EnergyView.Data>> groups) {
			return groups.stream().map($ -> {
				String unit = $.getExtraData().getStringOr("Unit", CommonProxy.defaultEnergyUnit());
				return new ClientViewGroup<>($.views.stream().map(data -> EnergyView.read(data, unit)).filter(Objects::nonNull).toList());
			}).toList();
		}

		@Nullable
		@Override
		public List<ViewGroup<EnergyView.Data>> getGroups(Accessor<?> accessor) {
			return CommonProxy.wrapEnergyStorage(accessor);
		}

		@Override
		public boolean shouldRequestData(Accessor<?> accessor) {
			return CommonProxy.hasDefaultEnergyStorage(accessor);
		}

		@Override
		public int getDefaultPriority() {
			return 9999;
		}
	}

}
