package snownee.jade.util;

import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.callback.JadeItemModNameCallback;
import snownee.jade.impl.WailaClientRegistration;

public class ModIdentification implements ResourceManagerReloadListener {

	public static final Map<String, String> NAMES = Maps.newConcurrentMap();
	public static final ModIdentification INSTANCE = new ModIdentification();

	static {
		init();
	}

	public static void init() {
		NAMES.clear();
		ClientPlatformProxy.initModNames(NAMES);
	}

	public static String getModName(String namespace) {
		return NAMES.computeIfAbsent(namespace, $ -> {
			String key = "jade.modName." + namespace;
			if (I18n.exists(key)) {
				return I18n.get(key);
			} else {
				return namespace;
			}
		});
	}

	public static String getModName(ResourceLocation id) {
		return getModName(id.getNamespace());
	}

	public static String getModName(Block block) {
		return getModName(PlatformProxy.getId(block));
	}

	public static String getModName(ItemStack stack) {
		for (JadeItemModNameCallback callback : WailaClientRegistration.INSTANCE.itemModNameCallbacks) {
			String s = callback.gatherItemModName(stack);
			if (!Strings.isNullOrEmpty(s)) {
				return s;
			}
		}
		return getModName(PlatformProxy.getModIdFromItem(stack));
	}

	public static String getModName(Entity entity) {
		if (entity instanceof Painting) {
			Motive motive = ((Painting) entity).motive;
			if (motive != null) {
				return getModName(PlatformProxy.getId(motive).getNamespace());
			}
		}
		if (entity instanceof ItemEntity) {
			return getModName(((ItemEntity) entity).getItem());
		}
		if (entity instanceof FallingBlockEntity) {
			return getModName(((FallingBlockEntity) entity).getBlockState().getBlock());
		}
		return getModName(PlatformProxy.getId(entity.getType()));
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		init();
	}

}
