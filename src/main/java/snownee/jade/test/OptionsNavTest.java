package snownee.jade.test;

import net.minecraft.network.chat.Component;
import snownee.jade.gui.config.OptionsList;

public final class OptionsNavTest {

	private OptionsNavTest() {
	}

	public static void addManyEntries(OptionsList options) {
		for (int i = 0; i < 100; i++) {
			options.add(new OptionsList.Title(Component.literal("Scroll Test %03d".formatted(i))));
		}
	}
}