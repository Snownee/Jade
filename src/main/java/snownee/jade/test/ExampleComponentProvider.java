package snownee.jade.test;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.gui.LayoutWithPadding;

public class ExampleComponentProvider implements IBlockComponentProvider {
	public static final ExampleComponentProvider INSTANCE = new ExampleComponentProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		tooltip.add(Button.builder(
				CommonComponents.GUI_DONE, _ -> {
					Jade.LOGGER.info("Button clicked in ExampleComponentProvider");
				}).build());
		tooltip.add(new LayoutWithPadding(JadeUI.item(new ItemStack(Items.DIAMOND)), 2, 2, 2, 2));
		Optional<Integer> fuel = ExampleDataProvider.INSTANCE.decodeFromData(accessor);
		if (fuel.isPresent()) {
			Element icon = JadeUI.smallItem(new ItemStack(Items.CLOCK));
			tooltip.add(icon);
			tooltip.append(Component.translatable("mymod.fuel", fuel.orElse(0)));
		}

		Component test1 = Component.literal("1").withStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Component.literal("1"))));
		Component test2 = Component.literal("2")
				.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowItem(new ItemStackTemplate(Items.DIAMOND))));
		Component test3 = Component.translatable("container.dropper")
				.withStyle(Style.EMPTY.withClickEvent(new ClickEvent.CopyToClipboard("test")));
		tooltip.add(JadeUI.text(test1).flexGrow(1));
		tooltip.append(JadeUI.text(test2).flexGrow(1));
		tooltip.append(JadeUI.text(test3).flexGrow(2));

		tooltip.add(JadeUI.text(test1).flexGrow(1));
		tooltip.append(JadeUI.text(test2).flexGrow(0));
		tooltip.append(JadeUI.text(test3).flexGrow(2));

		Element text = JadeUI.text(Component.literal("test"));
		tooltip.replace(JadeIds.CORE_OBJECT_NAME, $ -> List.of(List.of(text)));
	}

	@Override
	public Identifier getUid() {
		return ExamplePlugin.UID_TEST_FUEL;
	}

	@Override
	public int getDefaultPriority() {
		return 999999;
	}
}
