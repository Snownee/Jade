package snownee.jade;

import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.ui.IDisplayHelper;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
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
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import snownee.jade.addon.forge.ForgeCapabilityProvider;
import snownee.jade.addon.forge.InventoryProvider;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.addon.vanilla.AgableMobProvider;
import snownee.jade.addon.vanilla.AnimalOwnerProvider;
import snownee.jade.addon.vanilla.ArmorStandProvider;
import snownee.jade.addon.vanilla.BeehiveProvider;
import snownee.jade.addon.vanilla.BreedingProvider;
import snownee.jade.addon.vanilla.BrewingStandProvider;
import snownee.jade.addon.vanilla.ChestedHorseProvider;
import snownee.jade.addon.vanilla.ChickenEggProvider;
import snownee.jade.addon.vanilla.CommandBlockProvider;
import snownee.jade.addon.vanilla.EnchantmentPowerProvider;
import snownee.jade.addon.vanilla.FallingBlockProvider;
import snownee.jade.addon.vanilla.FurnaceProvider;
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
	public static final ResourceLocation ANIMAL_OWNER = MC("animal_owner");

	public static final ResourceLocation INVENTORY = MC("inventory");
	public static final ResourceLocation FORGE_ENERGY = MC("fe");
	public static final ResourceLocation FORGE_FLUID = MC("fluid");

	public static IWailaClientRegistration CLIENT_REGISTRATION;

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(BrewingStandProvider.INSTANCE, BrewingStandBlockEntity.class);
		registration.registerBlockDataProvider(BeehiveProvider.INSTANCE, BeehiveBlockEntity.class);
		registration.registerBlockDataProvider(CommandBlockProvider.INSTANCE, CommandBlockEntity.class);
		registration.registerBlockDataProvider(VanillaProvider.INSTANCE, JukeboxBlockEntity.class);
		registration.registerBlockDataProvider(VanillaProvider.INSTANCE, LecternBlockEntity.class);
		registration.registerBlockDataProvider(VanillaProvider.INSTANCE, ComparatorBlockEntity.class);
		registration.registerBlockDataProvider(FurnaceProvider.INSTANCE, AbstractFurnaceBlockEntity.class);
		registration.registerBlockDataProvider(InventoryProvider.INSTANCE, BlockEntity.class);
		registration.registerBlockDataProvider(ForgeCapabilityProvider.INSTANCE, BlockEntity.class);

		registration.registerEntityDataProvider(ChestedHorseProvider.INSTANCE, AbstractChestedHorse.class);
		registration.registerEntityDataProvider(PotionEffectsProvider.INSTANCE, LivingEntity.class);
		registration.registerEntityDataProvider(AgableMobProvider.INSTANCE, AgeableMob.class);
		registration.registerEntityDataProvider(BreedingProvider.INSTANCE, Animal.class);
		registration.registerEntityDataProvider(ChickenEggProvider.INSTANCE, Chicken.class);

		registration.addConfig(BREWING_STAND, true);
		registration.addConfig(HORSE_STAT, true);
		registration.addConfig(HORSE_INVENTORY, true);
		registration.addConfig(ITEM_FRAME, true);
		registration.addConfig(EFFECTS, true);
		registration.addConfig(MOB_GROWTH, true);
		registration.addConfig(MOB_BREEDING, true);
		registration.addConfig(TNT_STABILITY, true);
		registration.addConfig(BEEHIVE, true);
		registration.addConfig(NOTE_BLOCK, true);
		registration.addConfig(ARMOR_STAND, true);
		registration.addConfig(PAINTING, true);
		registration.addConfig(CHICKEN_EGG, true);
		registration.addConfig(HARVEST_TOOL, true);
		registration.addConfig(HARVEST_TOOL_NEW_LINE, false);
		registration.addConfig(EFFECTIVE_TOOL, true);
		registration.addConfig(BREAKING_PROGRESS, true);
		registration.addConfig(ENCH_POWER, true);
		registration.addConfig(TOTAL_ENCH_POWER, true);
		registration.addConfig(PLAYER_HEAD, true);
		registration.addConfig(PROFESSION, true);
		registration.addConfig(ITEM_TOOLTIP, true);
		registration.addConfig(FURNACE, true);
		registration.addConfig(SPAWNER_TYPE, true);
		registration.addConfig(CROP_PROGRESS, true);
		registration.addConfig(REDSTONE, true);
		registration.addConfig(JUKEBOX, true);
		registration.addConfig(LECTERN, true);
		registration.addConfig(INVENTORY, true);
		registration.addConfig(FORGE_ENERGY, true);
		registration.addConfig(FORGE_FLUID, true);
		registration.addConfig(ANIMAL_OWNER, false);

		registration.addSyncedConfig(COMMAND_BLOCK, true);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		CLIENT_REGISTRATION = registration;

		registration.registerComponentProvider(BrewingStandProvider.INSTANCE, TooltipPosition.BODY, BrewingStandBlock.class);
		registration.registerComponentProvider(HorseProvider.INSTANCE, TooltipPosition.BODY, AbstractHorse.class);
		registration.registerComponentProvider(ChestedHorseProvider.INSTANCE, TooltipPosition.BODY, AbstractChestedHorse.class);
		registration.registerComponentProvider(ItemFrameProvider.INSTANCE, TooltipPosition.BODY, ItemFrame.class);
		registration.registerComponentProvider(PotionEffectsProvider.INSTANCE, TooltipPosition.BODY, LivingEntity.class);
		registration.registerComponentProvider(AgableMobProvider.INSTANCE, TooltipPosition.BODY, AgeableMob.class);
		registration.registerComponentProvider(BreedingProvider.INSTANCE, TooltipPosition.BODY, Animal.class);
		registration.registerComponentProvider(TNTProvider.INSTANCE, TooltipPosition.BODY, TntBlock.class);
		registration.registerComponentProvider(BeehiveProvider.INSTANCE, TooltipPosition.BODY, BeehiveBlock.class);
		registration.registerComponentProvider(NoteBlockProvider.INSTANCE, TooltipPosition.BODY, NoteBlock.class);
		registration.registerComponentProvider(ArmorStandProvider.INSTANCE, TooltipPosition.BODY, ArmorStand.class);
		registration.registerComponentProvider(PaintingProvider.INSTANCE, TooltipPosition.BODY, Painting.class);
		registration.registerComponentProvider(ChickenEggProvider.INSTANCE, TooltipPosition.BODY, Chicken.class);
		registration.registerComponentProvider(HarvestToolProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registration.registerComponentProvider(HarvestToolProvider.INSTANCE, TooltipPosition.TAIL, Block.class);
		registration.registerComponentProvider(CommandBlockProvider.INSTANCE, TooltipPosition.BODY, CommandBlock.class);
		registration.registerComponentProvider(EnchantmentPowerProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registration.registerComponentProvider(PlayerHeadProvider.INSTANCE, TooltipPosition.HEAD, AbstractSkullBlock.class);
		registration.registerComponentProvider(VillagerProfessionProvider.INSTANCE, TooltipPosition.BODY, Villager.class);
		registration.registerComponentProvider(VillagerProfessionProvider.INSTANCE, TooltipPosition.BODY, ZombieVillager.class);
		registration.registerComponentProvider(ItemTooltipProvider.INSTANCE, TooltipPosition.BODY, ItemEntity.class);
		registration.registerIconProvider(VanillaProvider.INSTANCE, CropBlock.class);
		registration.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.HEAD, SpawnerBlock.class);
		registration.registerComponentProvider(VanillaProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registration.registerComponentProvider(FurnaceProvider.INSTANCE, TooltipPosition.BODY, AbstractFurnaceBlock.class);
		registration.registerComponentProvider(InventoryProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registration.registerComponentProvider(ForgeCapabilityProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registration.registerComponentProvider(AnimalOwnerProvider.INSTANCE, TooltipPosition.BODY, Entity.class);
		registration.registerComponentProvider(FallingBlockProvider.INSTANCE, TooltipPosition.HEAD, FallingBlockEntity.class);
		registration.registerIconProvider(FallingBlockProvider.INSTANCE, FallingBlockEntity.class);

		((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(HarvestToolProvider.INSTANCE);
	}

	public static IDisplayHelper getDisplayHelper() {
		return CLIENT_REGISTRATION.getDisplayHelper();
	}

	public static IElementHelper getElementHelper() {
		return CLIENT_REGISTRATION.getElementHelper();
	}

}
