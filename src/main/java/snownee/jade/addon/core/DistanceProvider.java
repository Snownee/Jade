package snownee.jade.addon.core;

import java.text.DecimalFormat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum DistanceProvider implements IBlockComponentProvider, IEntityComponentProvider {

	INSTANCE;

	public static DecimalFormat fmt = new DecimalFormat("#.#");

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		append(tooltip, accessor, accessor.getPosition(), config);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		append(tooltip, accessor, accessor.getEntity().blockPosition(), config);
	}

	public void append(ITooltip tooltip, Accessor<?> accessor, BlockPos pos, IPluginConfig config) {
		boolean distance = config.get(Identifiers.CORE_DISTANCE);
		String distanceVal = distance ? distance(accessor) : null;
		String distanceMsg = distance ? I18n.get("narration.jade.distance", distanceVal) : null;
		if (config.get(Identifiers.CORE_COORDINATES)) {
			if (config.get(Identifiers.CORE_REL_COORDINATES) && Screen.hasControlDown()) {
				xyz(tooltip, pos.subtract(new BlockPos(accessor.getPlayer().getEyePosition())));
			} else {
				xyz(tooltip, pos);
			}
			if (distance) {
				tooltip.append(tooltip.getElementHelper().text(new TranslatableComponent("jade.distance1", distanceVal)).message(distanceMsg));
			}
		} else if (distance) {
			tooltip.add(tooltip.getElementHelper().text(new TranslatableComponent("jade.distance2", distanceVal)).message(distanceMsg));
		}
	}

	public static String distance(Accessor<?> accessor) {
		return fmt.format(accessor.getPlayer().getEyePosition().distanceTo(accessor.getHitResult().getLocation()));
	}

	public static void xyz(ITooltip tooltip, Vec3i pos) {
		Component display = new TranslatableComponent("jade.blockpos", display(pos.getX(), 0xef9a9a), display(pos.getY(), 0xa5d6a7), display(pos.getZ(), 0x90caf9));
		String narrate = I18n.get("narration.jade.blockpos", narrate(pos.getX()), narrate(pos.getY()), narrate(pos.getZ()));
		tooltip.add(tooltip.getElementHelper().text(display).message(narrate));
	}

	public static Component display(int i, int color) {
		return new TextComponent(Integer.toString(i)).withStyle($ -> $.withColor(color));
	}

	public static String narrate(int i) {
		return i >= 0 ? Integer.toString(i) : I18n.get("narration.jade.negative", -i);
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
