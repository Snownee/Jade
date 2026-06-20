package snownee.jade.api.harvest;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import snownee.jade.addon.harvest.SimpleToolHandler;

public final class ToolTypeRegistry {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<Identifier, Supplier<? extends MutableToolHandler>> TOOL_TYPES = Maps.newLinkedHashMap();
	private static final LoadingCache<Boolean, Map<Identifier, ? extends MutableToolHandler>> TOOL_TYPES_VIEW = CacheBuilder.newBuilder()
			.build(new CacheLoader<>() {
				@Override
				public Map<Identifier, ? extends MutableToolHandler> load(Boolean key) {
					return Maps.transformValues(TOOL_TYPES, Supplier::get);
				}
			});

	private ToolTypeRegistry() {
	}

	public static synchronized boolean register(Identifier type, Supplier<? extends MutableToolHandler> supplier) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(supplier);
		if (TOOL_TYPES.containsKey(type)) {
			LOGGER.warn("Skipped duplicate harvest tool type registration: {}", type);
			return false;
		}
		TOOL_TYPES.put(type, Suppliers.memoize(supplier::get));
		TOOL_TYPES_VIEW.invalidateAll();
		return true;
	}

	public static synchronized boolean register(Identifier type, List<Item> tools) {
		return register(type, tools, true);
	}

	public static synchronized boolean register(Identifier type, List<Item> tools, boolean skipInstaBreakingBlock) {
		Objects.requireNonNull(tools);
		return register(type, () -> {
			var handler = SimpleToolHandler.create(type, tools, skipInstaBreakingBlock);
			RegisterToolTierCallback.event(type).call(callback -> callback.register(type, handler));
			return handler;
		});
	}

	@ApiStatus.Internal
	public static synchronized Map<Identifier, ? extends ToolHandler> registeredTypes() {
		return mutableTypes();
	}

	private static synchronized Map<Identifier, ? extends  MutableToolHandler> mutableTypes() {
		return TOOL_TYPES_VIEW.getUnchecked(true);
	}

	@ApiStatus.Internal
	public static synchronized void apply() {
		Objects.requireNonNull(registeredTypes().values());
	}

	@ApiStatus.Internal
	public static synchronized MutableToolHandler get(Identifier type) {
		return mutableTypes().get(type);
	}

	@ApiStatus.Internal
	public static synchronized void clear() {
		TOOL_TYPES_VIEW.invalidateAll();
		TOOL_TYPES.clear();
		RegisterToolTierCallback.clearEvents();
	}
}
