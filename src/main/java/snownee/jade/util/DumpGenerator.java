package snownee.jade.util;

import java.util.Comparator;

import snownee.jade.api.IJadeProvider;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.lookup.IHierarchyLookup;

public class DumpGenerator {

	public static String generateInfoDump() {
		StringBuilder builder = new StringBuilder("# Jade Handler Dump");
		WailaClientRegistration client = WailaClientRegistration.instance();
		WailaCommonRegistration common = WailaCommonRegistration.instance();

		builder.append("\n## Block");
		createSection(builder, "Icon Providers", client.blockIconProviders);
		createSection(builder, "Component Providers", client.blockComponentProviders);
		createSection(builder, "Data Providers", common.blockDataProviders);

		builder.append("\n## Entity");
		createSection(builder, "Icon Providers", client.entityIconProviders);
		createSection(builder, "Component Providers", client.entityComponentProviders);
		createSection(builder, "Data Providers", common.entityDataProviders);

		return builder.toString();
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
