package snownee.jade.addon.harvest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import snownee.jade.Jade;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElement.Align;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;

public class HarvestToolProvider implements IBlockComponentProvider, ResourceManagerReloadListener {

	public static final HarvestToolProvider INSTANCE = new HarvestToolProvider();

	public final Cache<BlockState, ImmutableList<ItemStack>> resultCache = CacheBuilder.newBuilder().expireAfterAccess(
			5,
			TimeUnit.MINUTES).build();
	private List<Block> shearableBlocks = List.of();
	public static final Map<ResourceLocation, ToolHandler> TOOL_HANDLERS = Maps.newLinkedHashMap();
	private static final Component CHECK = Component.literal("✔");
	private static final Component X = Component.literal("✕");
	private static final Vec2 ITEM_SIZE = new Vec2(10, 0);

	static {
		CommonProxy.registerTagsUpdatedListener(HarvestToolProvider.INSTANCE::tagsUpdated);
		if (CommonProxy.isPhysicallyClient()) {
			registerHandler(SimpleToolHandler.create(
					JadeIds.JADE("pickaxe"),
					List.of(
							Items.WOODEN_PICKAXE,
							Items.STONE_PICKAXE,
							Items.IRON_PICKAXE,
							Items.DIAMOND_PICKAXE,
							Items.NETHERITE_PICKAXE)));
			registerHandler(SimpleToolHandler.create(
					JadeIds.JADE("axe"),
					List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE)));
			registerHandler(SimpleToolHandler.create(
					JadeIds.JADE("shovel"),
					List.of(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL)));
			registerHandler(SimpleToolHandler.create(
					JadeIds.JADE("hoe"),
					List.of(Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE)));
			registerHandler(SimpleToolHandler.create(JadeIds.JADE("sword"), List.of(Items.WOODEN_SWORD))
					.addExtraBlock(Blocks.BAMBOO)
					.addExtraBlock(Blocks.BAMBOO_SAPLING));
			registerHandler(ShearsToolHandler.getInstance());
		}
	}

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
		TOOL_HANDLERS.put(handler.getUid(), handler);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Player player = accessor.getPlayer();
		if (!config.get(JadeIds.MC_HARVEST_TOOL_CREATIVE) && (player.isCreative() || player.isSpectator())) {
			return;
		}
		Level level = accessor.getLevel();
		BlockPos pos = accessor.getPosition();
		GameType gameType = ClientProxy.getGameMode();
		if (gameType == GameType.ADVENTURE && player.blockActionRestricted(level, pos, gameType)) {
			return;
		}
		BlockState state = accessor.getBlockState();
		float destroySpeed = state.getDestroySpeed(level, pos);
		// player-sensitive method, used by Waystones
		float destroyProgress = state.getDestroyProgress(player, level, pos);
		if (destroySpeed < 0 || destroyProgress <= 0) {
			if (config.get(JadeIds.MC_SHOW_UNBREAKABLE)) {
				Component text = IThemeHelper.get().failure(Component.translatable("jade.harvest_tool.unbreakable"));
				tooltip.add(IElementHelper.get().text(text).message(null));
			}
			//TODO: high priority handlers?
			return;
		}

		boolean newLine = config.get(JadeIds.MC_HARVEST_TOOL_NEW_LINE);
		List<IElement> elements = getText(accessor, config);
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

	public List<IElement> getText(BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		if (!state.requiresCorrectToolForDrops() && !config.get(JadeIds.MC_EFFECTIVE_TOOL)) {
			return List.of();
		}
		List<ItemStack> tools = List.of();
		try {
			tools = resultCache.get(state, () -> getTool(state, accessor.getLevel(), accessor.getPosition()));
		} catch (ExecutionException e) {
			Jade.LOGGER.error("Failed to get harvest tool", e);
		}
		if (tools.isEmpty()) {
			return List.of();
		}

		int offsetY = -3;
		boolean newLine = config.get(JadeIds.MC_HARVEST_TOOL_NEW_LINE);
		List<IElement> elements = Lists.newArrayList();
		for (ItemStack tool : tools) {
			elements.add(IElementHelper.get().item(tool, 0.75f).translate(new Vec2(-1, offsetY)).size(ITEM_SIZE).message(null));
		}

		if (!elements.isEmpty()) {
			elements.addFirst(IElementHelper.get().spacer(newLine ? -2 : 5, newLine ? 10 : 0));
			Player player = accessor.getPlayer();
			boolean canHarvest = CommonProxy.isCorrectToolForDrops(state, player, accessor.getLevel(), accessor.getPosition());
			if (state.requiresCorrectToolForDrops() || !canHarvest) {
				IThemeHelper t = IThemeHelper.get();
				Component text = canHarvest ? t.success(CHECK) : t.danger(X);
				elements.add(IElementHelper.get().text(text)
						.scale(0.75F)
						.zOffset(800)
						.size(Vec2.ZERO)
						.translate(new Vec2(-3, 6.25F + offsetY))
				);
			}
		}

		return elements;
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		resultCache.invalidateAll();
	}

	private void tagsUpdated(RegistryAccess registryAccess, boolean client) {
		if (client) {
			resultCache.invalidateAll();
		} else {
			//TODO execute on a thread?
			try {
				shearableBlocks = Collections.unmodifiableList(LootTableMineableCollector.execute(
						registryAccess.lookupOrThrow(Registries.LOOT_TABLE),
						Items.SHEARS.getDefaultInstance()));
			} catch (Throwable e) {
				Jade.LOGGER.error("Failed to collect shearable blocks", e);
			}
		}
	}

	public List<Block> getShearableBlocks() {
		return shearableBlocks;
	}

	public void setShearableBlocks(Collection<Block> blocks) {
		if (TOOL_HANDLERS.get(JadeIds.JADE("shears")) instanceof ShearsToolHandler handler) {
			handler.setShearableBlocks(blocks);
		}
	}

	@Override
	public ResourceLocation getUid() {
		return JadeIds.MC_HARVEST_TOOL;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.HEAD + 2000;
	}

}
