package snownee.jade.api;

import net.minecraft.resources.Identifier;

/**
 * Base contract for every Jade API extension point.
 * <p>
 * Implementations provide a stable identifier used for registration, configuration, and ordering.
 */
/**
 * Legacy root interface for all Jade API providers.
 * <p>
 * Use {@link #getUid()} to identify registrations and {@link #getDefaultPriority()} to influence tooltip ordering.
 */
public interface IJadeProvider {

	/**
	 * Returns the unique identifier of this provider within its registry.
	 * <p>
	 * Providers from different registries may reuse the same identifier.
	 *
	 * @return the provider identifier
	 */
	Identifier getUid();

	/**
	 * Returns the default tooltip priority for this provider.
	 * <p>
	 * Lower values render earlier. Values greater than {@code 5000} prevent the content from being collapsed in lite mode.
	 *
	 * @return the default tooltip priority
	 */
	default int getDefaultPriority() {
		return TooltipPosition.BODY;
	}

}
