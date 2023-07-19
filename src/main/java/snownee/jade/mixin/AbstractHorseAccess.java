package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

// We can't access-transform an abstract class, so we use Mixin here.
@Mixin(AbstractHorse.class)
public interface AbstractHorseAccess {

	@Accessor
	SimpleContainer getInventory();

}
