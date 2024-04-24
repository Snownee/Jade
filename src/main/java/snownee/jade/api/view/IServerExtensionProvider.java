package snownee.jade.api.view;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import snownee.jade.api.Accessor;
import snownee.jade.api.IJadeProvider;

public interface IServerExtensionProvider<T> extends IJadeProvider {

	@Nullable
	List<ViewGroup<T>> getGroups(Accessor<?> accessor);

	default boolean shouldRequestData(Accessor<?> accessor) {
		return true;
	}

}
