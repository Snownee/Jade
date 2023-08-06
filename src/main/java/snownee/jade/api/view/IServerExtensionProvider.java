package snownee.jade.api.view;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import snownee.jade.api.Accessor;
import snownee.jade.api.IJadeProvider;

public interface IServerExtensionProvider<IN, OUT> extends IJadeProvider {

	@Nullable
	List<ViewGroup<OUT>> getGroups(Accessor<?> accessor, IN target);

}
