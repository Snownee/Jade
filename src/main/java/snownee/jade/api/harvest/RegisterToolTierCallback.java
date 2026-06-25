package snownee.jade.api.harvest;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import com.google.common.collect.Maps;

import net.minecraft.resources.Identifier;
import snownee.jade.api.callback.CallbackContainer;

/**
 * Registers ordered harvest tool tiers for a single tool type.
 * <p>
 * Call {@link #event(Identifier)} with the target type id, then register a callback. Jade executes tier callbacks when
 * that tool type is materialized for harvest checks. Buckets for tool types that are never materialized are not invoked.
 * <p>
 * The callback priority controls ordering when Jade invokes all callbacks during tool type materialization. If the tool type
 * already exists when this event receives a new callback, Jade immediately invokes only the newly added callback, so its
 * priority does not delay or reorder that immediate invocation.
 * <p>
 * This API is intended for Jade's plugin registration phase. Modifying tiers later at runtime may require cache
 * invalidation and is not recommended.
 */
@FunctionalInterface
public interface RegisterToolTierCallback {

	/**
	 * Returns the tier-registration event bucket for {@code toolType}.
	 */
	static CallbackContainer<RegisterToolTierCallback> event(Identifier toolType) {
		return RegisterToolTierEvents.EVENTS.computeIfAbsent(toolType, RegisterToolTierEvent::new);
	}

	/**
	 * Clears registered callbacks during Jade's plugin reload cycle.
	 */
	@ApiStatus.Internal
	static void clearEvents() {
		RegisterToolTierEvents.EVENTS.clear();
	}

	/**
	 * Registers {@link ToolTier} entries for {@code toolType}.
	 * <p>
	 * Example: {@code handler.add(ToolTier.item(myTierId, myItem));}
	 */
	void register(Identifier toolType, MutableToolHandler handler);
}

final class RegisterToolTierEvents {
	static final Map<Identifier, CallbackContainer<RegisterToolTierCallback>> EVENTS = Maps.newLinkedHashMap();

	private RegisterToolTierEvents() {
	}
}

final class RegisterToolTierEvent extends CallbackContainer<RegisterToolTierCallback> {
	private final Identifier toolType;

	RegisterToolTierEvent(Identifier toolType) {
		this.toolType = toolType;
	}

	@Override
	public void add(int priority, @NonNull RegisterToolTierCallback callback) {
		super.add(priority, callback);
		MutableToolHandler handler = ToolTypeRegistry.get(toolType);
		if (handler != null) {
			callback.register(toolType, handler);
		}
	}
}
