package snownee.jade.addon.universal;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ScreenDirection;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;

public abstract class ProgressProvider<T extends Accessor<?>> implements IComponentProvider<T>, StreamServerDataProvider<T, Map.Entry<ResourceLocation, List<ViewGroup<ProgressView.Data>>>> {

	private static final StreamCodec<RegistryFriendlyByteBuf, Map.Entry<ResourceLocation, List<ViewGroup<ProgressView.Data>>>> STREAM_CODEC = ViewGroup.listCodec(
			ProgressView.Data.STREAM_CODEC).cast();

	public static ForBlock getBlock() {
		return ForBlock.INSTANCE;
	}

	public static ForEntity getEntity() {
		return ForEntity.INSTANCE;
	}

	public static class ForBlock extends ProgressProvider<BlockAccessor> {
		private static final ForBlock INSTANCE = new ForBlock();
	}

	public static class ForEntity extends ProgressProvider<EntityAccessor> {
		private static final ForEntity INSTANCE = new ForEntity();
	}

	@Override
	public void appendTooltip(ITooltip tooltip, T accessor, IPluginConfig config) {
		List<ClientViewGroup<ProgressView>> groups = ClientProxy.mapToClientGroups(
				accessor,
				JadeIds.UNIVERSAL_PROGRESS,
				STREAM_CODEC,
				WailaClientRegistration.instance().progressProviders::get,
				tooltip);
		if (groups == null || groups.isEmpty()) {
			return;
		}

		IElementHelper helper = IElementHelper.get();
		boolean renderGroup = groups.size() > 1 || groups.getFirst().shouldRenderGroup();
		BoxStyle.GradientBorder boxStyle = BoxStyle.getTransparent().clone();
		boxStyle.bgColor = 0x44FFFFFF;
		ClientViewGroup.tooltip(tooltip, groups, renderGroup, (theTooltip, group) -> {
			if (renderGroup) {
				group.renderHeader(theTooltip);
			}
			for (var view : group.views) {
				if (view.text != null) {
					theTooltip.add(helper.text(view.text).scale(0.75F));
					theTooltip.setLineMargin(-1, ScreenDirection.DOWN, 0);
				}
				theTooltip.add(helper.progress(view.progress, null, view.style, boxStyle, false).size(new Vec2(10, 2)));
			}
		});
	}

	@Override
	public @Nullable Map.Entry<ResourceLocation, List<ViewGroup<ProgressView.Data>>> streamData(T accessor) {
		return CommonProxy.getServerExtensionData(accessor, WailaCommonRegistration.instance().progressProviders);
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, Map.Entry<ResourceLocation, List<ViewGroup<ProgressView.Data>>>> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public boolean shouldRequestData(T accessor) {
		return WailaCommonRegistration.instance().progressProviders.hitsAny(accessor, IServerExtensionProvider::shouldRequestData);
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.UNIVERSAL_PROGRESS;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.BODY + 1000;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

}
