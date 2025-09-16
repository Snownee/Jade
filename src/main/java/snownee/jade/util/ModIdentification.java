package snownee.jade.util;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.JadeIds;
import snownee.jade.api.TraceableException;
import snownee.jade.api.callback.JadeItemModNameCallback;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.overlay.DisplayHelper;

public class ModIdentification implements KeyedResourceManagerReloadListener {

	public static final ResourceLocation ID = JadeIds.JADE("mod_id");
	public static final ModIdentification INSTANCE = new ModIdentification();
	public static int NAME_MAX_WIDTH = 160;
	private static final Map<String, Optional<String>> NAMES = Maps.newConcurrentMap();
	private static final Map<String, Optional<String>> CUT_NAMES = Maps.newConcurrentMap();
	@Nullable
	private static WordCutter wordCutter;
	public static final String JADE_STACK = "$jade:stack";
	public static final MapCodec<ResourceLocation> JADE_STACK_ID_CODEC = ResourceLocation.CODEC.fieldOf("id").fieldOf(JADE_STACK);
	public static final String POLYMER_STACK = "$polymer:stack";
	public static final MapCodec<ResourceLocation> POLYMER_STACK_ID_CODEC = ResourceLocation.CODEC.fieldOf("id").fieldOf(POLYMER_STACK);

	public static WordCutter wordCutter() {
		WordCutter cutter = wordCutter;
		if (cutter == null) {
			JadeLanguages languages = JadeLanguages.INSTANCE;
			BreakIterator iterator = BreakIterator.getWordInstance(languages.getLocale());
			wordCutter = cutter = new WordCutter(iterator, languages);
		}
		return cutter;
	}

	public static void invalidateCache() {
		NAMES.clear();
		CUT_NAMES.clear();
		wordCutter = null;

		/*
		List<String> names = List.of(
				"Minecraft",
				"Forgified Fabric BlockRenderLayer Registration (v1)",
				"Pam's HarvestCraft - Food Extended",
				"Nice Mobs Remastered: Friends & Foes",
				"Nexus (Tower Defense Battle Mode)",
				"MrCrayfish's Furniture Mod: Refurbished",
				"立即重生配置指令 [DCC]doImmediateRespawn Config Command");
		for (String name : names) {
			Jade.LOGGER.info("{} -> {}", name, cutName(name, NAME_MAX_WIDTH));
		}*/
	}

	public static String cutName(String fullName, int maxWidth) {
		fullName = fullName.trim();
		if (maxWidth <= 0) {
			return fullName;
		}
		if (DisplayHelper.font().width(fullName) <= maxWidth) {
			return fullName;
		}

		WordCutter cutter = wordCutter();
		cutter.setText(fullName, maxWidth);

		int tokens;
		do {
			tokens = cutter.tokenCount();
			cutter.removeBracketed();
			cutter.trim();
		} while (tokens != cutter.tokenCount() && cutter.tooLong());

		afterColon:
		if (cutter.hasColon() && cutter.tooLong()) {
			int start = cutter.findFirst(token -> token.type() == WordCutter.TokenType.COLON);
			if (start == -1) {
				break afterColon;
			}
			String s = cutter.concat(start + 1, cutter.tokenCount()).trim().toLowerCase(Locale.ENGLISH);
			boolean remove = s.endsWith("edition") || s.endsWith("version");
			if (!remove && !s.contains(" ")) {
				remove = s.equals("legacy") || s.startsWith("re");
			}
			if (remove) {
				cutter.removeRange(start, cutter.tokenCount());
				cutter.trim();
			}
		}

		if (cutter.tooLong()) {
			WordCutter.Token last = cutter.tokens().getLast();
			if (last.type() == WordCutter.TokenType.WORD && last.str().toLowerCase(Locale.ENGLISH).equals("mod")) {
				cutter.removeRange(cutter.tokenCount() - 1, cutter.tokenCount());
				cutter.trim();
			}
		}

		cutter.cutToMaxWidth(true);
		return cutter.toString();
	}

	public static Optional<String> getModName(String namespace) {
		return getModName(namespace, NAME_MAX_WIDTH);
	}

	public static Optional<String> getModName(String namespace, int maxWidth) {
		if (maxWidth != NAME_MAX_WIDTH) {
			return getModNameInternal(namespace, maxWidth);
		}
		return CUT_NAMES.computeIfAbsent(namespace, $ -> getModNameInternal($, NAME_MAX_WIDTH));
	}

	public static Optional<String> getModNameInternal(String namespace, int maxWidth) {
		String fullName = getModFullName(namespace).orElse(null);
		if (fullName == null) {
			return Optional.empty();
		}
		return Optional.of(cutName(fullName, maxWidth));
	}

	public static Optional<String> getModFullName(String namespace) {
		return NAMES.computeIfAbsent(
				namespace, $ -> {
					String key = "jade.modName." + $;
					if (I18n.exists(key)) {
						return Optional.of(I18n.get(key));
					}
					key = "itemGroup." + $;
					if (I18n.exists(key)) {
						return Optional.of(I18n.get(key));
					}
					return ClientProxy.getModName($).map(ChatFormatting::stripFormatting);
				});
	}

	public static String getModName(ResourceLocation id) {
		return getModName(id.getNamespace()).orElse(id.getNamespace());
	}

	public static String getModName(Block block) {
		ResourceLocation id;
		try {
			id = CommonProxy.getId(block);
		} catch (Throwable e) {
			throw TraceableException.create(e, BuiltInRegistries.BLOCK.getKey(block).getNamespace());
		}
		return getModName(id);
	}

	public static Optional<ResourceLocation> getSpecialId(ItemStack stack) {
		CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		if (!CustomData.EMPTY.equals(data)) {
			if (data.contains(JADE_STACK)) {
				return data.read(JADE_STACK_ID_CODEC).result();
			} else if (data.contains(POLYMER_STACK)) {
				return data.read(POLYMER_STACK_ID_CODEC).result();
			}
		}
		return Optional.empty();
	}

	public static String getModId(ItemStack stack) {
		Optional<ResourceLocation> specialId = getSpecialId(stack);
		if (specialId.isPresent()) {
			return specialId.orElseThrow().getNamespace();
		}
		return CommonProxy.getModIdFromItem(stack);
	}

	public static String getModName(ItemStack stack) {
		String id;
		try {
			for (JadeItemModNameCallback callback : WailaClientRegistration.instance().itemModNameCallback.callbacks()) {
				String s = callback.gatherItemModName(stack);
				if (!Strings.isNullOrEmpty(s)) {
					return s;
				}
			}
			id = getModId(stack);
		} catch (Throwable e) {
			throw TraceableException.create(e, BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
		}
		return getModName(id).orElse(id);
	}

	public static String getModName(Entity entity) {
		if (entity instanceof Painting painting) {
			return getModName(painting.getVariant().unwrapKey().orElseThrow().location());
		}
		if (entity instanceof ItemEntity itemEntity) {
			return getModName(itemEntity.getItem());
		}
		if (entity instanceof FallingBlockEntity fallingBlock) {
			return getModName(fallingBlock.getBlockState().getBlock());
		}
		if (entity instanceof Villager villager) {
			return getModName(villager.getVillagerData().profession().unwrapKey().orElseThrow().location());
		}
		ResourceLocation id;
		try {
			id = CommonProxy.getId(entity.getType());
		} catch (Throwable e) {
			throw TraceableException.create(e, BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace());
		}
		return getModName(id);
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		invalidateCache();
	}

	@Override
	public ResourceLocation getUid() {
		return ID;
	}
}
