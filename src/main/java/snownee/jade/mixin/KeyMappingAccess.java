package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

@Mixin(KeyMapping.class)
public interface KeyMappingAccess {

	@Accessor
	InputConstants.Key getKey();

}
