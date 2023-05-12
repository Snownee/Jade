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

	public static final ResourceLocation PACKET_RECEIVE_DATA = JADE("receive_data");
	public static final ResourceLocation PACKET_SERVER_PING = JADE("server_ping");
	public static final ResourceLocation PACKET_REQUEST_ENTITY = JADE("request_entity");
	public static final ResourceLocation PACKET_REQUEST_TILE = JADE("request_tile");
	public static final ResourceLocation PACKET_SHOW_OVERLAY = JADE("show_overlay");

	public static final ResourceLocation CORE_OBJECT_NAME = JADE("object_name");
	public static final ResourceLocation CORE_REGISTRY_NAME = JADE("registry_name");
	public static final ResourceLocation CORE_MOD_NAME = JADE("mod_name");
	public static final ResourceLocation CORE_DISTANCE = JADE("distance");
	public static final ResourceLocation CORE_COORDINATES = JADE("coordinates");
	public static final ResourceLocation CORE_REL_COORDINATES = JADE("coordinates.rel");
	public static final ResourceLocation CORE_BLOCK_FACE = JADE("block_face");

	//TODO(1.20): Change names
	public static final ResourceLocation UNIVERSAL_ITEM_STORAGE = MC("block_inventory");
	public static final ResourceLocation UNIVERSAL_FLUID_STORAGE = MC("fluid");
	public static final ResourceLocation UNIVERSAL_FLUID_STORAGE_DETAILED = MC("fluid.detailed");
	public static final ResourceLocation UNIVERSAL_ENERGY_STORAGE = MC("energy_storage");
	public static final ResourceLocation UNIVERSAL_ENERGY_STORAGE_DETAILED = MC("energy_storage.detailed");
	public static final ResourceLocation UNIVERSAL_PROGRESS = MC("progress");

	public static final ResourceLocation MC_BLOCK_INVENTORY_DETAILED_AMOUNT = MC("block_inventory.detailed_amount");
	public static final ResourceLocation MC_BLOCK_INVENTORY_NORMAL_AMOUNT = MC("block_inventory.normal_amount");
	public static final ResourceLocation MC_BLOCK_INVENTORY_SHOW_NAME_AMOUNT = MC("block_inventory.show_name_amount");
	public static final ResourceLocation MC_BLOCK_INVENTORY_ITEMS_PER_LINE = MC("block_inventory.items_per_line");

	public static final ResourceLocation MC_ANIMAL_OWNER = MC("animal_owner");
	public static final ResourceLocation MC_ANIMAL_OWNER_FETCH_NAMES = MC("animal_owner.fetch_names");
	public static final ResourceLocation MC_ARMOR_STAND = MC("armor_stand");
	public static final ResourceLocation MC_BEEHIVE = MC("beehive");
	public static final ResourceLocation MC_BLOCK_STATES = MC("block_states");
	public static final ResourceLocation MC_BREWING_STAND = MC("brewing_stand");
	public static final ResourceLocation MC_CHICKEN_EGG = MC("chicken_egg");
	public static final ResourceLocation MC_CHISELED_BOOKSHELF = MC("chiseled_bookshelf");
	public static final ResourceLocation MC_COMMAND_BLOCK = MC("command_block");
	public static final ResourceLocation MC_CROP_PROGRESS = MC("crop_progress");
	public static final ResourceLocation MC_ENCHANTMENT_POWER = MC("enchantment_power");
	public static final ResourceLocation MC_ENTITY_ARMOR = MC("entity_armor");
	public static final ResourceLocation MC_ENTITY_ARMOR_MAX_FOR_RENDER = MC("entity_armor.max_for_render");
	public static final ResourceLocation MC_ENTITY_HEALTH = MC("entity_health");
	public static final ResourceLocation MC_ENTITY_HEALTH_MAX_FOR_RENDER = MC("entity_health.max_for_render");
	public static final ResourceLocation MC_ENTITY_HEALTH_ICONS_PER_LINE = MC("entity_health.icons_per_line");
	public static final ResourceLocation MC_ENTITY_HEALTH_SHOW_FRACTIONS = MC("entity_health.show_fractions");
	public static final ResourceLocation MC_FALLING_BLOCK = MC("falling_block");
	public static final ResourceLocation MC_FURNACE = MC("furnace");
	public static final ResourceLocation MC_HARVEST_TOOL = MC("harvest_tool");
	public static final ResourceLocation MC_HARVEST_TOOL_NEW_LINE = MC("harvest_tool.new_line");
	public static final ResourceLocation MC_EFFECTIVE_TOOL = MC("harvest_tool.effective_tool");
	public static final ResourceLocation MC_SHOW_UNBREAKABLE = MC("harvest_tool.show_unbreakable");
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
	public static final ResourceLocation MC_ITEM_DISPLAY = MC("item_display");
	public static final ResourceLocation MC_BLOCK_DISPLAY = MC("block_display");

	public static final ResourceLocation MC_BREAKING_PROGRESS = MC("breaking_progress");
}
