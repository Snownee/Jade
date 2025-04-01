package snownee.jade.api.view;

import java.util.List;

import org.jetbrains.annotations.ApiStatus.Experimental;

import snownee.jade.api.Accessor;
import snownee.jade.api.IJadeProvider;

@Experimental
public interface IClientExtensionProvider<IN, OUT> extends IJadeProvider {

	List<ClientViewGroup<OUT>> getClientGroups(Accessor<?> accessor, List<ViewGroup<IN>> groups);

}
