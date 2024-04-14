package snownee.jade.addon.universal;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.ScreenDirection;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;

public abstract class ProgressProvider<T extends Accessor<?>> implements IComponentProvider<T>, IServerDataProvider<T> {

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

	public static void append(ITooltip tooltip, Accessor<?> accessor, IPluginConfig config) {
		if (accessor.getServerData().contains("JadeProgress")) {
			var provider = Optional.ofNullable(ResourceLocation.tryParse(accessor.getServerData().getString("JadeProgressUid"))).map(
					WailaClientRegistration.instance().progressProviders::get);
			if (provider.isPresent()) {
				var groups = provider.get().getClientGroups(
						accessor,
						ViewGroup.readList(accessor.getServerData(), "JadeProgress", Function.identity()));
				if (groups.isEmpty()) {
					return;
				}

				IElementHelper helper = IElementHelper.get();
				boolean renderGroup = groups.size() > 1 || groups.get(0).shouldRenderGroup();
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
		}
	}

	public static void putData(Accessor<?> accessor) {
		CompoundTag tag = accessor.getServerData();
		Object target = accessor.getTarget();
		for (var provider : WailaCommonRegistration.instance().progressProviders.get(target)) {
			var groups = provider.getGroups(accessor);
			if (groups != null) {
				if (ViewGroup.saveList(tag, "JadeProgress", groups, Function.identity())) {
					tag.putString("JadeProgressUid", provider.getUid().toString());
				}
				return;
			}
		}
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
		for (var provider : WailaCommonRegistration.instance().progressProviders.get(accessor)) {
			if (provider.shouldRequestData(accessor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.UNIVERSAL_PROGRESS;
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
