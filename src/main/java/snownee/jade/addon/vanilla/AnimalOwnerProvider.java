package snownee.jade.addon.vanilla;

import java.util.UUID;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.UsernameCache;
import snownee.jade.VanillaPlugin;

public class AnimalOwnerProvider implements IEntityComponentProvider {

	public static final AnimalOwnerProvider INSTANCE = new AnimalOwnerProvider();

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!config.get(VanillaPlugin.ANIMAL_OWNER)) {
			return;
		}
		Entity entity = accessor.getEntity();
		UUID ownerUUID = null;
		if (entity instanceof OwnableEntity) {
			ownerUUID = ((OwnableEntity) entity).getOwnerUUID();
		} else if (entity instanceof AbstractHorse) {
			ownerUUID = ((AbstractHorse) entity).getOwnerUUID();
		}
		if (ownerUUID != null) {
			String name = UsernameCache.getLastKnownUsername(ownerUUID);
			if (name == null) {
				name = "???";
			}
			tooltip.add(new TranslatableComponent("jade.owner", name));
		}
	}

}
