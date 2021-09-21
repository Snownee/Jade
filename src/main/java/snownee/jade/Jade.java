package snownee.jade;

import java.text.DecimalFormat;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.network.MessageBlockBreak;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkDirection;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;

@Mod(Jade.MODID)
public class Jade {
	public static final String MODID = "jade";
	public static final String NAME = "Jade";
	public static DecimalFormat dfCommas = new DecimalFormat("##.##");

	public Jade() {
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, JadeCommonConfig.spec);
		FMLJavaModLoadingContext.get().getModEventBus().register(JadeCommonConfig.class);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
	}

	private void init(FMLCommonSetupEvent event) {
		JadeCommonConfig.refresh();
	}

	@Mod.EventBusSubscriber
	public static class JadeBlockEvent {

		@SubscribeEvent
		public static void breakBlock(BlockEvent.BreakEvent event) {
			IWorld world = event.getWorld();
			boolean isRemote = world.isRemote();
			if (!isRemote) {
				Waila.NETWORK.sendTo(new MessageBlockBreak(), ((ServerPlayerEntity) event.getPlayer()).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
			}
		}
	}
}
