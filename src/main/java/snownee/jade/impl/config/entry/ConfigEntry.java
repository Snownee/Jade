package snownee.jade.impl.config.entry;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.jade.gui.config.WailaOptionsList;
import snownee.jade.gui.config.value.OptionValue;

public abstract class ConfigEntry<T> {

	public final ResourceLocation id;
	private final T defaultValue;
	private boolean synced;
	protected T value;
	private List<Consumer<ResourceLocation>> listeners = List.of();

	public ConfigEntry(ResourceLocation id, T defaultValue) {
		this.id = id;
		value = this.defaultValue = defaultValue;
	}

	public ResourceLocation getId() {
		return id;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public boolean isSynced() {
		return synced;
	}

	public void setSynced(boolean synced) {
		this.synced = synced;
	}

	public T getValue() {
		return value;
	}

	public void setValue(Object value) {
		if (!Objects.equals(this.value, value)) {
			this.value = (T) value;
			notifyChange();
		}
	}

	abstract public boolean isValidValue(Object value);

	@OnlyIn(Dist.CLIENT)
	abstract public OptionValue<?> createUI(WailaOptionsList options, String optionName);

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
