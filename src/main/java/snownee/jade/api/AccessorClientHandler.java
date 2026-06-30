package snownee.jade.api;

import java.util.List;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.Element;

/**
 * Client-side bridge that collects data, chooses an icon, and builds tooltip content for an accessor.
 *
 * @param <T> accessor type handled by this handler
 */
public interface AccessorClientHandler<T extends Accessor<?>> {

	/**
	 * Returns whether the accessor should be rendered at all.
	 *
	 * @param accessor the current accessor
	 * @return {@code true} if the target should be displayed
	 */
	boolean shouldDisplay(T accessor);

	/**
	 * Returns the server data providers that should be queried for this accessor.
	 *
	 * @param accessor the current accessor
	 * @return providers that need to be synced before rendering
	 */
	List<IServerDataProvider<T>> shouldRequestData(T accessor);

	/**
	 * Requests server data from the selected providers.
	 *
	 * @param accessor the current accessor
	 * @param providers providers selected by {@link #shouldRequestData(Accessor)}
	 */
	void requestData(T accessor, List<IServerDataProvider<T>> providers);

	/**
	 * Returns the icon to render for this accessor.
	 *
	 * @param accessor the current accessor
	 * @return the icon element or {@code null}
	 */
	@Nullable Element getIcon(T accessor);

	/**
	 * Builds tooltip content for the accessor.
	 *
	 * @param accessor the current accessor
	 * @param tooltipProvider callback used to obtain a tooltip for each provider
	 */
	void gatherComponents(T accessor, Function<IJadeProvider, ITooltip> tooltipProvider);

	default boolean isEnabled(IToggleableProvider provider) {
		if (!IWailaConfig.get().accessibility().getEnableAccessibilityPlugin() && JadeIds.isAccess(provider.getUid())) {
			return false;
		}
		return IWailaConfig.get().plugin().get(provider);
	}
}
