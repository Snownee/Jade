package snownee.jade.addon.core;

import java.text.DecimalFormat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.theme.ThemeHelper;

public abstract class DistanceProvider implements IToggleableProvider {

	public static ForBlock getBlock() {
		return ForBlock.INSTANCE;
	}

	public static ForEntity getEntity() {
		return ForEntity.INSTANCE;
	}

	public static class ForBlock extends DistanceProvider implements IBlockComponentProvider {
		private static final ForBlock INSTANCE = new ForBlock();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			append(tooltip, accessor, accessor.getPosition(), config);
		}
	}

	public static class ForEntity extends DistanceProvider implements IEntityComponentProvider {
		private static final ForEntity INSTANCE = new ForEntity();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			append(tooltip, accessor, accessor.getEntity().blockPosition(), config);
		}
	}

	public static final DecimalFormat fmt = new DecimalFormat("#.#");
	private static final int[] colors = {0xef9a9a, 0xa5d6a7, 0x90caf9, 0xb02a37, 0x198754, 0x0a58ca};

	public static String distance(Accessor<?> accessor) {
		return fmt.format(accessor.getPlayer().getEyePosition().distanceTo(accessor.getHitResult().getLocation()));
	}

	public static void xyz(ITooltip tooltip, Vec3i pos) {
		Component display = Component.translatable("jade.blockpos", display(pos.getX(), 0), display(pos.getY(), 1), display(pos.getZ(), 2));
		String narrate = I18n.get("narration.jade.blockpos", narrate(pos.getX()), narrate(pos.getY()), narrate(pos.getZ()));
		tooltip.add(IElementHelper.get().text(display).message(narrate));
	}

	public static Component display(int i, int colorIndex) {
		if (IThemeHelper.get().isLightColorScheme()) {
			colorIndex += 3;
		}
		return Component.literal(Integer.toString(i)).withStyle(ThemeHelper.colorStyle(colors[colorIndex]));
	}

	public static String narrate(int i) {
		return i >= 0 ? Integer.toString(i) : I18n.get("narration.jade.negative", -i);
	}

	public void append(ITooltip tooltip, Accessor<?> accessor, BlockPos pos, IPluginConfig config) {
		boolean distance = config.get(Identifiers.CORE_DISTANCE);
		String distanceVal = distance ? distance(accessor) : null;
		String distanceMsg = distance ? I18n.get("narration.jade.distance", distanceVal) : null;
		if (config.get(Identifiers.CORE_COORDINATES)) {
			if (config.get(Identifiers.CORE_REL_COORDINATES) && Screen.hasControlDown()) {
				xyz(tooltip, pos.subtract(BlockPos.containing(accessor.getPlayer().getEyePosition())));
			} else {
				xyz(tooltip, pos);
			}
			if (distance) {
				tooltip.append(IElementHelper.get().text(Component.translatable("jade.distance1", distanceVal)).message(distanceMsg));
			}
		} else if (distance) {
			tooltip.add(IElementHelper.get().text(Component.translatable("jade.distance2", distanceVal)).message(distanceMsg));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.CORE_DISTANCE;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return -4600;
	}

}
