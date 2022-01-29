package snownee.jade.addon.vanilla;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElement.Align;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.impl.ui.SubTextElement;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.Tags;
import snownee.jade.VanillaPlugin;

public class HarvestToolProvider implements IComponentProvider, ResourceManagerReloadListener {

	public static final HarvestToolProvider INSTANCE = new HarvestToolProvider();

	public static final Cache<BlockState, ImmutableList<TestCase>> resultCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
	public static final List<TestCase> testTools = Lists.newLinkedList();

	private static final Component UNBREAKABLE_TEXT = new TranslatableComponent("jade.harvest_tool.unbreakable").withStyle(ChatFormatting.DARK_RED);
	private static final Component CHECK = new TextComponent("✔").withStyle(ChatFormatting.GREEN);
	private static final Component X = new TextComponent("✕").withStyle(ChatFormatting.RED);
	private static final Vec2 ITEM_SIZE = new Vec2(10, 0);
	private static final TestCase SHEARS;

	static {
		/* off */
		registerTool(new ItemStack(Items.WOODEN_PICKAXE), "pickaxe", BlockTags.MINEABLE_WITH_PICKAXE)
			.addTool(Items.STONE_PICKAXE)
			.addTool(Items.IRON_PICKAXE)
			.addTool(Items.DIAMOND_PICKAXE)
			.addTool(Items.NETHERITE_PICKAXE);
		registerTool(new ItemStack(Items.WOODEN_AXE), "axe", BlockTags.MINEABLE_WITH_AXE)
			.addTool(Items.STONE_AXE)
			.addTool(Items.IRON_AXE)
			.addTool(Items.DIAMOND_AXE)
			.addTool(Items.NETHERITE_AXE);
		registerTool(new ItemStack(Items.WOODEN_SHOVEL), "shovel", BlockTags.MINEABLE_WITH_SHOVEL)
			.addTool(Items.STONE_SHOVEL)
			.addTool(Items.IRON_SHOVEL)
			.addTool(Items.DIAMOND_SHOVEL)
			.addTool(Items.NETHERITE_SHOVEL);
		registerTool(new ItemStack(Items.WOODEN_HOE), "hoe", BlockTags.MINEABLE_WITH_HOE)
			.addTool(Items.STONE_HOE)
			.addTool(Items.IRON_HOE)
			.addTool(Items.DIAMOND_HOE)
			.addTool(Items.NETHERITE_HOE);
		SHEARS = registerTool(new ItemStack(Items.SHEARS), "shears", null);
		/* on */
	}

	@Nullable
	public static ImmutableList<TestCase> getTool(BlockState state, Level world, BlockPos pos) {
		ImmutableList.Builder<TestCase> list = ImmutableList.builder();
		for (TestCase testCase : testTools) {
			if (testCase == SHEARS && state.getBlock() instanceof IForgeShearable) {
				list.add(testCase);
				continue;
			}
			if (testCase.blocks == null) {
				if (!testCase.stack.isEmpty() && testCase.stack.isCorrectToolForDrops(state)) {
					list.add(testCase);
				}
			} else {
				if (state.is(testCase.blocks)) {
					list.add(testCase);
				}
			}
		}
		return list.build();
	}

	public static synchronized TestCase registerTool(ItemStack stack, String name, @Nullable Tag<Block> toolType) {
		TestCase testCase = new TestCase(stack, name, toolType);
		testTools.add(testCase);
		return testCase;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Player player = accessor.getPlayer();
		if (player.isCreative() || player.isSpectator()) {
			return;
		}
		BlockState state = accessor.getBlockState();
		float hardness = state.getDestroySpeed(accessor.getLevel(), accessor.getPosition());
		if (hardness < 0) {
			if (accessor.getTooltipPosition() == TooltipPosition.BODY) {
				tooltip.add(UNBREAKABLE_TEXT);
			}
			return;
		}

		boolean newLine = config.get(VanillaPlugin.HARVEST_TOOL_NEW_LINE);
		if (!newLine && accessor.getTooltipPosition() != TooltipPosition.TAIL) {
			return;
		} else if (newLine && accessor.getTooltipPosition() != TooltipPosition.BODY) {
			return;
		}
		List<IElement> elements = getText(accessor, config, tooltip.getElementHelper());
		if (elements.isEmpty()) {
			return;
		}
		if (newLine) {
			tooltip.add(elements);
		} else {
			elements.forEach(e -> e.align(Align.RIGHT));
			tooltip.append(0, elements);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public List<IElement> getText(BlockAccessor accessor, IPluginConfig config, IElementHelper helper) {
		if (!config.get(VanillaPlugin.HARVEST_TOOL)) {
			return Collections.EMPTY_LIST;
		}
		BlockState state = accessor.getBlockState();
		List<TestCase> results = Collections.EMPTY_LIST;
		try {
			results = resultCache.get(state, () -> getTool(state, accessor.getLevel(), accessor.getPosition()));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (results.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		if (!state.requiresCorrectToolForDrops() && !config.get(VanillaPlugin.EFFECTIVE_TOOL)) {
			return Collections.EMPTY_LIST;
		}

		int offsetY = 0;
		if (!config.get(VanillaPlugin.HARVEST_TOOL_NEW_LINE)) {
			offsetY = -3;
		}
		List<IElement> elements = Lists.newArrayList();
		for (TestCase result : results) {
			ItemStack stack = ItemStack.EMPTY;
			if (result.blocks == null) {
				stack = result.stack;
			} else {
				for (ItemStack tool : result.tools) {
					if (tool.isCorrectToolForDrops(state)) {
						stack = tool;
						break;
					}
				}
			}
			if (!stack.isEmpty()) {
				elements.add(helper.item(stack, 0.75f).translate(new Vec2(-1, offsetY)).size(ITEM_SIZE));
			}
		}

		if (!elements.isEmpty()) {
			elements.add(0, helper.spacer(5, 0));
			ItemStack held = accessor.getPlayer().getMainHandItem();
			boolean canHarvest = held.isCorrectToolForDrops(state);
			if (state.getBlock() instanceof IForgeShearable && held.is(Tags.Items.SHEARS)) {
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

	public static class TestCase {
		private ItemStack stack;
		public final String name;
		private final Tag<Block> blocks;
		private final List<ItemStack> tools = Lists.newArrayList();

		public TestCase(ItemStack stack, String name, @Nullable Tag<Block> blocks) {
			this.stack = stack;
			this.name = name;
			this.blocks = blocks;
			addTool(stack);
		}

		public TestCase addTool(Item tool) {
			return addTool(new ItemStack(tool));
		}

		public TestCase addTool(ItemStack stack) {
			if (!stack.isEmpty())
				tools.add(stack);
			return this;
		}
	}

	public static void init() {
		//		NonNullList<ItemStack> stacks = NonNullList.create();
		//		Set<ToolType> newToolTypes = Sets.newHashSet();
		//		for (Item item : ForgeRegistries.ITEMS.getValues()) {
		//			item.fillItemCategory(CreativeModeTab.TAB_SEARCH, stacks);
		//		}
		//		for (ItemStack stack : stacks) {
		//			if (stack.isEmpty())
		//				continue;
		//			Set<ToolType> toolTypes = stack.getToolTypes();
		//			if (toolTypes == null)
		//				throw new NullPointerException(stack.getItem().getRegistryName() + " getToolTypes returns null, report to their developer!");
		//			for (ToolType toolType : toolTypes) {
		//				if (newToolTypes.contains(toolType)) {
		//					toolTypeMap.get(toolType).addTool(stack);
		//					log(stack, toolType);
		//				} else if (!toolTypeMap.containsKey(toolType)) {
		//					registerTool(ItemStack.EMPTY, toolType.getName(), toolType).addTool(stack);
		//					newToolTypes.add(toolType);
		//					log(stack, toolType);
		//				}
		//			}
	}

	//	private static void log(ItemStack stack, ToolType toolType) {
	//		if (Waila.CONFIG.get().getGeneral().isDebug())
	//			Waila.LOGGER.info("Add tool: {} {} {}", stack, toolType.getName(), stack.getHarvestLevel(toolType, null, null));
	//	}

}
