package snownee.jade.impl;

import java.util.List;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import snownee.jade.api.AccessorClientHandler;
import snownee.jade.api.EmptyAccessor;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.Element;

public class EmptyAccessorClientHandler implements AccessorClientHandler<EmptyAccessor> {

	@Override
	public boolean shouldDisplay(EmptyAccessor accessor) {
		return false;
	}

	@Override
	public List<IServerDataProvider<EmptyAccessor>> shouldRequestData(EmptyAccessor accessor) {
		return List.of();
	}

	@Override
	public void requestData(EmptyAccessor accessor, List<IServerDataProvider<EmptyAccessor>> providers) {
	}

	@Override
	public @Nullable Element getIcon(EmptyAccessor accessor) {
		return null;
	}

	@Override
	public void gatherComponents(EmptyAccessor accessor, Function<IJadeProvider, ITooltip> tooltipProvider) {
	}
}
