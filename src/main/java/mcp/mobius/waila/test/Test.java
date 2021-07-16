package mcp.mobius.waila.test;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.MOD, modid = "waila")
public class Test {

	public static final TestBlock BLOCK = new TestBlock();
	public static final TileEntityType<TestTileEntity> TILE = TileEntityType.Builder.create(TestTileEntity::new, BLOCK).build(null);

	@SubscribeEvent
	public static void registerBlocks(Register<Block> event) {
		event.getRegistry().register(BLOCK.setRegistryName("test"));
	}

	@SubscribeEvent
	public static void registerItems(Register<Item> event) {
		event.getRegistry().register(new BlockItem(BLOCK, new Item.Properties()).setRegistryName("test"));
	}

	@SubscribeEvent
	public static void registerTileTypes(Register<TileEntityType<?>> event) {
		event.getRegistry().register(TILE.setRegistryName("test"));
	}

}
