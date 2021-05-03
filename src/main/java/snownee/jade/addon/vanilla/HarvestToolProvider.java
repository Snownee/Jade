package snownee.jade.addon.vanilla;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElement.Align;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.impl.ui.SubStringElement;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.Items;
import net.minecraft.item.TieredItem;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import snownee.jade.VanillaPlugin;

public class HarvestToolProvider implements IComponentProvider, ISelectiveResourceReloadListener {

	public static final HarvestToolProvider INSTANCE = new HarvestToolProvider();

	public static final Cache<TestCase, String> toolNames = CacheBuilder.newBuilder().build();
	public static final Cache<BlockState, TestCase> resultCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
	public static final List<TestCase> testTools = Lists.newLinkedList();
	public static final Map<ToolType, TestCase> toolTypeMap = Maps.newHashMap();

	public static final TestCase NO_TOOL = new TestCase(ItemStack.EMPTY, "no_tool", null);
	public static final TestCase UNBREAKABLE = new TestCase(ItemStack.EMPTY, "unbreakable", null);

	private static final ITextComponent UNBREAKABLE_TEXT = new TranslationTextComponent("jade.harvest_tool.unbreakable").mergeStyle(TextFormatting.DARK_RED);
	private static final Vector2f ITEM_SIZE = new Vector2f(13, 0);

	static {
		/* off */
        registerTool(new ItemStack(Items.WOODEN_PICKAXE), "pickaxe", ToolType.PICKAXE)
            .addTool(Items.STONE_PICKAXE)
            .addTool(Items.IRON_PICKAXE)
            .addTool(Items.DIAMOND_PICKAXE)
            .addTool(Items.NETHERITE_PICKAXE);
        registerTool(new ItemStack(Items.WOODEN_AXE), "axe", ToolType.AXE);
        registerTool(new ItemStack(Items.WOODEN_SHOVEL), "shovel", ToolType.SHOVEL);
        registerTool(new ItemStack(Items.WOODEN_HOE), "hoe", ToolType.HOE);
        registerTool(new ItemStack(Items.SHEARS), "shears", null);
        /* on */
	}

	public static String getToolName(TestCase testCase) {
		try {
			return toolNames.get(testCase, () -> {
				if (I18n.hasKey("jade.harvest_tool." + testCase.name)) {
					return I18n.format("jade.harvest_tool." + testCase.name);
				} else {
					return StringUtils.capitalize(testCase.name);
				}
			});
		} catch (ExecutionException e) {
			Waila.LOGGER.catching(e);
			return testCase.name;
		}
	}

	@Nullable
	public static TestCase getTool(BlockState state, World world, BlockPos pos) {
		ToolType toolType = state.getHarvestTool();
		if (toolType != null) {
			TestCase testCase = toolTypeMap.get(toolType);
			if (testCase == null) {
				testCase = new TestCase(ItemStack.EMPTY, toolType.getName(), toolType);
				toolTypeMap.put(toolType, testCase);
			}
			return testCase;
		}
		if (state.getRequiresTool()) {
			for (TestCase testCase : testTools) {
				if (testCase.stack.isEmpty()) {
					continue;
				}
				if (testCase.stack.canHarvestBlock(state)) {
					return testCase;
				}
			}
		}
		return NO_TOOL;
	}

	public static synchronized TestCase registerTool(ItemStack stack, String name, @Nullable ToolType toolType) {
		TestCase testCase = new TestCase(stack, name, toolType);
		testTools.add(testCase);
		if (toolType != null) {
			toolTypeMap.put(toolType, testCase);
		}
		return testCase;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		PlayerEntity player = accessor.getPlayer();
		if (player.isCreative() || player.isSpectator()) {
			return;
		}
		BlockState state = accessor.getBlockState();
		float hardness = state.getBlockHardness(accessor.getWorld(), accessor.getPosition());
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

	public List<IElement> getText(BlockAccessor accessor, IPluginConfig config, IElementHelper helper) {
		if (!config.get(VanillaPlugin.HARVEST_TOOL)) {
			return Collections.EMPTY_LIST;
		}
		BlockState state = accessor.getBlockState();
		TestCase testCase = NO_TOOL;
		resultCache.invalidateAll();
		try {
			testCase = resultCache.get(state, () -> getTool(state, accessor.getWorld(), accessor.getPosition()));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (testCase == NO_TOOL || testCase == UNBREAKABLE) {
			return Collections.EMPTY_LIST;
		}
		if (!state.getRequiresTool() && !config.get(VanillaPlugin.EFFECTIVE_TOOL)) {
			return Collections.EMPTY_LIST;
		}
		List<IElement> elements = Lists.newArrayList();
		int level = state.getHarvestLevel();
		String name = "";
		ItemStack tool = testCase.toolMap.get(level);
		if (tool == null) {
			tool = testCase.stack;
			if (level > 0) {
				name = " " + level;
			}
		}
		IElement item = helper.item(tool, 0.75f);
		int offsetY = 0;
		if (!config.get(VanillaPlugin.HARVEST_TOOL_NEW_LINE)) {
			offsetY = -3;
			item.translate(new Vector2f(-1, offsetY)).size(ITEM_SIZE);
		}
		elements.add(item);

		boolean canHarvest = ForgeHooks.canHarvestBlock(state, accessor.getPlayer(), accessor.getWorld(), accessor.getPosition());
		if (state.getRequiresTool()) {
			String sub = canHarvest ? "§a✔" : "§4✕";
			elements.add(new SubStringElement(sub).translate(new Vector2f(-6, 7 + offsetY)));
		} else {
			ItemStack held = accessor.getPlayer().getHeldItemMainhand();
			if (canHarvest && ForgeHooks.isToolEffective(accessor.getWorld(), accessor.getPosition(), held)) {
				elements.add(new SubStringElement("§a✔").translate(new Vector2f(-6, 7 + offsetY)));
			}
		}

		if (!name.isEmpty()) {
			elements.add(helper.text(new StringTextComponent(name)).translate(new Vector2f(3, offsetY + 3)));
		}
		return elements;
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
		if (resourcePredicate.test(VanillaResourceType.LANGUAGES)) {
			toolNames.invalidateAll();
			resultCache.invalidateAll();
		}
	}

	public static class TestCase {
		private ItemStack stack;
		private final String name;
		private final ToolType toolType;
		private final Int2ObjectMap<ItemStack> toolMap = new Int2ObjectOpenHashMap<>();

		public TestCase(ItemStack stack, String name, @Nullable ToolType toolType) {
			this.stack = stack;
			this.name = name;
			this.toolType = toolType;
			addTool(stack);
		}

		public TestCase addTool(Item tool) {
			return addTool(new ItemStack(tool));
		}

		public TestCase addTool(ItemStack stack) {
			if (stack.isEmpty())
				return this;
			int level = 0;
			if (toolType != null) {
				level = stack.getHarvestLevel(toolType, null, null);
			}
			if (this.stack.isEmpty() || level == 0) {
				this.stack = stack;
			}
			ItemStack oldTool = toolMap.get(level);
			if (oldTool != null) {
				IItemTier tier = null;
				if (oldTool.getItem() instanceof TieredItem) {
					tier = ((TieredItem) oldTool.getItem()).getTier();
				}
				if (tier != ItemTier.GOLD) {
					return this;
				}
			}
			toolMap.put(level, stack);
			return this;
		}
	}

	public static void init() {
		NonNullList<ItemStack> stacks = NonNullList.create();
		Set<ToolType> newToolTypes = Sets.newHashSet();
		for (Item item : ForgeRegistries.ITEMS.getValues()) {
			item.fillItemGroup(ItemGroup.SEARCH, stacks);
		}
		for (ItemStack stack : stacks) {
			for (ToolType toolType : stack.getToolTypes()) {
				if (newToolTypes.contains(toolType)) {
					toolTypeMap.get(toolType).addTool(stack);
					log(stack, toolType);
				} else if (!toolTypeMap.containsKey(toolType)) {
					registerTool(ItemStack.EMPTY, toolType.getName(), toolType).addTool(stack);
					newToolTypes.add(toolType);
					log(stack, toolType);
				}
			}
		}
	}

	private static void log(ItemStack stack, ToolType toolType) {
		if (Waila.CONFIG.get().getGeneral().isDebug())
			Waila.LOGGER.info("Add tool: {} {} {}", stack, toolType.getName(), stack.getHarvestLevel(toolType, null, null));
	}

}
