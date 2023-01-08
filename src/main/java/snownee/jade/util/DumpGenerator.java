package snownee.jade.util;

import java.util.Comparator;

import com.google.common.collect.Multimap;

import snownee.jade.api.IJadeProvider;
import snownee.jade.impl.HierarchyLookup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;

@SuppressWarnings("unchecked")
public class DumpGenerator {

	public static String generateInfoDump() {
		StringBuilder builder = new StringBuilder("# Jade Handler Dump");

		builder.append("\n## Block");
		createSection(builder, "Icon Providers", WailaClientRegistration.INSTANCE.blockIconProviders);
		createSection(builder, "Component Providers", WailaClientRegistration.INSTANCE.blockComponentProviders);
		createSection(builder, "Data Providers", WailaCommonRegistration.INSTANCE.blockDataProviders);

		builder.append("\n## Entity");
		createSection(builder, "Icon Providers", WailaClientRegistration.INSTANCE.entityIconProviders);
		createSection(builder, "Component Providers", WailaClientRegistration.INSTANCE.entityComponentProviders);
		createSection(builder, "Data Providers", WailaCommonRegistration.INSTANCE.entityDataProviders);

		return builder.toString();
	}

	private static void createSection(StringBuilder builder, String subsection, HierarchyLookup<? extends IJadeProvider> lookup) {
		Multimap<Class<?>, ? extends IJadeProvider> multimap = lookup.getObjects();
		if (multimap.isEmpty())
			return;

		builder.append("\n### ").append(subsection);
		multimap.asMap().forEach((k, v) -> {
			builder.append("\n\n#### ").append(k.getName());
			v.stream().distinct().sorted(Comparator.comparingInt(WailaCommonRegistration.INSTANCE.priorities::byValue)).forEach($ -> {
				builder.append("\n* ").append($.getUid()).append(", ").append(WailaCommonRegistration.INSTANCE.priorities.byValue($)).append(", ").append($.getClass().getName());
			});
		});
		builder.append("\n\n");
	}
}
