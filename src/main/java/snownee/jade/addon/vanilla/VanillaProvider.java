package snownee.jade.addon.vanilla;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.VanillaPlugin;

public class VanillaProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {

	public static final VanillaProvider INSTANCE = new VanillaProvider();

	static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation(Waila.MODID, "object_name");

	@Override
	@OnlyIn(Dist.CLIENT)
	public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
		if (accessor.getBlock() == Blocks.WHEAT)
			return VanillaPlugin.getElementHelper().item(new ItemStack(Items.WHEAT));

		if (accessor.getBlock() == Blocks.BEETROOTS)
			return VanillaPlugin.getElementHelper().item(new ItemStack(Items.BEETROOT));

		return null;
	}

	@OnlyIn(Dist.CLIENT)
	public void appendHead(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getBlock() == Blocks.SPAWNER && config.get(VanillaPlugin.SPAWNER_TYPE)) {
			SpawnerBlockEntity spawner = (SpawnerBlockEntity) accessor.getBlockEntity();
			String name = I18n.get(accessor.getBlock().getDescriptionId());
			Entity entity = spawner.getSpawner().getOrCreateDisplayEntity(accessor.getLevel());
			if (entity != null) {
				name = I18n.get("jade.spawner", name, entity.getDisplayName().getString());
				name = String.format(config.getWailaConfig().getFormatting().getBlockName(), name);
				tooltip.remove(OBJECT_NAME_TAG);
				tooltip.add(new TextComponent(name).withStyle(config.getWailaConfig().getOverlay().getColor().getTitle()), OBJECT_NAME_TAG);
			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getTooltipPosition() == TooltipPosition.HEAD) {
			appendHead(tooltip, accessor, config);
			return;
		}

		BlockState state = accessor.getBlockState();
		Block block = state.getBlock();

		if (config.get(VanillaPlugin.CROP_PROGRESS)) {
			if (block instanceof CropBlock) {
				CropBlock crop = (CropBlock) block;
				addMaturityTooltip(tooltip, state.getValue(crop.getAgeProperty()) / (float) crop.getMaxAge());
			} else if (state.hasProperty(BlockStateProperties.AGE_7)) {
				addMaturityTooltip(tooltip, state.getValue(BlockStateProperties.AGE_7) / 7F);
			} else if (state.hasProperty(BlockStateProperties.AGE_2)) {
				addMaturityTooltip(tooltip, state.getValue(BlockStateProperties.AGE_2) / 2F);
			} else if (state.hasProperty(BlockStateProperties.AGE_3)) {
				if (block instanceof SweetBerryBushBlock || block instanceof NetherWartBlock) {
					addMaturityTooltip(tooltip, state.getValue(BlockStateProperties.AGE_3) / 3F);
				}
			}
		}

		if (config.get(VanillaPlugin.REDSTONE)) {
			appendRedstone(tooltip, state, accessor.getServerData());
		}

		if (config.get(VanillaPlugin.JUKEBOX) && block instanceof JukeboxBlock) {
			if (state.getValue(JukeboxBlock.HAS_RECORD) && accessor.getServerData().contains("record")) {
				try {
					Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(accessor.getServerData().getString("record")));
					if (item instanceof RecordItem) {
						tooltip.add(new TranslatableComponent("record.nowPlaying", ((RecordItem) item).getDisplayName()));
					}
				} catch (Exception e) {
					Waila.LOGGER.catching(e);
				}
			} else
				tooltip.add(new TranslatableComponent("tooltip.waila.empty"));
		}

		if (config.get(VanillaPlugin.LECTERN) && block instanceof LecternBlock) {
			if (state.getValue(LecternBlock.HAS_BOOK) && accessor.getServerData().contains("book")) {
				ItemStack stack = ItemStack.of(accessor.getServerData().getCompound("book"));
				if (!stack.isEmpty()) {
					IElementHelper helper = tooltip.getElementHelper();
					tooltip.add(helper.item(stack, 0.75f));
					tooltip.append(helper.text(stack.getHoverName()).translate(new Vec2(3, 3)));
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void appendRedstone(ITooltip tooltip, BlockState state, CompoundTag serverData) {
		Block block = state.getBlock();
		if (block instanceof LeverBlock) {
			boolean active = state.getValue(BlockStateProperties.POWERED);
			tooltip.add(new TranslatableComponent("tooltip.waila.state", new TranslatableComponent("tooltip.waila.state_" + (active ? "on" : "off"))));
			return;
		}

		if (block == Blocks.REPEATER) {
			int delay = state.getValue(BlockStateProperties.DELAY);
			tooltip.add(new TranslatableComponent("tooltip.waila.delay", ChatFormatting.WHITE.toString() + delay));
			return;
		}

		if (block == Blocks.COMPARATOR) {
			ComparatorMode mode = state.getValue(BlockStateProperties.MODE_COMPARATOR);
			tooltip.add(new TranslatableComponent("tooltip.waila.mode", new TranslatableComponent("tooltip.waila.mode_" + (mode == ComparatorMode.COMPARE ? "comparator" : "subtractor")).withStyle(ChatFormatting.WHITE)));
			if (serverData.contains("signal")) {
				tooltip.add(new TranslatableComponent("tooltip.waila.power", ChatFormatting.WHITE.toString() + serverData.getInt("signal")));
			}
			return;
		}

		if (state.hasProperty(BlockStateProperties.POWER)) {
			tooltip.add(new TranslatableComponent("tooltip.waila.power", ChatFormatting.WHITE.toString() + state.getValue(BlockStateProperties.POWER)));
		}
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity blockEntity, boolean showDetails) {
		if (blockEntity instanceof JukeboxBlockEntity jukebox) {
			ItemStack stack = jukebox.getRecord();
			if (!stack.isEmpty()) {
				data.putString("record", stack.getItem().getRegistryName().toString());
			}
		}

		if (blockEntity instanceof LecternBlockEntity lectern) {
			ItemStack stack = lectern.getBook();
			if (!stack.isEmpty()) {
				if (stack.hasCustomHoverName() || stack.getItem() != Items.WRITABLE_BOOK) {
					data.put("book", stack.serializeNBT());
				}
			}
		}

		if (blockEntity instanceof ComparatorBlockEntity comparator) {
			data.putInt("signal", comparator.getOutputSignal());
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void addMaturityTooltip(ITooltip tooltip, float growthValue) {
		growthValue *= 100.0F;
		if (growthValue < 100.0F)
			tooltip.add(new TranslatableComponent("tooltip.waila.crop_growth", String.format("%.0f%%", growthValue)));
		else
			tooltip.add(new TranslatableComponent("tooltip.waila.crop_growth", new TranslatableComponent("tooltip.waila.crop_mature").withStyle(ChatFormatting.GREEN)));
	}
}
