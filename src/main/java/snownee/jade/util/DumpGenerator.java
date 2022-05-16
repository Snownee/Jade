package snownee.jade.util;

import com.google.common.collect.Multimap;

import snownee.jade.api.TooltipPosition;
import snownee.jade.impl.HierarchyLookup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;

@SuppressWarnings("unchecked")
public class DumpGenerator {

	public static String generateInfoDump() {
		StringBuilder builder = new StringBuilder("# Waila Handler Dump");

		builder.append("\n## Block");
		createSection(builder, "Icon Providers", WailaClientRegistration.INSTANCE.blockIconProviders);
		createSection(builder, "Head Providers", WailaClientRegistration.INSTANCE.blockComponentProviders.get(TooltipPosition.HEAD));
		createSection(builder, "Body Providers", WailaClientRegistration.INSTANCE.blockComponentProviders.get(TooltipPosition.BODY));
		createSection(builder, "Tail Providers", WailaClientRegistration.INSTANCE.blockComponentProviders.get(TooltipPosition.TAIL));
		createSection(builder, "Data Providers", WailaCommonRegistration.INSTANCE.blockDataProviders);

		builder.append("\n## Entity");
		createSection(builder, "Icon Providers", WailaClientRegistration.INSTANCE.entityIconProviders);
		createSection(builder, "Head Providers", WailaClientRegistration.INSTANCE.entityComponentProviders.get(TooltipPosition.HEAD));
		createSection(builder, "Body Providers", WailaClientRegistration.INSTANCE.entityComponentProviders.get(TooltipPosition.BODY));
		createSection(builder, "Tail Providers", WailaClientRegistration.INSTANCE.entityComponentProviders.get(TooltipPosition.TAIL));
		createSection(builder, "Data Providers", WailaCommonRegistration.INSTANCE.entityDataProviders);

		return builder.toString();
	}

	private static void createSection(StringBuilder builder, String subsection, HierarchyLookup<?> lookup) {
		Multimap<Class<?>, ?> multimap = lookup.getObjects();
		if (multimap.isEmpty())
			return;

		builder.append("\n### ").append(subsection);
		multimap.asMap().forEach((k, v) -> {
			builder.append("\n\n#### ").append(k.getName());
			v.stream().distinct().map(o -> o.getClass().getName()).sorted(String::compareToIgnoreCase).forEachOrdered(s -> builder.append("\n* ").append(s));
		});
		builder.append("\n\n");
	}
}
