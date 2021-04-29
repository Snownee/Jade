package snownee.jade;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.ui.IDisplayHelper;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.TNTBlock;
import net.minecraft.block.TargetBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.jade.addon.forge.ForgeCapabilityProvider;
import snownee.jade.addon.forge.InventoryProvider;
import snownee.jade.addon.vanilla.AgeableEntityProvider;
import snownee.jade.addon.vanilla.ArmorStandProvider;
import snownee.jade.addon.vanilla.BeehiveProvider;
import snownee.jade.addon.vanilla.BreedingProvider;
import snownee.jade.addon.vanilla.BrewingStandProvider;
import snownee.jade.addon.vanilla.ChestedHorseProvider;
import snownee.jade.addon.vanilla.ChickenEggProvider;
import snownee.jade.addon.vanilla.CommandBlockProvider;
import snownee.jade.addon.vanilla.EnchantmentPowerProvider;
import snownee.jade.addon.vanilla.FurnaceProvider;
import snownee.jade.addon.vanilla.HarvestToolProvider;
import snownee.jade.addon.vanilla.HorseProvider;
import snownee.jade.addon.vanilla.ItemFrameProvider;
import snownee.jade.addon.vanilla.ItemTooltipProvider;
import snownee.jade.addon.vanilla.NoteBlockProvider;
import snownee.jade.addon.vanilla.PaintingProvider;
import snownee.jade.addon.vanilla.PlayerHeadProvider;
import snownee.jade.addon.vanilla.PotionEffectsProvider;
import snownee.jade.addon.vanilla.TNTProvider;
import snownee.jade.addon.vanilla.VanillaProvider;
import snownee.jade.addon.vanilla.VillagerProfessionProvider;

@WailaPlugin
public class VanillaPlugin implements IWailaPlugin {

	private static ResourceLocation MC(String path) {
		return new ResourceLocation(path);
	}

	public static final ResourceLocation BREWING_STAND = MC("brewing_stand");
	public static final ResourceLocation HORSE_STAT = MC("horse_stat");
	public static final ResourceLocation HORSE_INVENTORY = MC("horse_inventory");
	public static final ResourceLocation ITEM_FRAME = MC("item_frame");
	public static final ResourceLocation EFFECTS = MC("effects");
	public static final ResourceLocation MOB_GROWTH = MC("mob_growth");
	public static final ResourceLocation MOB_BREEDING = MC("mob_breeding");
	public static final ResourceLocation TNT_STABILITY = MC("tnt_stability");
	public static final ResourceLocation BEEHIVE = MC("beehive");
	public static final ResourceLocation NOTE_BLOCK = MC("note_block");
	public static final ResourceLocation ARMOR_STAND = MC("armor_stand");
	public static final ResourceLocation PAINTING = MC("painting");
	public static final ResourceLocation CHICKEN_EGG = MC("chicken_egg");
	public static final ResourceLocation HARVEST_TOOL = MC("harvest_tool");
	public static final ResourceLocation HARVEST_TOOL_NEW_LINE = MC("harvest_tool_new_line");
	public static final ResourceLocation EFFECTIVE_TOOL = MC("effective_tool");
	public static final ResourceLocation COMMAND_BLOCK = MC("command_block");
	public static final ResourceLocation BREAKING_PROGRESS = MC("breaking_progress");
	public static final ResourceLocation ENCH_POWER = MC("ench_power");
	public static final ResourceLocation TOTAL_ENCH_POWER = MC("total_ench_power");
	public static final ResourceLocation PLAYER_HEAD = MC("player_head");
	public static final ResourceLocation PROFESSION = MC("profession");
	public static final ResourceLocation ITEM_TOOLTIP = MC("item_tooltip");

	public static final ResourceLocation FURNACE = MC("display_furnace_contents");
	public static final ResourceLocation SPAWNER_TYPE = MC("spawner_type");
	public static final ResourceLocation CROP_PROGRESS = MC("crop_progress");
	public static final ResourceLocation REDSTONE = MC("redstone");
	public static final ResourceLocation JUKEBOX = MC("jukebox");
	public static final ResourceLocation LECTERN = MC("lectern");

	public static final ResourceLocation INVENTORY = MC("inventory");
	public static final ResourceLocation FORGE_ENERGY = MC("fe");
	public static final ResourceLocation FORGE_FLUID = MC("fluid");

	public static IDisplayHelper displayHelper;

	@Override
	public void register(IRegistrar registrar) {
		displayHelper = registrar.getDisplayHelper();

		registrar.registerComponentProvider(BrewingStandProvider.INSTANCE, TooltipPosition.BODY, BrewingStandBlock.class);
		registrar.registerBlockDataProvider(BrewingStandProvider.INSTANCE, BrewingStandTileEntity.class);
		registrar.addConfig(BREWING_STAND, true);

		registrar.registerComponentProvider(HorseProvider.INSTANCE, TooltipPosition.BODY, AbstractHorseEntity.class);
		registrar.addConfig(HORSE_STAT, true);

		registrar.registerComponentProvider(ChestedHorseProvider.INSTANCE, TooltipPosition.BODY, AbstractChestedHorseEntity.class);
		registrar.registerEntityDataProvider(ChestedHorseProvider.INSTANCE, AbstractChestedHorseEntity.class);
		registrar.addConfig(HORSE_INVENTORY, true);

		registrar.registerComponentProvider(ItemFrameProvider.INSTANCE, TooltipPosition.BODY, ItemFrameEntity.class);
		registrar.addConfig(ITEM_FRAME, true);

		registrar.registerComponentProvider(PotionEffectsProvider.INSTANCE, TooltipPosition.BODY, LivingEntity.class);
		registrar.registerEntityDataProvider(PotionEffectsProvider.INSTANCE, LivingEntity.class);
		registrar.addConfig(EFFECTS, true);

		registrar.registerComponentProvider(AgeableEntityProvider.INSTANCE, TooltipPosition.BODY, AgeableEntity.class);
		registrar.registerEntityDataProvider(AgeableEntityProvider.INSTANCE, AgeableEntity.class);
		registrar.addConfig(MOB_GROWTH, true);

		registrar.registerComponentProvider(BreedingProvider.INSTANCE, TooltipPosition.BODY, AnimalEntity.class);
		registrar.registerEntityDataProvider(BreedingProvider.INSTANCE, AnimalEntity.class);
		registrar.addConfig(MOB_BREEDING, true);

		registrar.registerComponentProvider(TNTProvider.INSTANCE, TooltipPosition.BODY, TNTBlock.class);
		registrar.addConfig(TNT_STABILITY, true);

		registrar.registerComponentProvider(BeehiveProvider.INSTANCE, TooltipPosition.BODY, BeehiveBlock.class);
		registrar.registerBlockDataProvider(BeehiveProvider.INSTANCE, BeehiveTileEntity.class);
		registrar.addConfig(BEEHIVE, true);

		registrar.registerComponentProvider(NoteBlockProvider.INSTANCE, TooltipPosition.BODY, NoteBlock.class);
		registrar.addConfig(NOTE_BLOCK, true);

		registrar.registerComponentProvider(ArmorStandProvider.INSTANCE, TooltipPosition.BODY, ArmorStandEntity.class);
		registrar.addConfig(ARMOR_STAND, true);

		registrar.registerComponentProvider(PaintingProvider.INSTANCE, TooltipPosition.BODY, PaintingEntity.class);
		registrar.addConfig(PAINTING, true);

		registrar.registerComponentProvider(ChickenEggProvider.INSTANCE, TooltipPosition.BODY, ChickenEntity.class);
		registrar.registerEntityDataProvider(ChickenEggProvider.INSTANCE, ChickenEntity.class);
		registrar.addConfig(CHICKEN_EGG, true);

		registrar.registerComponentProvider(HarvestToolProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.registerComponentProvider(HarvestToolProvider.INSTANCE, TooltipPosition.TAIL, Block.class);
		registrar.addConfig(HARVEST_TOOL, true);
		registrar.addConfig(HARVEST_TOOL_NEW_LINE, false);
		registrar.addConfig(EFFECTIVE_TOOL, true);

		registrar.registerComponentProvider(CommandBlockProvider.INSTANCE, TooltipPosition.BODY, CommandBlockBlock.class);
		registrar.registerBlockDataProvider(CommandBlockProvider.INSTANCE, CommandBlockTileEntity.class);
		registrar.addSyncedConfig(COMMAND_BLOCK, true);

		registrar.addConfig(BREAKING_PROGRESS, true);

		registrar.registerComponentProvider(EnchantmentPowerProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.addConfig(ENCH_POWER, true);
		registrar.addConfig(TOTAL_ENCH_POWER, true);

		registrar.registerComponentProvider(PlayerHeadProvider.INSTANCE, TooltipPosition.HEAD, AbstractSkullBlock.class);
		registrar.addConfig(PLAYER_HEAD, true);

		registrar.registerComponentProvider(VillagerProfessionProvider.INSTANCE, TooltipPosition.BODY, VillagerEntity.class);
		registrar.registerComponentProvider(VillagerProfessionProvider.INSTANCE, TooltipPosition.BODY, ZombieVillagerEntity.class);
		registrar.addConfig(PROFESSION, true);

		registrar.registerComponentProvider(ItemTooltipProvider.INSTANCE, TooltipPosition.BODY, ItemEntity.class);
		registrar.addConfig(ITEM_TOOLTIP, true);

		registrar.addConfig(FURNACE, true);
		registrar.addConfig(SPAWNER_TYPE, true);
		registrar.addConfig(CROP_PROGRESS, true);
		registrar.addConfig(REDSTONE, true);
		registrar.addConfig(JUKEBOX, true);
		registrar.addConfig(LECTERN, true);

		registrar.registerIconProvider(VanillaProvider.INSTANCE, SilverfishBlock.class);
		registrar.registerIconProvider(VanillaProvider.INSTANCE, CropsBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.HEAD, SilverfishBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.HEAD, SpawnerBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, CropsBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, StemBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, CocoaBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, LeverBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, RepeaterBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, ComparatorBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, RedstoneWireBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, TargetBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, DaylightDetectorBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, RedstoneWireBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, JukeboxBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, LecternBlock.class);
		registrar.registerBlockDataProvider(VanillaProvider.INSTANCE, JukeboxTileEntity.class);
		registrar.registerBlockDataProvider(VanillaProvider.INSTANCE, LecternTileEntity.class);

		registrar.registerComponentProvider(FurnaceProvider.INSTANCE, TooltipPosition.BODY, AbstractFurnaceBlock.class);
		registrar.registerBlockDataProvider(FurnaceProvider.INSTANCE, AbstractFurnaceTileEntity.class);

		registrar.registerComponentProvider(InventoryProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.registerBlockDataProvider(InventoryProvider.INSTANCE, LockableTileEntity.class);
		registrar.addConfig(INVENTORY, true);

		registrar.registerComponentProvider(ForgeCapabilityProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.registerBlockDataProvider(ForgeCapabilityProvider.INSTANCE, TileEntity.class);
		registrar.addConfig(FORGE_ENERGY, true);
		registrar.addConfig(FORGE_FLUID, true);

		if (FMLEnvironment.dist.isClient()) {
			((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(HarvestToolProvider.INSTANCE);
		}
	}

}
