package mcp.mobius.waila.api.impl;

import mcp.mobius.waila.api.TooltipPosition;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DumpGenerator {

    public static String generateInfoDump() {
        StringBuilder builder = new StringBuilder("# Waila Handler Dump");

        builder.append("\n## Block");
        createSection(builder, "Stack Providers", (Map<Class, List<Object>>) (Object) WailaRegistrar.INSTANCE.blockStackProviders);
        createSection(builder, "Head Providers", (Map<Class, List<Object>>) (Object) WailaRegistrar.INSTANCE.blockComponentProviders.get(TooltipPosition.HEAD));
        createSection(builder, "Body Providers", (Map<Class, List<Object>>) (Object) WailaRegistrar.INSTANCE.blockComponentProviders.get(TooltipPosition.BODY));
        createSection(builder, "Tail Providers", (Map<Class, List<Object>>) (Object) WailaRegistrar.INSTANCE.blockComponentProviders.get(TooltipPosition.TAIL));
        createSection(builder, "Data Providers", (Map<Class, List<Object>>) (Object) WailaRegistrar.INSTANCE.blockDataProviders);
        createSection(builder, "Decorators", (Map<Class, List<Object>>) (Object) WailaRegistrar.INSTANCE.blockDecorators);

        builder.append("\n## Entity");
        createSection(builder, "Override Providers", (Map<Class, List<Object>>) (Object) WailaRegistrar.INSTANCE.entityOverrideProviders);
        createSection(builder, "Head Providers", (Map<Class, List<Object>>) (Object) WailaRegistrar.INSTANCE.entityComponentProviders.get(TooltipPosition.HEAD));
        createSection(builder, "Body Providers", (Map<Class, List<Object>>) (Object) WailaRegistrar.INSTANCE.entityComponentProviders.get(TooltipPosition.BODY));
        createSection(builder, "Tail Providers", (Map<Class, List<Object>>) (Object) WailaRegistrar.INSTANCE.entityComponentProviders.get(TooltipPosition.TAIL));
        createSection(builder, "Data Providers", (Map<Class, List<Object>>) (Object) WailaRegistrar.INSTANCE.entityDataProviders);

        return builder.toString();
    }

    private static void createSection(StringBuilder builder, String subsection, Map<Class, List<Object>> providers) {
        if (providers.isEmpty())
            return;

        builder.append("\n### ").append(subsection);
        providers.forEach((k, v) -> {
            builder.append("\n\n#### ").append(k.getName());
            v.stream().distinct().map(o -> o.getClass().getName()).sorted(String::compareToIgnoreCase).forEachOrdered(s -> builder.append("\n* ").append(s));
        });
        builder.append("\n\n");
    }
}
