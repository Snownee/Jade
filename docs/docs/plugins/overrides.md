# Overrides

!!! note

    Before talking about overrides. If you are looking for a way to show the "correct" name of a block, you probably actually need `IWailaClientRegistration#usePickedResult`

You can subscribe to `WailaRayTraceEvent` to replace the ray-trace result. New result can be created from `IWailaClientRegistration` in the `IWailaPlugin#registerClient` method.

Here is a small example that displays grass block as TNT block:

``` java
package mcp.mobius.waila.test;

import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.event.WailaRayTraceEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;

@WailaPlugin
public class ExamplePlugin implements IWailaPlugin {

	private static IWailaClientRegistration client;

	public ExamplePlugin() {
		MinecraftForge.EVENT_BUS.addListener(this::overrideGrass);
	}

	public void overrideGrass(WailaRayTraceEvent event) {
		Accessor<?> accessor = event.getAccessor();
		if (accessor instanceof BlockAccessor blockAccessor) {
			if (blockAccessor.getBlock() == Blocks.GRASS_BLOCK) {
				accessor = client.createBlockAccessor(
					Blocks.TNT.defaultBlockState(),
					null,
					accessor.getLevel(),
					accessor.getPlayer(),
					null,
					blockAccessor.getHitResult(),
					accessor.isServerConnected()
				);
				event.setAccessor(accessor);
			}
		}
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		ExamplePlugin.client = registration;
	}

}
```

Result:

![](../images/overrides.png)