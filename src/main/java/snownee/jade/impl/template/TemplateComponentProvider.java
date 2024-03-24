package snownee.jade.impl.template;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

/**
 * A template implementation for script languages like KubeJS
 */
public abstract class TemplateComponentProvider<T extends Accessor<?>> implements IComponentProvider<T> {
	private final ResourceLocation uid;
	private final boolean required;
	private final boolean enabledByDefault;
	private final int defaultPriority;
	private BiFunction<T, IElement, IElement> iconFunction = (accessor, currentIcon) -> null;
	private BiConsumer<ITooltip, T> tooltipFunction = (tooltip, accessor) -> {};

	protected TemplateComponentProvider(ResourceLocation uid, boolean required, boolean enabledByDefault, int defaultPriority) {
		this.uid = uid;
		this.required = required;
		this.enabledByDefault = enabledByDefault;
		this.defaultPriority = defaultPriority;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean enabledByDefault() {
		return enabledByDefault;
	}

	@Override
	public int getDefaultPriority() {
		return defaultPriority;
	}

	@Override
	public ResourceLocation getUid() {
		return uid;
	}

	@Override
	public @Nullable IElement getIcon(T accessor, IPluginConfig config, IElement currentIcon) {
		return iconFunction.apply(accessor, currentIcon);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, T accessor, IPluginConfig config) {
		tooltipFunction.accept(tooltip, accessor);
	}

	public void setIconFunction(BiFunction<T, IElement, IElement> iconFunction) {
		this.iconFunction = iconFunction;
	}

	public void setTooltipFunction(BiConsumer<ITooltip, T> tooltipFunction) {
		this.tooltipFunction = tooltipFunction;
	}
}
