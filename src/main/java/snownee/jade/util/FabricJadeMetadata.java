package snownee.jade.util;

import com.google.common.collect.ImmutableList;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import snownee.jade.Jade;

public class FabricJadeMetadata extends JadeMetadata {

	public FabricJadeMetadata() {
		ImmutableList.Builder<String> disableModNameBuilder = ImmutableList.builder();
		for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
			ModMetadata metadata = container.getMetadata();
			if (!metadata.containsCustomValue(Jade.ID) || metadata.getCustomValue(Jade.ID).getType() != CustomValue.CvType.OBJECT) {
				continue;
			}
			CustomValue.CvObject obj = metadata.getCustomValue(Jade.ID).getAsObject();
			accessibilityMod |= getBool(obj, "accessibilityMod");
			fastScroll |= getBool(obj, "fastScroll");
			smoothScroll |= getBool(obj, "smoothScroll");
			if (getBool(obj, "disableItemModNameTooltip")) {
				disableModNameBuilder.add(metadata.getId());
			}
		}
		disableItemModNameTooltip = disableModNameBuilder.build();
	}

	private static boolean getBool(CustomValue.CvObject obj, String key) {
		if (!obj.containsKey(key)) {
			return false;
		}
		CustomValue value = obj.get(key);
		if (value.getType() != CustomValue.CvType.BOOLEAN) {
			return false;
		}
		return value.getAsBoolean();
	}

}
