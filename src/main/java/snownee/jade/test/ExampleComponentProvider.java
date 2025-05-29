package snownee.jade.test;

import java.util.List;

import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

public enum ExampleComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		tooltip.add(Button.builder(
				CommonComponents.GUI_DONE, $ -> {
					Jade.LOGGER.info("Button clicked in ExampleComponentProvider");
				}).build());
		if (accessor.getServerData().contains("Fuel")) {
			Element icon = JadeUI.smallItem(new ItemStack(Items.CLOCK));
			tooltip.add(icon);
			tooltip.append(Component.translatable("mymod.fuel", accessor.getServerData().getIntOr("Fuel", 0)));
		}

		Component test1 = Component.literal("1");
		Component test2 = Component.literal("2");
		Component test3 = Component.literal("3");
		tooltip.add(JadeUI.text(test1).flexGrow(1));
		tooltip.append(JadeUI.text(test2).flexGrow(1));
		tooltip.append(JadeUI.text(test3).flexGrow(2));

		tooltip.add(JadeUI.text(test1).flexGrow(1));
		tooltip.append(JadeUI.text(test2).flexGrow(0));
		tooltip.append(JadeUI.text(test3).flexGrow(2));

		Element text = JadeUI.text(Component.literal("test"));
		tooltip.replace(JadeIds.CORE_OBJECT_NAME, $ -> List.of(List.of(text), List.of(text), List.of(text)));
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) accessor.getBlockEntity();
		data.putInt("Fuel", furnace.litTimeRemaining);
	}

	@Override
	public ResourceLocation getUid() {
		return ExamplePlugin.UID_TEST_FUEL;
	}

}
