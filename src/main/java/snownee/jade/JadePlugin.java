package snownee.jade;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.ResourceLocation;
import snownee.jade.addon.vanilla.AgeableEntityProvider;
import snownee.jade.addon.vanilla.ArmorStandProvider;
import snownee.jade.addon.vanilla.BeehiveProvider;
import snownee.jade.addon.vanilla.BreedingProvider;
import snownee.jade.addon.vanilla.BrewingStandProvider;
import snownee.jade.addon.vanilla.ChestedHorseProvider;
import snownee.jade.addon.vanilla.ChickenEggProvider;
import snownee.jade.addon.vanilla.CommandBlockProvider;
import snownee.jade.addon.vanilla.EnchantmentPowerProvider;
import snownee.jade.addon.vanilla.HarvestToolProvider;
import snownee.jade.addon.vanilla.HorseProvider;
import snownee.jade.addon.vanilla.InventoryProvider;
import snownee.jade.addon.vanilla.ItemFrameProvider;
import snownee.jade.addon.vanilla.ItemTooltipProvider;
import snownee.jade.addon.vanilla.MiscEntityNameProvider;
import snownee.jade.addon.vanilla.NoteBlockProvider;
import snownee.jade.addon.vanilla.PaintingProvider;
import snownee.jade.addon.vanilla.PlayerHeadProvider;
import snownee.jade.addon.vanilla.PotionEffectsProvider;
import snownee.jade.addon.vanilla.TNTProvider;
import snownee.jade.addon.vanilla.VillagerProfessionProvider;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {

	private static ResourceLocation RL(String path) {
		return new ResourceLocation(Jade.MODID, path);
	}

	public static final ResourceLocation INVENTORY = RL("inventory");
	public static final ResourceLocation BREWING_STAND = RL("brewing_stand");
	public static final ResourceLocation HORSE_STAT = RL("horse_stat");
	public static final ResourceLocation HORSE_INVENTORY = RL("horse_inventory");
	public static final ResourceLocation ITEM_FRAME = RL("item_frame");
	public static final ResourceLocation EFFECTS = RL("effects");
	public static final ResourceLocation MOB_GROWTH = RL("mob_growth");
	public static final ResourceLocation MOB_BREEDING = RL("mob_breeding");
	public static final ResourceLocation MISC_ENTITY = RL("misc_entity");
	public static final ResourceLocation TNT_STABILITY = RL("tnt_stability");
	public static final ResourceLocation BEEHIVE = RL("beehive");
	public static final ResourceLocation NOTE_BLOCK = RL("note_block");
	public static final ResourceLocation ARMOR_STAND = RL("armor_stand");
	public static final ResourceLocation HIDE_MOD_NAME = RL("hide_mod_name");
	public static final ResourceLocation TRAPPED_CHEST = RL("trapped_chest");
	public static final ResourceLocation PAINTING = RL("painting");
	public static final ResourceLocation CHICKEN_EGG = RL("chicken_egg");
	public static final ResourceLocation HARVEST_TOOL = RL("harvest_tool");
	public static final ResourceLocation HARVEST_TOOL_NEW_LINE = RL("harvest_tool_new_line");
	public static final ResourceLocation EFFECTIVE_TOOL = RL("effective_tool");
	public static final ResourceLocation COMMAND_BLOCK = RL("command_block");
	public static final ResourceLocation BREAKING_PROGRESS = RL("breaking_progress");
	//public static final ResourceLocation ACCURATE_NAME = RL("accurate_name");
	public static final ResourceLocation HIDE_ITEM_MOD_NAME = RL("hide_item_mod_name");
	public static final ResourceLocation ENCH_POWER = RL("ench_power");
	public static final ResourceLocation TOTAL_ENCH_POWER = RL("total_ench_power");
	public static final ResourceLocation PLAYER_HEAD = RL("player_head");
	public static final ResourceLocation PROFESSION = RL("profession");
	public static final ResourceLocation ITEM_TOOLTIP = RL("item_tooltip");

	@Override
	public void register(IRegistrar registrar) {
		registrar.registerComponentProvider(InventoryProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.registerBlockDataProvider(InventoryProvider.INSTANCE, Block.class);
		registrar.addConfig(INVENTORY, true);

		registrar.registerComponentProvider(BrewingStandProvider.INSTANCE, TooltipPosition.BODY, BrewingStandBlock.class);
		registrar.registerBlockDataProvider(BrewingStandProvider.INSTANCE, BrewingStandBlock.class);
		registrar.addConfig(BREWING_STAND, true);

		registrar.registerComponentProvider(HorseProvider.INSTANCE, TooltipPosition.BODY, AbstractHorseEntity.class);
		registrar.addConfig(HORSE_STAT, true);

		registrar.registerComponentProvider(ChestedHorseProvider.INSTANCE, TooltipPosition.BODY, AbstractChestedHorseEntity.class);
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

		//        registrar.registerComponentProvider(MiscEntityNameProvider.INSTANCE, TooltipPosition.HEAD, Entity.class);
		//        registrar.registerComponentProvider(MiscEntityNameProvider.INSTANCE, TooltipPosition.TAIL, Entity.class);
		registrar.registerEntityStackProvider(MiscEntityNameProvider.INSTANCE, Entity.class);
		//        registrar.addConfig(MISC_ENTITY, true);

		registrar.registerComponentProvider(TNTProvider.INSTANCE, TooltipPosition.BODY, TNTBlock.class);
		registrar.addConfig(TNT_STABILITY, true);

		registrar.registerComponentProvider(BeehiveProvider.INSTANCE, TooltipPosition.BODY, BeehiveBlock.class);
		registrar.registerBlockDataProvider(BeehiveProvider.INSTANCE, BeehiveBlock.class);
		registrar.addConfig(BEEHIVE, true);

		registrar.registerComponentProvider(NoteBlockProvider.INSTANCE, TooltipPosition.BODY, NoteBlock.class);
		registrar.addConfig(NOTE_BLOCK, true);

		registrar.registerComponentProvider(ArmorStandProvider.INSTANCE, TooltipPosition.BODY, ArmorStandEntity.class);
		registrar.addConfig(ARMOR_STAND, true);

		registrar.addConfig(HIDE_MOD_NAME, false);

		registrar.registerComponentProvider(PaintingProvider.INSTANCE, TooltipPosition.BODY, PaintingEntity.class);
		registrar.registerComponentProvider(PaintingProvider.INSTANCE, TooltipPosition.TAIL, PaintingEntity.class);
		registrar.addConfig(PAINTING, true);

		registrar.registerComponentProvider(ChickenEggProvider.INSTANCE, TooltipPosition.BODY, ChickenEntity.class);
		registrar.registerEntityDataProvider(ChickenEggProvider.INSTANCE, ChickenEntity.class);
		registrar.addConfig(CHICKEN_EGG, true);

		registrar.registerComponentProvider(HarvestToolProvider.INSTANCE, TooltipPosition.HEAD, Block.class);
		registrar.registerComponentProvider(HarvestToolProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.addConfig(HARVEST_TOOL, true);
		registrar.addConfig(HARVEST_TOOL_NEW_LINE, false);
		registrar.addConfig(EFFECTIVE_TOOL, true);

		registrar.registerComponentProvider(CommandBlockProvider.INSTANCE, TooltipPosition.BODY, CommandBlockBlock.class);
		registrar.registerBlockDataProvider(CommandBlockProvider.INSTANCE, CommandBlockBlock.class);
		registrar.addConfig(COMMAND_BLOCK, true);

		registrar.addConfig(BREAKING_PROGRESS, true);

		//registrar.registerComponentProvider(TrappedChestProvider.INSTANCE, TooltipPosition.HEAD, TrappedChestBlock.class);
		registrar.addConfig(TRAPPED_CHEST, true);

		registrar.registerComponentProvider(EnchantmentPowerProvider.INSTANCE, TooltipPosition.BODY, Block.class);
		registrar.addConfig(ENCH_POWER, true);
		registrar.addConfig(TOTAL_ENCH_POWER, true);

		registrar.addConfig(HIDE_ITEM_MOD_NAME, false);

		registrar.registerComponentProvider(PlayerHeadProvider.INSTANCE, TooltipPosition.HEAD, AbstractSkullBlock.class);
		registrar.addConfig(PLAYER_HEAD, true);

		registrar.registerComponentProvider(VillagerProfessionProvider.INSTANCE, TooltipPosition.HEAD, VillagerEntity.class);
		registrar.registerComponentProvider(VillagerProfessionProvider.INSTANCE, TooltipPosition.BODY, VillagerEntity.class);
		registrar.registerComponentProvider(VillagerProfessionProvider.INSTANCE, TooltipPosition.BODY, ZombieVillagerEntity.class);
		registrar.addConfig(PROFESSION, true);

		registrar.registerComponentProvider(ItemTooltipProvider.INSTANCE, TooltipPosition.BODY, ItemEntity.class);
		registrar.addConfig(ITEM_TOOLTIP, true);
	}

}
