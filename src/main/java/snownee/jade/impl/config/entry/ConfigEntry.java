package snownee.jade.impl.config.entry;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.resources.Identifier;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;

public abstract class ConfigEntry<T> {

	protected final Identifier id;
	private final T defaultValue;
	private @Nullable T syncedValue;
	private List<Consumer<Identifier>> listeners = List.of();

	public ConfigEntry(Identifier id, T defaultValue) {
		this.id = id;
		this.defaultValue = defaultValue;
	}

	public Identifier id() {
		return id;
	}

	public T defaultValue() {
		return defaultValue;
	}

	public T syncedValue() {
		return Objects.requireNonNull(syncedValue);
	}

	public boolean isSynced() {
		return syncedValue != null;
	}

	public void setSyncedValue(@Nullable T value) {
		syncedValue = value;
	}

	@SuppressWarnings("unchecked")
	public T convertValue(Object value) {
		return (T) value;
	}

	abstract public boolean isValidValue(Object value);

	abstract public OptionValue<?> createUI(
			OptionsList options,
			String optionName,
			IPluginConfig config,
			BiConsumer<Identifier, Object> setter);

	public void addListener(Consumer<Identifier> listener) {
		if (listeners.isEmpty()) {
			listeners = Lists.newArrayList();
		}
		listeners.add(listener);
	}

	public void notifyChange() {
		for (Consumer<Identifier> listener : listeners) {
			listener.accept(id);
		}
	}
}
