package snownee.jade.test;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class TestBlockItem extends BlockItem {

	public TestBlockItem(Block p_40565_, Properties p_40566_) {
		super(p_40565_, p_40566_);
	}

	@Override
	public boolean isBarVisible(ItemStack p_150899_) {
		return true;
	}

	@Override
	public int getBarColor(ItemStack p_150901_) {
		return 0xEFEFEF;
	}

	@Override
	public int getBarWidth(ItemStack p_150900_) {
		return 13;
	}

}
