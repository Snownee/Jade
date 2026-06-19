package snownee.jade.api.harvest;

import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;

public final class ToolTypeRegistry {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<Identifier, MutableToolHandler> TOOL_TYPES = Maps.newLinkedHashMap();

	private ToolTypeRegistry() {
	}

	public static synchronized boolean register(MutableToolHandler handler) {
		Objects.requireNonNull(handler);
		Identifier type = Objects.requireNonNull(handler.getUid());
		if (TOOL_TYPES.containsKey(type)) {
			LOGGER.warn("Skipped duplicate harvest tool type registration: {}", type);
			return false;
		}
		TOOL_TYPES.put(type, handler);
		RegisterToolTierCallback.event(type).call(callback -> callback.register(type, handler));
		return true;
	}

	@ApiStatus.Internal
	public static synchronized Map<Identifier, MutableToolHandler> registeredTypes() {
		return ImmutableMap.copyOf(TOOL_TYPES);
	}

	@ApiStatus.Internal
	public static synchronized MutableToolHandler get(Identifier type) {
		return TOOL_TYPES.get(type);
	}

	@ApiStatus.Internal
	public static synchronized void clear() {
		TOOL_TYPES.clear();
		RegisterToolTierCallback.clearEvents();
	}
}
