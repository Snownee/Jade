package snownee.jade.util;

import java.util.Map;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.google.common.collect.ImmutableList;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import snownee.jade.Jade;

public class ForgeJadeMetadata extends JadeMetadata {

	public ForgeJadeMetadata() {
		ImmutableList.Builder<String> disableModNameBuilder = ImmutableList.builder();
		for (IModInfo container : ModList.get().getMods()) {
			String modId = container.getModId();
			try {
				Object raw = container.getModProperties().get(Jade.ID);
				if (raw == null) {
					continue;
				}
				Map<String, Object> obj;
				if (raw instanceof AbstractConfig config) {
					obj = config.valueMap();
				} else {
					//noinspection unchecked
					obj = (Map<String, Object>) raw;
				}
				if (obj.isEmpty()) {
					continue;
				}
				accessibilityMod |= getBool(obj, "accessibilityMod");
				fastScroll |= getBool(obj, "fastScroll");
				smoothScroll |= getBool(obj, "smoothScroll");
				if (getBool(obj, "disableItemModNameTooltip")) {
					disableModNameBuilder.add(modId);
				}
			} catch (Exception e) {
				Jade.LOGGER.warn("Failed to read jade metadata for mod %s".formatted(modId), e);
			}
		}
		disableItemModNameTooltip = disableModNameBuilder.build();
	}

	private static boolean getBool(Map<String, Object> obj, String key) {
		if (!obj.containsKey(key)) {
			return false;
		}
		Object value = obj.get(key);
		if (value.getClass() != Boolean.class) {
			return false;
		}
		return (Boolean) value;
	}

}
