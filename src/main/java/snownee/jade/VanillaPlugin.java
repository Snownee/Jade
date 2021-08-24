package snownee.jade;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.jade.addon.forge.ForgeCapabilityProvider;
import snownee.jade.addon.forge.InventoryProvider;
import snownee.jade.addon.vanilla.AgableMobProvider;
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

	public static IRegistrar registrar;

	@Override
	public void register(IRegistrar registrar) {
		registrar.registerComponentProvider(BrewingStandProvider.INSTANCE, TooltipPosition.BODY, BrewingStandBlock.class);
		registrar.registerBlockDataProvider(BrewingStandProvider.INSTANCE, BrewingStandBlockEntity.class);
		registrar.addConfig(BREWING_STAND, true);

		registrar.registerComponentProvider(HorseProvider.INSTANCE, TooltipPosition.BODY, AbstractHorse.class);
		registrar.addConfig(HORSE_STAT, true);

		registrar.registerComponentProvider(ChestedHorseProvider.INSTANCE, TooltipPosition.BODY, AbstractChestedHorse.class);
		registrar.registerEntityDataProvider(ChestedHorseProvider.INSTANCE, AbstractChestedHorse.class);
		registrar.addConfig(HORSE_INVENTORY, true);

		registrar.registerComponentProvider(ItemFrameProvider.INSTANCE, TooltipPosition.BODY, ItemFrame.class);
		registrar.addConfig(ITEM_FRAME, true);

		registrar.registerComponentProvider(PotionEffectsProvider.INSTANCE, TooltipPosition.BODY, LivingEntity.class);
		registrar.registerEntityDataProvider(PotionEffectsProvider.INSTANCE, LivingEntity.class);
		registrar.addConfig(EFFECTS, true);

		registrar.registerComponentProvider(AgableMobProvider.INSTANCE, TooltipPosition.BODY, AgeableMob.class);
		registrar.registerEntityDataProvider(AgableMobProvider.INSTANCE, AgeableMob.class);
		registrar.addConfig(MOB_GROWTH, true);

		registrar.registerComponentProvider(BreedingProvider.INSTANCE, TooltipPosition.BODY, Animal.class);
		registrar.registerEntityDataProvider(BreedingProvider.INSTANCE, Animal.class);
		registrar.addConfig(MOB_BREEDING, true);

		registrar.registerComponentProvider(TNTProvider.INSTANCE, TooltipPosition.BODY, TntBlock.class);
		registrar.addConfig(TNT_STABILITY, true);

		registrar.registerComponentProvider(BeehiveProvider.INSTANCE, TooltipPosition.BODY, BeehiveBlock.class);
		registrar.registerBlockDataProvider(BeehiveProvider.INSTANCE, BeehiveBlockEntity.class);
		registrar.addConfig(BEEHIVE, true);

		registrar.registerComponentProvider(NoteBlockProvider.INSTANCE, TooltipPosition.BODY, NoteBlock.class);
		registrar.addConfig(NOTE_BLOCK, true);

		registrar.registerComponentProvider(ArmorStandProvider.INSTANCE, TooltipPosition.BODY, ArmorStand.class);
		registrar.addConfig(ARMOR_STAND, true);

		registrar.registerComponentProvider(PaintingProvider.INSTANCE, TooltipPosition.BODY, Painting.class);
		registrar.addConfig(PAINTING, true);

		registrar.registerComponentProvider(ChickenEggProvider.INSTANCE, TooltipPosition.BODY, Chicken.class);
		registrar.registerEntityDataProvider(ChickenEggProvider.INSTANCE, Chicken.class);
		registrar.addConfig(CHICKEN_EGG, true);

		registrar.registerComponentProvider(HarvestToolProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.registerComponentProvider(HarvestToolProvider.INSTANCE, TooltipPosition.TAIL, Block.class);
		registrar.addConfig(HARVEST_TOOL, true);
		registrar.addConfig(HARVEST_TOOL_NEW_LINE, false);
		registrar.addConfig(EFFECTIVE_TOOL, true);

		registrar.registerComponentProvider(CommandBlockProvider.INSTANCE, TooltipPosition.BODY, CommandBlock.class);
		registrar.registerBlockDataProvider(CommandBlockProvider.INSTANCE, CommandBlockEntity.class);
		registrar.addSyncedConfig(COMMAND_BLOCK, true);

		registrar.addConfig(BREAKING_PROGRESS, true);

		registrar.registerComponentProvider(EnchantmentPowerProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.addConfig(ENCH_POWER, true);
		registrar.addConfig(TOTAL_ENCH_POWER, true);

		registrar.registerComponentProvider(PlayerHeadProvider.INSTANCE, TooltipPosition.HEAD, AbstractSkullBlock.class);
		registrar.addConfig(PLAYER_HEAD, true);

		registrar.registerComponentProvider(VillagerProfessionProvider.INSTANCE, TooltipPosition.BODY, Villager.class);
		registrar.registerComponentProvider(VillagerProfessionProvider.INSTANCE, TooltipPosition.BODY, ZombieVillager.class);
		registrar.addConfig(PROFESSION, true);

		registrar.registerComponentProvider(ItemTooltipProvider.INSTANCE, TooltipPosition.BODY, ItemEntity.class);
		registrar.addConfig(ITEM_TOOLTIP, true);

		registrar.addConfig(FURNACE, true);
		registrar.addConfig(SPAWNER_TYPE, true);
		registrar.addConfig(CROP_PROGRESS, true);
		registrar.addConfig(REDSTONE, true);
		registrar.addConfig(JUKEBOX, true);
		registrar.addConfig(LECTERN, true);

		registrar.registerIconProvider(VanillaProvider.INSTANCE, CropBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.HEAD, SpawnerBlock.class);
		registrar.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.registerBlockDataProvider(VanillaProvider.INSTANCE, JukeboxBlockEntity.class);
		registrar.registerBlockDataProvider(VanillaProvider.INSTANCE, LecternBlockEntity.class);

		registrar.registerComponentProvider(FurnaceProvider.INSTANCE, TooltipPosition.BODY, AbstractFurnaceBlock.class);
		registrar.registerBlockDataProvider(FurnaceProvider.INSTANCE, AbstractFurnaceBlockEntity.class);

		registrar.registerComponentProvider(InventoryProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.registerBlockDataProvider(InventoryProvider.INSTANCE, BaseContainerBlockEntity.class);
		registrar.addConfig(INVENTORY, true);

		registrar.registerComponentProvider(ForgeCapabilityProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.registerBlockDataProvider(ForgeCapabilityProvider.INSTANCE, BlockEntity.class);
		registrar.addConfig(FORGE_ENERGY, true);
		registrar.addConfig(FORGE_FLUID, true);

		if (FMLEnvironment.dist.isClient()) {
			((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(HarvestToolProvider.INSTANCE);
			HarvestToolProvider.init();
		}
	}

}
