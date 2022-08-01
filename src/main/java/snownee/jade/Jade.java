package snownee.jade;

import java.text.DecimalFormat;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.ResourceLocation;
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
	public static final INamedTag<Block> PICK = BlockTags.createOptional(new ResourceLocation(MODID, "pick"));

	public Jade() {
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, JadeCommonConfig.spec);
		FMLJavaModLoadingContext.get().getModEventBus().register(JadeCommonConfig.class);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
	}

	private void init(FMLCommonSetupEvent event) {
		JadeCommonConfig.refresh();
	}
}
