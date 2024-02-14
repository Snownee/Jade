package snownee.jade.addon.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FluidState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.util.CommonProxy;

public enum RegistryNameProvider implements IBlockComponentProvider, IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (append(tooltip, CommonProxy.getId(accessor.getBlock()).toString(), config) && config.get(Identifiers.DEBUG_SPECIAL_REGISTRY_NAME)) {
			if (accessor.getBlockEntity() != null) {
				ResourceLocation id = CommonProxy.getId(accessor.getBlockEntity().getType());
				String s = I18n.get("config.jade.plugin_jade.registry_name.special.block_entity_type", id);
				tooltip.add(IWailaConfig.get().getFormatting().registryName(s), Identifiers.DEBUG_SPECIAL_REGISTRY_NAME);
			}
			FluidState fluidState = accessor.getBlockState().getFluidState();
			if (!fluidState.isEmpty()) {
				ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluidState.getType());
				String s = I18n.get("config.jade.plugin_jade.registry_name.special.fluid", id);
				tooltip.add(IWailaConfig.get().getFormatting().registryName(s), Identifiers.DEBUG_SPECIAL_REGISTRY_NAME);
			}
		}
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		append(tooltip, CommonProxy.getId(accessor.getEntity().getType()).toString(), config);
	}

	public boolean append(ITooltip tooltip, String id, IPluginConfig config) {
		Mode mode = config.getEnum(Identifiers.DEBUG_REGISTRY_NAME);
		if (mode == Mode.OFF)
			return false;
		if (mode == Mode.ADVANCED_TOOLTIPS && !Minecraft.getInstance().options.advancedItemTooltips)
			return false;
		tooltip.add(IWailaConfig.get().getFormatting().registryName(id));
		return true;
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.DEBUG_REGISTRY_NAME;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.HEAD + 100;
	}

	public enum Mode {
		ON, OFF, ADVANCED_TOOLTIPS
	}

}
