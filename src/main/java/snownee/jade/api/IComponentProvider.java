package snownee.jade.api;

import org.jspecify.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;

/**
 * Client-side tooltip provider for blocks and entities.
 *
 * @param <T> accessor type handled by this provider
 */
public interface IComponentProvider<T extends Accessor<?>> extends IToggleableProvider {

	/**
	 * Allows the provider to replace the default target icon.
	 * <p>
	 * This callback only runs on the client and only for providers registered with
	 * {@link IWailaClientRegistration#registerBlockIcon(IComponentProvider, Class)} or
	 * {@link IWailaClientRegistration#registerEntityIcon(IComponentProvider, Class)}.
	 *
	 * <p>If the icon depends on server-only information, synchronize it through
	 * {@link IServerDataProvider#appendServerData(CompoundTag, Accessor)} first.
	 *
	 * @param accessor accessor describing the current target and context
	 * @param config current plugin configuration
	 * @param currentIcon icon that Jade would otherwise render
	 * @return a replacement icon, or {@code null} to keep the current icon
	 */
	default @Nullable Element getIcon(T accessor, IPluginConfig config, @Nullable Element currentIcon) {
		return null;
	}

	/**
	 * Appends or modifies rendered tooltip elements.
	 * <p>
	 * This callback only runs on the client and only for providers registered with
	 * {@link IWailaClientRegistration#registerBlockComponent(IComponentProvider, Class)} or
	 * {@link IWailaClientRegistration#registerEntityComponent(IComponentProvider, Class)}.
	 *
	 * @param tooltip mutable tooltip container shared with other providers
	 * @param accessor accessor describing the current target and context
	 * @param config current plugin configuration
	 */
	void appendTooltip(ITooltip tooltip, T accessor, IPluginConfig config);

}
