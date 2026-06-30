package snownee.jade.api.view;

import java.util.List;

import snownee.jade.api.Accessor;
import snownee.jade.api.IJadeProvider;

/**
 * Client-side companion to a server extension provider.
 *
 * @param <IN> server-side data type
 * @param <OUT> client-side view type
 */
public interface IClientExtensionProvider<IN, OUT> extends IJadeProvider {

	/**
	 * Converts server view groups into client render groups.
	 *
	 * @param accessor current accessor
	 * @param groups server groups
	 * @return client groups ready for rendering
	 */
	List<ClientViewGroup<OUT>> getClientGroups(Accessor<?> accessor, List<ViewGroup<IN>> groups);

}
