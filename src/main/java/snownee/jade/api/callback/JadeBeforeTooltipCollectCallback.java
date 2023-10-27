package snownee.jade.api.callback;

import org.apache.commons.lang3.mutable.MutableObject;

import snownee.jade.api.Accessor;
import snownee.jade.api.theme.Theme;

@FunctionalInterface
public interface JadeBeforeTooltipCollectCallback {

	boolean beforeCollecting(MutableObject<Theme> theme, Accessor<?> accessor);

}
