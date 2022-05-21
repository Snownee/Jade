package snownee.jade.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

public class ModIdentification implements ResourceManagerReloadListener {

	public static final Map<String, String> NAMES = Maps.newConcurrentMap();
	public static final ModIdentification INSTANCE = new ModIdentification();

	static {
		init();
	}

	public static void init() {
		NAMES.clear();
		List<IModInfo> mods = ImmutableList.copyOf(ModList.get().getMods());
		for (IModInfo mod : mods) {
			String modid = mod.getModId();
			String name = mod.getDisplayName();
			if (Strings.isNullOrEmpty(name)) {
				StringUtils.capitalize(modid);
			}
			NAMES.put(modid, name);
		}
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
		return getModName(block.getRegistryName());
	}

	public static String getModName(ItemStack stack) {
		return getModName(stack.getItem().getCreatorModId(stack));
	}

	public static String getModName(Entity entity) {
		if (entity instanceof Painting) {
			Motive motive = ((Painting) entity).motive;
			if (motive != null) {
				return getModName(motive.getRegistryName().getNamespace());
			}
		}
		if (entity instanceof ItemEntity) {
			return getModName(((ItemEntity) entity).getItem());
		}
		if (entity instanceof FallingBlockEntity) {
			return getModName(((FallingBlockEntity) entity).getBlockState().getBlock());
		}
		return getModName(entity.getType().getRegistryName());
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		init();
	}

}
