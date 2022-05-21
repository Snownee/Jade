package snownee.jade.api;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.Jade;

public class Identifiers {

	private static ResourceLocation JADE(String path) {
		return new ResourceLocation(Jade.MODID, path);
	}

	private static ResourceLocation MC(String path) {
		return new ResourceLocation("minecraft", path);
	}

	public static final ResourceLocation CORE_OBJECT_NAME = JADE("object_name");
	public static final ResourceLocation CORE_REGISTRY_NAME = JADE("registry_name");
	public static final ResourceLocation CORE_MOD_NAME = JADE("mod_name");

	public static final ResourceLocation FORGE_BLOCK_INVENTORY = MC("block_inventory");
	public static final ResourceLocation FORGE_FLUID = MC("fluid");
	public static final ResourceLocation FORGE_ENERGY = MC("fe");

	public static final ResourceLocation FABRIC_BLOCK_INVENTORY = MC("block_inventory");
	public static final ResourceLocation FABRIC_FLUID = MC("fluid");

	public static final ResourceLocation MC_ANIMAL_OWNER = MC("animal_owner");
	public static final ResourceLocation MC_ARMOR_STAND = MC("armor_stand");
	public static final ResourceLocation MC_BEEHIVE = MC("beehive");
	public static final ResourceLocation MC_BLOCK_STATES = MC("block_states");
	public static final ResourceLocation MC_BREWING_STAND = MC("brewing_stand");
	public static final ResourceLocation MC_CHESTED_HORSE = MC("chested_horse");
	public static final ResourceLocation MC_CHICKEN_EGG = MC("chicken_egg");
	public static final ResourceLocation MC_COMMAND_BLOCK = MC("command_block");
	public static final ResourceLocation MC_CROP_PROGRESS = MC("crop_progress");
	public static final ResourceLocation MC_ENCHANTMENT_POWER = MC("enchantment_power");
	public static final ResourceLocation MC_ENTITY_ARMOR = MC("entity_armor");
	public static final ResourceLocation MC_ENTITY_HEALTH = MC("entity_health");
	public static final ResourceLocation MC_FALLING_BLOCK = MC("falling_block");
	public static final ResourceLocation MC_FURNACE = MC("furnace");
	public static final ResourceLocation MC_HARVEST_TOOL = MC("harvest_tool");
	public static final ResourceLocation MC_HORSE_STATS = MC("horse_stats");
	public static final ResourceLocation MC_ITEM_FRAME = MC("item_frame");
	public static final ResourceLocation MC_ITEM_TOOLTIP = MC("item_tooltip");
	public static final ResourceLocation MC_JUKEBOX = MC("jukebox");
	public static final ResourceLocation MC_LECTERN = MC("lectern");
	public static final ResourceLocation MC_MOB_BREEDING = MC("mob_breeding");
	public static final ResourceLocation MC_MOB_GROWTH = MC("mob_growth");
	public static final ResourceLocation MC_MOB_SPAWNER = MC("mob_spawner");
	public static final ResourceLocation MC_NOTE_BLOCK = MC("note_block");
	public static final ResourceLocation MC_PAINTING = MC("painting");
	public static final ResourceLocation MC_PLAYER_HEAD = MC("player_head");
	public static final ResourceLocation MC_POTION_EFFECTS = MC("potion_effects");
	public static final ResourceLocation MC_REDSTONE = MC("redstone");
	public static final ResourceLocation MC_TNT_STABILITY = MC("tnt_stability");
	public static final ResourceLocation MC_TOTAL_ENCHANTMENT_POWER = MC("total_enchantment_power");
	public static final ResourceLocation MC_VILLAGER_PROFESSION = MC("villager_profession");

	public static final ResourceLocation MC_BREAKING_PROGRESS = MC("breaking_progress");
	public static final ResourceLocation MC_HARVEST_TOOL_NEW_LINE = MC("harvest_tool_new_line");
	public static final ResourceLocation MC_EFFECTIVE_TOOL = MC("effective_tool");

}
