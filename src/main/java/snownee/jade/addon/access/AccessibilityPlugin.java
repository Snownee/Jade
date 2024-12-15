package snownee.jade.addon.access;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import snownee.jade.JadeClient;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.util.JadeLanguages;

@WailaPlugin
public class AccessibilityPlugin implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(new SignProvider(), SignBlock.class);
		registration.markAsClientFeature(JadeIds.ACCESS_SIGN);

		registration.registerBlockComponent(new BlockDetailsProvider(), Block.class);
		registration.registerBlockComponent(new BlockDetailsBodyProvider(), Block.class);
		registration.markAsClientFeature(JadeIds.ACCESS_BLOCK_DETAILS);

		registration.registerBlockComponent(new BlockAmountProvider(), Block.class);
		registration.markAsClientFeature(JadeIds.ACCESS_BLOCK_AMOUNT);

		registration.registerEntityComponent(new EntityDetailsProvider(), Entity.class);
		registration.registerEntityComponent(new EntityDetailsBodyProvider(), Entity.class);
		registration.markAsClientFeature(JadeIds.ACCESS_ENTITY_DETAILS);

		registration.registerEntityComponent(new EntityVariantProvider(), LivingEntity.class);
		registration.markAsClientFeature(JadeIds.ACCESS_ENTITY_VARIANT);

		registration.registerEntityComponent(new HeldItemProvider(), LivingEntity.class);
		registration.markAsClientFeature(JadeIds.ACCESS_HELD_ITEM);
	}

	public static void replaceTitle(ITooltip tooltip, String objectName, String key) {
		String message = tooltip.getMessage(JadeIds.CORE_OBJECT_NAME);
		if (!message.isBlank()) {
			var nameClass = JadeLanguages.INSTANCE.getNameClass(objectName);
			var title = IThemeHelper.get().title(JadeClient.format("jade.access." + key, message, nameClass));
			tooltip.replace(JadeIds.CORE_OBJECT_NAME, title);
		}
	}
}
