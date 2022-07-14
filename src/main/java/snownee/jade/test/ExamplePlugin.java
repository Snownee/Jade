package snownee.jade.test;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ExamplePlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		//TODO register data providers
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		//TODO register component providers, icon providers, callbacks, and config options here
		//registration.registerBlockComponent(ExampleComponentProvider.INSTANCE, AbstractFurnaceBlock.class);
	}

}
