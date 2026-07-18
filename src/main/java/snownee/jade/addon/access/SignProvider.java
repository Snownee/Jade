package snownee.jade.addon.access;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public class SignProvider implements IBlockComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!(accessor.getBlockEntity() instanceof SignBlockEntity be)) {
			return;
		}
		SignTextSlot slot = be.getSlotPlayerIsFacing(accessor.getPlayer());
		tooltip.add(Component.translatable("jade.access.sign." + (slot == SignTextSlot.FRONT ? "front" : "back")));
		int i = 0;
		for (Component message : be.getText(slot).getMessages(true)) {
			++i;
			if (accessor.showDetails()) {
				tooltip.add(Component.translatable("jade.access.sign.line" + i, message));
			} else {
				tooltip.add(message);
			}
			if (i >= 4) {
				break;
			}
		}
	}

	@Override
	public Identifier getUid() {
		return JadeIds.ACCESS_SIGN;
	}
}
