package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccess {
	@Invoker
	Component callGetTypeName();

	@Invoker
	void callApplyImplicitComponents(DataComponentGetter getter);
}
