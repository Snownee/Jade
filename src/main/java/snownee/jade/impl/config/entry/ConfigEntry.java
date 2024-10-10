package snownee.jade.impl.config.entry;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;

public abstract class ConfigEntry<T> {

	protected final ResourceLocation id;
	private final T defaultValue;
	@Nullable
	private T syncedValue;
	private List<Consumer<ResourceLocation>> listeners = List.of();

	public ConfigEntry(ResourceLocation id, T defaultValue) {
		this.id = id;
		this.defaultValue = defaultValue;
	}

	public ResourceLocation id() {
		return id;
	}

	public T defaultValue() {
		return defaultValue;
	}

	@Nullable
	public T syncedValue() {
		return syncedValue;
	}

	public boolean isSynced() {
		return syncedValue != null;
	}

	public void setSyncedValue(@Nullable T value) {
		syncedValue = value;
	}

	public T convertValue(Object value) {
		//noinspection unchecked
		return (T) value;
	}

	abstract public boolean isValidValue(Object value);

	abstract public OptionValue<?> createUI(
			OptionsList options,
			String optionName,
			IPluginConfig config,
			BiConsumer<ResourceLocation, Object> setter);

	public void addListener(Consumer<ResourceLocation> listener) {
		if (listeners.isEmpty()) {
			listeners = Lists.newArrayList();
		}
		listeners.add(listener);
	}

	public void notifyChange() {
		for (Consumer<ResourceLocation> listener : listeners) {
			listener.accept(id);
		}
	}
}
