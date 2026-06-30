package snownee.jade.api.view;

import java.util.List;

import org.jspecify.annotations.Nullable;

import snownee.jade.api.Accessor;
import snownee.jade.api.IJadeProvider;

/**
 * Server-side provider that groups storage-like data for later client rendering.
 *
 * @param <T> server-side data type
 */
public interface IServerExtensionProvider<T> extends IJadeProvider {

	/**
	 * Builds the logical view groups for the current target.
	 *
	 * @param accessor current accessor
	 * @return grouped views, or {@code null} if nothing should be shown
	 */
	@Nullable
	List<ViewGroup<T>> getGroups(Accessor<?> accessor);

	/**
	 * Returns whether Jade should request data for this provider.
	 *
	 * @param accessor current accessor
	 * @return {@code true} if data should be requested
	 */
	default boolean shouldRequestData(Accessor<?> accessor) {
		return true;
	}

}
