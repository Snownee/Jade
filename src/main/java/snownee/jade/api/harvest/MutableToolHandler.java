package snownee.jade.api.harvest;

import net.minecraft.resources.Identifier;

/**
 * Mutable harvest tool handler used during Jade's plugin registration phase.
 * <p>
 * Runtime changes are not recommended: harvest results may be cached and callers are responsible for invalidating any
 * affected caches before expecting changes to be reflected in the UI.
 */
public interface MutableToolHandler extends ToolHandler {

	void add(ToolTier tier);

	boolean insertBefore(Identifier targetTier, ToolTier tier);

	boolean insertAfter(Identifier targetTier, ToolTier tier);

}
