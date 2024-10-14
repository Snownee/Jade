package snownee.jade.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IJadeProvider;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.lookup.IHierarchyLookup;

public class DumpGenerator {

	public static String generateInfoDump() {
		StringBuilder builder = new StringBuilder("# Jade Handler Dump");
		WailaClientRegistration client = WailaClientRegistration.instance();
		WailaCommonRegistration common = WailaCommonRegistration.instance();

		builder.append("\nGenerated at ").append(ZonedDateTime.now(ZoneOffset.UTC)).append("\n");

		builder.append("\n## Block");
		createSection(builder, "Icon Providers", client.blockIconProviders);
		createSection(builder, "Component Providers", client.blockComponentProviders);
		createSection(builder, "Data Providers", common.blockDataProviders);

		builder.append("\n## Entity");
		createSection(builder, "Icon Providers", client.entityIconProviders);
		createSection(builder, "Component Providers", client.entityComponentProviders);
		createSection(builder, "Data Providers", common.entityDataProviders);

		builder.append("\n## Common Extension");
		createSection(builder, "Item Storage", common.itemStorageProviders);
		createSection(builder, "Fluid Storage", common.fluidStorageProviders);
		createSection(builder, "Energy Storage", common.energyStorageProviders);
		createSection(builder, "Progress", common.progressProviders);

		builder.append("\n## Client Extension");
		createSection(builder, "Item Storage", client.itemStorageProviders);
		createSection(builder, "Fluid Storage", client.fluidStorageProviders);
		createSection(builder, "Energy Storage", client.energyStorageProviders);
		createSection(builder, "Progress", client.progressProviders);

		builder.append("\n## Priorities");
		for (ResourceLocation resourceLocation : common.priorities.getSortedList()) {
			builder.append("\n* ").append(resourceLocation);
		}

		return builder.toString();
	}

	private static void createSection(StringBuilder builder, String subsection, Map<ResourceLocation, ? extends IJadeProvider> map) {
		if (map.isEmpty()) {
			return;
		}
		builder.append("\n### ").append(subsection);
		map.forEach((key, value) -> {
			builder.append("\n\n#### ").append(key);
			builder.append("\n* ").append(value.getUid()).append(", ").append(value.getClass().getName());
		});
		builder.append("\n\n");
	}

	private static void createSection(StringBuilder builder, String subsection, IHierarchyLookup<? extends IJadeProvider> lookup) {
		if (lookup.isEmpty()) {
			return;
		}
		builder.append("\n### ").append(subsection);
		lookup.entries().forEach(entry -> {
			builder.append("\n\n#### ").append(entry.getKey().getName());
			entry.getValue()
					.stream()
					.distinct()
					.sorted(Comparator.comparingInt(WailaCommonRegistration.instance().priorities::byValue))
					.forEach($ -> {
						builder.append("\n* ").append($.getUid()).append(", ").append(WailaCommonRegistration.instance().priorities.byValue(
								$)).append(", ").append($.getClass().getName());
					});
		});
		builder.append("\n\n");
	}
}
