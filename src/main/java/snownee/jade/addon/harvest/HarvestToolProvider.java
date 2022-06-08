package snownee.jade.addon.harvest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElement.Align;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.SubTextElement;
import snownee.jade.util.PlatformProxy;

public enum HarvestToolProvider implements IBlockComponentProvider, ResourceManagerReloadListener {

	INSTANCE;

	public static final Cache<BlockState, ImmutableList<ItemStack>> resultCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
	public static final Map<String, ToolHandler> TOOL_HANDLERS = Maps.newLinkedHashMap();

	private static final Component UNBREAKABLE_TEXT = Component.translatable("jade.harvest_tool.unbreakable").withStyle(ChatFormatting.DARK_RED);
	private static final Component CHECK = Component.literal("✔").withStyle(ChatFormatting.GREEN);
	private static final Component X = Component.literal("✕").withStyle(ChatFormatting.RED);
	private static final Vec2 ITEM_SIZE = new Vec2(10, 0);

	static {
		registerHandler(new SimpleToolHandler("pickaxe", BlockTags.MINEABLE_WITH_PICKAXE, Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE));
		registerHandler(new SimpleToolHandler("axe", BlockTags.MINEABLE_WITH_AXE, Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE));
		registerHandler(new SimpleToolHandler("shovel", BlockTags.MINEABLE_WITH_SHOVEL, Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL));
		registerHandler(new SimpleToolHandler("hoe", BlockTags.MINEABLE_WITH_HOE, Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE));
		SpecialToolHandler handler = new SpecialToolHandler("sword", Items.WOODEN_SWORD.getDefaultInstance());
		handler.blocks.add(Blocks.COBWEB);
		registerHandler(handler);
		registerHandler(new ShearsToolHandler());
	}

	@Nullable
	public static ImmutableList<ItemStack> getTool(BlockState state, Level world, BlockPos pos) {
		ImmutableList.Builder<ItemStack> tools = ImmutableList.builder();
		for (ToolHandler handler : TOOL_HANDLERS.values()) {
			ItemStack tool = handler.test(state, world, pos);
			if (!tool.isEmpty()) {
				tools.add(tool);
			}
		}
		return tools.build();
	}

	public static synchronized void registerHandler(ToolHandler handler) {
		TOOL_HANDLERS.put(handler.getName(), handler);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Player player = accessor.getPlayer();
		if (player.isCreative() || player.isSpectator()) {
			return;
		}
		IElementHelper helper = tooltip.getElementHelper();
		BlockState state = accessor.getBlockState();
		float hardness = state.getDestroySpeed(accessor.getLevel(), accessor.getPosition());
		if (hardness < 0) {
			tooltip.add(helper.text(UNBREAKABLE_TEXT).message(null));
			return;
		}

		boolean newLine = config.get(Identifiers.MC_HARVEST_TOOL_NEW_LINE);
		List<IElement> elements = getText(accessor, config, tooltip.getElementHelper());
		if (elements.isEmpty()) {
			return;
		}
		elements.forEach(e -> e.message(null));
		if (newLine) {
			tooltip.add(elements);
		} else {
			elements.forEach(e -> e.align(Align.RIGHT));
			tooltip.append(0, elements);
		}
	}

	public List<IElement> getText(BlockAccessor accessor, IPluginConfig config, IElementHelper helper) {
		BlockState state = accessor.getBlockState();
		List<ItemStack> tools = Collections.EMPTY_LIST;
		try {
			tools = resultCache.get(state, () -> getTool(state, accessor.getLevel(), accessor.getPosition()));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (tools.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		if (!state.requiresCorrectToolForDrops() && !config.get(Identifiers.MC_EFFECTIVE_TOOL)) {
			return Collections.EMPTY_LIST;
		}

		int offsetY = 0;
		if (!config.get(Identifiers.MC_HARVEST_TOOL_NEW_LINE)) {
			offsetY = -3;
		}
		List<IElement> elements = Lists.newArrayList();
		for (ItemStack tool : tools) {
			elements.add(helper.item(tool, 0.75f).translate(new Vec2(-1, offsetY)).size(ITEM_SIZE).message(null));
		}

		if (!elements.isEmpty()) {
			elements.add(0, helper.spacer(5, 0));
			ItemStack held = accessor.getPlayer().getMainHandItem();
			boolean canHarvest = held.isCorrectToolForDrops(state);
			if (PlatformProxy.isShearable(state) && PlatformProxy.isShears(held)) {
				canHarvest = true;
			}
			if (state.requiresCorrectToolForDrops()) {
				Component sub = canHarvest ? CHECK : X;
				elements.add(new SubTextElement(sub).translate(new Vec2(-2, 7 + offsetY)));
			} else if (canHarvest) {
				elements.add(new SubTextElement(CHECK).translate(new Vec2(-2, 7 + offsetY)));
			}
		}

		return elements;
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		//if (resourcePredicate.test(VanillaResourceType.LANGUAGES)) {
		resultCache.invalidateAll();
		//}
	}

	@Override
	public ResourceLocation getUid() {
		return Identifiers.MC_HARVEST_TOOL;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.HEAD + 2000;
	}

}
