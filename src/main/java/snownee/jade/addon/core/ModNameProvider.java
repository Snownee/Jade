package snownee.jade.addon.core;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.util.ModIdentification;

public abstract class ModNameProvider implements IToggleableProvider {

	public static class ForBlock extends ModNameProvider implements IBlockComponentProvider {
		public static final ForBlock INSTANCE = new ForBlock();

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			if (Objects.equal(config.getEnum(JadeIds.CORE_MOD_NAME), Mode.OFF)) {
				return;
			}
			String modName = null;
			if (accessor.isServersideContent()) {
				modName = ModIdentification.getModName(accessor.getServersideRep());
			}
			if (modName == null && WailaCommonRegistration.instance().blockOperations().shouldPick(accessor.getBlockState())) {
				ItemStack pick = accessor.getPickedResult();
				if (!pick.isEmpty()) {
					modName = ModIdentification.getModName(pick);
				}
			}
			if (modName == null) {
				modName = ModIdentification.getModName(accessor.getBlock());
			}

			if (!Strings.isNullOrEmpty(modName)) {
				tooltip.add(IThemeHelper.get().modName(modName));
			}
		}
	}

	public static class ForEntity extends ModNameProvider implements IEntityComponentProvider {
		public static final ForEntity INSTANCE = new ForEntity();

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			if (Objects.equal(config.getEnum(JadeIds.CORE_MOD_NAME), Mode.OFF)) {
				return;
			}
			String modName = null;
			if (accessor.isServersideContent()) {
				modName = ModIdentification.getModName(accessor.getServersideRep());
			}
			if (modName == null) {
				modName = ModIdentification.getModName(accessor.getEntity());
			}
			if (!Strings.isNullOrEmpty(modName)) {
				tooltip.add(IThemeHelper.get().modName(ModIdentification.getModName(accessor.getEntity())));
			}
		}
	}

	@Override
	public Identifier getUid() {
		return JadeIds.CORE_MOD_NAME;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.TAIL - 1;
	}

	public enum Mode {
		ON, OFF, SMALLER
	}
}
