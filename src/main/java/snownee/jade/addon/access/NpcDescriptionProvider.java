package snownee.jade.addon.access;

import com.google.common.base.Strings;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Mannequin;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public class NpcDescriptionProvider implements IEntityComponentProvider {
	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Mannequin entity = (Mannequin) accessor.getEntity();
		Component description = entity.getDescription();
		if (description == null || Strings.isNullOrEmpty(description.getString())) {
			description = Mannequin.DEFAULT_DESCRIPTION;
		}
		String message = tooltip.getString(JadeIds.CORE_OBJECT_NAME);
		if (!message.isBlank()) {
			tooltip.replace(
					JadeIds.CORE_OBJECT_NAME,
					IThemeHelper.get().title(Component.translatable("jade.access.npc_description", message, description)));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.ACCESS_NPC_DESCRIPTION;
	}
}
