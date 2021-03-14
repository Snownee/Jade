package mcp.mobius.waila.utils;

import com.google.common.collect.Multimap;

import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.impl.HierarchyLookup;
import mcp.mobius.waila.impl.WailaRegistrar;

@SuppressWarnings("unchecked")
public class DumpGenerator {

    public static String generateInfoDump() {
        StringBuilder builder = new StringBuilder("# Waila Handler Dump");

        builder.append("\n## Block");
        createSection(builder, "Stack Providers", WailaRegistrar.INSTANCE.blockStackProviders);
        createSection(builder, "Head Providers", WailaRegistrar.INSTANCE.blockComponentProviders.get(TooltipPosition.HEAD));
        createSection(builder, "Body Providers", WailaRegistrar.INSTANCE.blockComponentProviders.get(TooltipPosition.BODY));
        createSection(builder, "Tail Providers", WailaRegistrar.INSTANCE.blockComponentProviders.get(TooltipPosition.TAIL));
        createSection(builder, "Data Providers", WailaRegistrar.INSTANCE.blockDataProviders);

        builder.append("\n## Entity");
        createSection(builder, "Override Providers", WailaRegistrar.INSTANCE.entityOverrideProviders);
        createSection(builder, "Stack Providers", WailaRegistrar.INSTANCE.entityStackProviders);
        createSection(builder, "Head Providers", WailaRegistrar.INSTANCE.entityComponentProviders.get(TooltipPosition.HEAD));
        createSection(builder, "Body Providers", WailaRegistrar.INSTANCE.entityComponentProviders.get(TooltipPosition.BODY));
        createSection(builder, "Tail Providers", WailaRegistrar.INSTANCE.entityComponentProviders.get(TooltipPosition.TAIL));
        createSection(builder, "Data Providers", WailaRegistrar.INSTANCE.entityDataProviders);

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
