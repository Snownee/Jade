package snownee.jade.addon.harvest;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import snownee.jade.Jade;
import snownee.jade.api.callback.CallbackContainer;
import snownee.jade.api.harvest.ToolTier;
import snownee.jade.api.harvest.ToolTierAddedCallback;
import snownee.jade.api.harvest.ToolType;
import snownee.jade.api.harvest.ToolTypeRegistry;
import snownee.jade.impl.WailaClientRegistration;

public final class ToolTypeRegistryImpl implements ToolTypeRegistry {

	private static ImmutableMap<Identifier, ToolType> TOOL_TYPES = ImmutableMap.of();
	public static final Supplier<ToolTier> DEFAULT_SHEARS_TIER = Suppliers.memoize(() -> ToolTier.alwaysPass(Items.SHEARS));
	private final Map<Identifier, ToolType> pendingMap = Maps.newLinkedHashMap();
	private final Map<Identifier, CallbackContainer<ToolTierAddedCallback>> pendingCallbacks = Maps.newHashMap();

	private ToolTypeRegistryImpl() {
	}

	@Override
	public ToolType type(ToolType type) {
		Objects.requireNonNull(type);
		Identifier id = type.getUid();
		Objects.requireNonNull(id);
		if (pendingMap.containsKey(id)) {
			Jade.LOGGER.warn("Skipped duplicate harvest tool type registration: {}", id);
			return type;
		}
		pendingMap.put(id, type);
		CallbackContainer<ToolTierAddedCallback> callbacks = pendingCallbacks.remove(id);
		if (callbacks != null) {
			for (ToolTierAddedCallback callback : callbacks.callbacks()) {
				type.tierAddedCallbacks().add(callback);
			}
		}
		return type;
	}

	@Override
	public ToolType type(Identifier id) {
		return type(id, true);
	}

	@Override
	public ToolType type(Identifier id, boolean skipInstaBreakingBlock) {
		return type(ToolType.of(id, skipInstaBreakingBlock));
	}

	@Override
	public @Nullable ToolType get(Identifier id) {
		return pendingMap.get(id);
	}

	@Override
	public CallbackContainer<ToolTierAddedCallback> tierAddedCallback(Identifier typeId) {
		ToolType type = get(typeId);
		if (type != null) {
			return type.tierAddedCallbacks();
		}
		return pendingCallbacks.computeIfAbsent(typeId, _ -> new CallbackContainer<>());
	}

	@Override
	public ToolTier defaultShearsTier() {
		return DEFAULT_SHEARS_TIER.get();
	}

	public static synchronized Map<Identifier, ? extends ToolType> registeredTypes() {
		return TOOL_TYPES;
	}

	public static synchronized void apply() {
		clear();
		ToolTypeRegistryImpl registry = new ToolTypeRegistryImpl();
		for (Consumer<ToolTypeRegistry> plugin : WailaClientRegistration.instance().harvestPlugins) {
			plugin.accept(registry);
		}
		TOOL_TYPES = ImmutableMap.copyOf(registry.pendingMap);
	}

	public static synchronized void clear() {
		DEFAULT_SHEARS_TIER.get().replaceExtraBlocks(List.of(Blocks.TRIPWIRE));
		TOOL_TYPES = ImmutableMap.of();
	}
}
