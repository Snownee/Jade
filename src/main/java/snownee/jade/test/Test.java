package snownee.jade.test;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import snownee.jade.Jade;

@EventBusSubscriber(bus = Bus.MOD)
public class Test {

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Jade.MODID);

	public static final RegistryObject<TestBlock> BLOCK = BLOCKS.register("test", TestBlock::new);

	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Jade.MODID);

	public static final RegistryObject<BlockItem> ITEM = ITEMS.register("test", () -> new BlockItem(BLOCK.get(), new Item.Properties()));

	private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Jade.MODID);

	public static final RegistryObject<BlockEntityType<TestBlockEntity>> TILE = TILES.register("test", () -> BlockEntityType.Builder.of(TestBlockEntity::new, BLOCK.get()).build(null));

	static {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		BLOCKS.register(bus);
		ITEMS.register(bus);
		TILES.register(bus);
	}

}
