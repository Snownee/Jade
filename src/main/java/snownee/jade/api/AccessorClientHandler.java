package snownee.jade.api;

import java.util.function.Function;

import snownee.jade.api.ui.IElement;

public interface AccessorClientHandler<T extends Accessor<?>> {

	boolean shouldDisplay(T accessor);

	boolean shouldRequestData(T accessor);

	void requestData(T accessor);

	IElement getIcon(T accessor);

	void gatherComponents(T accessor, Function<IJadeProvider, ITooltip> tooltipProvider);
}
