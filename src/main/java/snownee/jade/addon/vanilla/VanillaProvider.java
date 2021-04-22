package snownee.jade.addon.vanilla;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.overlay.element.ItemStackElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.VanillaPlugin;

public class VanillaProvider implements IComponentProvider, IServerDataProvider<TileEntity> {

	public static final VanillaProvider INSTANCE = new VanillaProvider();

	static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation(Waila.MODID, "object_name");

	@Override
	public IElement getIcon(IBlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
		if (config.get(VanillaPlugin.HIDE_SILVERFISH) && accessor.getBlock() instanceof SilverfishBlock)
			return ItemStackElement.of(new ItemStack(((SilverfishBlock) accessor.getBlock()).getMimickedBlock().asItem()));

		if (accessor.getBlock() == Blocks.WHEAT)
			return ItemStackElement.of(new ItemStack(Items.WHEAT));

		if (accessor.getBlock() == Blocks.BEETROOTS)
			return ItemStackElement.of(new ItemStack(Items.BEETROOT));

		return null;
	}

	public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		if (config.get(VanillaPlugin.HIDE_SILVERFISH) && accessor.getBlock() instanceof SilverfishBlock) {
			tooltip.remove(OBJECT_NAME_TAG);
			Block block = ((SilverfishBlock) accessor.getBlock()).getMimickedBlock();
			tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getBlockName(), I18n.format(block.getTranslationKey()))), OBJECT_NAME_TAG);
		}

		if (accessor.getBlock() == Blocks.SPAWNER && config.get(VanillaPlugin.SPAWNER_TYPE)) {
			MobSpawnerTileEntity spawner = (MobSpawnerTileEntity) accessor.getTileEntity();
			String name = I18n.format(accessor.getBlock().getTranslationKey());
			name = I18n.format("jade.spawner", name, spawner.getSpawnerBaseLogic().getCachedEntity().getDisplayName().getString());
			name = String.format(Waila.CONFIG.get().getFormatting().getBlockName(), name);
			tooltip.remove(OBJECT_NAME_TAG);
			tooltip.add(new StringTextComponent(name), OBJECT_NAME_TAG);
		}
	}

	@Override
	public void append(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		if (accessor.getTooltipPosition() == TooltipPosition.HEAD) {
			appendHead(tooltip, accessor, config);
			return;
		}

		BlockState state = accessor.getBlockState();
		Block block = state.getBlock();

		if (config.get(VanillaPlugin.CROP_PROGRESS)) {
			if (block instanceof CropsBlock) {
				CropsBlock crop = (CropsBlock) block;
				addMaturityTooltip(tooltip, state.get(crop.getAgeProperty()) / (float) crop.getMaxAge());
			} else if (state.hasProperty(BlockStateProperties.AGE_0_7)) {
				addMaturityTooltip(tooltip, state.get(BlockStateProperties.AGE_0_7) / 7F);
			} else if (state.hasProperty(BlockStateProperties.AGE_0_2)) {
				addMaturityTooltip(tooltip, state.get(BlockStateProperties.AGE_0_2) / 2.0F);
			}
		}

		if (config.get(VanillaPlugin.REDSTONE)) {
			appendRedstone(tooltip, state);
		}

		if (config.get(VanillaPlugin.JUKEBOX) && block instanceof JukeboxBlock) {
			if (state.get(JukeboxBlock.HAS_RECORD) && accessor.getServerData().contains("record")) {
				try {
					Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(accessor.getServerData().getString("record")));
					if (item instanceof MusicDiscItem) {
						tooltip.add(new TranslationTextComponent("record.nowPlaying", ((MusicDiscItem) item).getDescription()));
					}
				} catch (Exception e) {
					Waila.LOGGER.catching(e);
				}
			} else
				tooltip.add(new TranslationTextComponent("tooltip.waila.empty"));
		}

		if (config.get(VanillaPlugin.LECTERN) && block instanceof LecternBlock) {
			if (state.get(LecternBlock.HAS_BOOK) && accessor.getServerData().contains("book")) {
				ItemStack stack = ItemStack.read(accessor.getServerData().getCompound("book"));
				if (!stack.isEmpty()) {
					IElementHelper helper = tooltip.getElementHelper();
					tooltip.add(helper.item(stack, 0.75f));
					tooltip.append(helper.text(stack.getDisplayName()).translate(3, 3));
				}
			}
		}
	}

	private void appendRedstone(ITooltip tooltip, BlockState state) {
		Block block = state.getBlock();
		if (block instanceof LeverBlock) {
			boolean active = state.get(BlockStateProperties.POWERED);
			tooltip.add(new TranslationTextComponent("tooltip.waila.state", new TranslationTextComponent("tooltip.waila.state_" + (active ? "on" : "off"))));
			return;
		}

		if (block == Blocks.REPEATER) {
			int delay = state.get(BlockStateProperties.DELAY_1_4);
			tooltip.add(new TranslationTextComponent("tooltip.waila.delay", TextFormatting.WHITE.toString() + delay));
			return;
		}

		if (block == Blocks.COMPARATOR) {
			ComparatorMode mode = state.get(BlockStateProperties.COMPARATOR_MODE);
			tooltip.add(new TranslationTextComponent("tooltip.waila.mode", new TranslationTextComponent("tooltip.waila.mode_" + (mode == ComparatorMode.COMPARE ? "comparator" : "subtractor"))));
			return;
		}

		if (state.hasProperty(BlockStateProperties.POWER_0_15)) {
			tooltip.add(new TranslationTextComponent("tooltip.waila.power", TextFormatting.WHITE.toString() + state.get(BlockStateProperties.POWER_0_15)));
			return;
		}
	}

	@Override
	public void appendServerData(CompoundNBT data, ServerPlayerEntity player, World world, TileEntity blockEntity) {
		if (blockEntity instanceof JukeboxTileEntity) {
			JukeboxTileEntity jukebox = (JukeboxTileEntity) blockEntity;
			ItemStack stack = jukebox.getRecord();
			if (!stack.isEmpty()) {
				data.putString("record", stack.getItem().getRegistryName().toString());
			}
		}

		if (blockEntity instanceof LecternTileEntity) {
			LecternTileEntity lectern = (LecternTileEntity) blockEntity;
			ItemStack stack = lectern.getBook();
			if (!stack.isEmpty()) {
				if (stack.hasDisplayName() || stack.getItem() != Items.WRITABLE_BOOK) {
					data.put("book", stack.serializeNBT());
				}
			}
		}
	}

	private static void addMaturityTooltip(ITooltip tooltip, float growthValue) {
		growthValue *= 100.0F;
		if (growthValue < 100.0F)
			tooltip.add(new TranslationTextComponent("tooltip.waila.crop_growth", TextFormatting.WHITE + String.format("%.0f%%", growthValue)));
		else
			tooltip.add(new TranslationTextComponent("tooltip.waila.crop_growth", new TranslationTextComponent("tooltip.waila.crop_mature").mergeStyle(TextFormatting.GREEN)));
	}
}
