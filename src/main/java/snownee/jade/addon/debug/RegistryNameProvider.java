package snownee.jade.addon.debug;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.level.material.FluidState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.ModIdentification;

public abstract class RegistryNameProvider implements IToggleableProvider {

	public static ForBlock getBlock() {
		return ForBlock.INSTANCE;
	}

	public static ForEntity getEntity() {
		return ForEntity.INSTANCE;
	}

	public static class ForBlock extends RegistryNameProvider implements IBlockComponentProvider {
		private static final ForBlock INSTANCE = new ForBlock();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			Identifier id = CommonProxy.getId(accessor.getBlock());
			if (accessor.isServersideContent()) {
				id = ModIdentification.getSpecialId(accessor.getServersideRep()).orElse(id);
			}
			if (append(tooltip, id, config) && config.get(JadeIds.DEBUG_SPECIAL_REGISTRY_NAME)) {
				if (accessor.getBlockEntity() != null) {
					id = CommonProxy.getId(accessor.getBlockEntity().getType());
					String s = I18n.get("config.jade.plugin_jade.registry_name.special.block_entity_type", id);
					tooltip.add(IWailaConfig.get().formatting().registryName(s), JadeIds.DEBUG_SPECIAL_REGISTRY_NAME);
				}
				FluidState fluidState = accessor.getBlockState().getFluidState();
				if (!fluidState.isEmpty()) {
					id = BuiltInRegistries.FLUID.getKey(fluidState.getType());
					String s = I18n.get("config.jade.plugin_jade.registry_name.special.fluid", id);
					tooltip.add(IWailaConfig.get().formatting().registryName(s), JadeIds.DEBUG_SPECIAL_REGISTRY_NAME);
				}
				Optional<Holder<PoiType>> poiTypeHolder = PoiTypes.forState(accessor.getBlockState());
				if (poiTypeHolder.isPresent()) {
					id = poiTypeHolder.get().unwrapKey().orElseThrow().identifier();
					String s = I18n.get("config.jade.plugin_jade.registry_name.special.poi", id);
					tooltip.add(IWailaConfig.get().formatting().registryName(s), JadeIds.DEBUG_SPECIAL_REGISTRY_NAME);
				}
			}
		}
	}

	public static class ForEntity extends RegistryNameProvider implements IEntityComponentProvider {
		private static final ForEntity INSTANCE = new ForEntity();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			Identifier id = CommonProxy.getId(accessor.getEntity().getType());
			if (accessor.isServersideContent()) {
				id = ModIdentification.getSpecialId(accessor.getServersideRep()).orElse(id);
			}
			if (append(tooltip, id, config) && config.get(JadeIds.DEBUG_SPECIAL_REGISTRY_NAME)) {
				if (accessor.getEntity() instanceof Painting painting) {
					id = painting.getVariant().unwrapKey().orElseThrow().identifier();
					String s = I18n.get("config.jade.plugin_jade.registry_name.special.painting", id);
					tooltip.add(IWailaConfig.get().formatting().registryName(s), JadeIds.DEBUG_SPECIAL_REGISTRY_NAME);
				}
			}
		}
	}

	public boolean append(ITooltip tooltip, Identifier id, IPluginConfig config) {
		Mode mode = config.getEnum(JadeIds.DEBUG_REGISTRY_NAME);
		if (mode == Mode.OFF) {
			return false;
		}
		if (mode == Mode.ADVANCED_TOOLTIPS && !Minecraft.getInstance().options.advancedItemTooltips) {
			return false;
		}
		tooltip.add(IWailaConfig.get().formatting().registryName(id.toString()));
		return true;
	}

	@Override
	public Identifier getUid() {
		return JadeIds.DEBUG_REGISTRY_NAME;
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
