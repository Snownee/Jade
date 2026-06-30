package snownee.jade.api;

import net.minecraft.resources.Identifier;

/**
 * Shared identifier constants used across Jade's built-in providers and configuration keys.
 */
public interface JadeIds {

	Identifier ROOT = MC("root");
	Identifier DEFAULT_THEME = JADE("dark");
	Identifier UI_MAIN = JADE("main");
	Identifier PACKET_RECEIVE_DATA = JADE("receive_data");
	Identifier PACKET_SERVER_HANDSHAKE = JADE("server_handshake");
	Identifier PACKET_CLIENT_HANDSHAKE = JADE("client_handshake");
	Identifier PACKET_REQUEST_ENTITY = JADE("request_entity");
	Identifier PACKET_REQUEST_BLOCK = JADE("request_block");
	Identifier PACKET_SHOW_OVERLAY = JADE("show_overlay");
	Identifier CORE_ROOT_ICON = JADE("root_icon");
	Identifier CORE_OBJECT_NAME = JADE("object_name");
	Identifier CORE_MOD_NAME = JADE("mod_name");
	Identifier CORE_TRANSLATE_MOD_NAME = JADE("mod_name.translated");
	Identifier CORE_DISTANCE = JADE("distance");
	Identifier CORE_COORDINATES = JADE("coordinates");
	Identifier CORE_REL_COORDINATES = JADE("coordinates.rel");
	Identifier CORE_BLOCK_FACE = JADE("block_face");
	Identifier DEBUG_REGISTRY_NAME = JADE("registry_name");
	Identifier DEBUG_SPECIAL_REGISTRY_NAME = JADE("registry_name.special");
	Identifier DEBUG_BLOCK_PROPERTIES = JADE("block_properties");
	Identifier DEBUG_BLOCK_STATES = JADE("block_states");
	Identifier DEBUG_LOOT_TABLE = JADE("loot_table");
	Identifier DEBUG_ENTITY_ATTRIBUTES = JADE("entity_attributes");
	Identifier UNIVERSAL_ITEM_STORAGE = MC("item_storage");
	Identifier UNIVERSAL_ITEM_STORAGE_DEFAULT = MC("item_storage.default");
	Identifier UNIVERSAL_ITEM_STORAGE_DETAILED_AMOUNT = MC("item_storage.detailed_amount");
	Identifier UNIVERSAL_ITEM_STORAGE_NORMAL_AMOUNT = MC("item_storage.normal_amount");
	Identifier UNIVERSAL_ITEM_STORAGE_SHOW_NAME_AMOUNT = MC("item_storage.show_name_amount");
	Identifier UNIVERSAL_ITEM_STORAGE_ITEMS_PER_LINE = MC("item_storage.items_per_line");
	Identifier UNIVERSAL_ITEM_STORAGE_SORT = MC("item_storage.sort");
	Identifier UNIVERSAL_FLUID_STORAGE = MC("fluid_storage");
	Identifier UNIVERSAL_FLUID_STORAGE_DEFAULT = MC("fluid_storage.default");
	Identifier UNIVERSAL_FLUID_STORAGE_DETAILED = MC("fluid_storage.detailed");
	Identifier UNIVERSAL_FLUID_STORAGE_STYLE = MC("fluid_storage.style");
	Identifier UNIVERSAL_ENERGY_STORAGE = MC("energy_storage");
	Identifier UNIVERSAL_ENERGY_STORAGE_DEFAULT = MC("energy_storage.default");
	Identifier UNIVERSAL_ENERGY_STORAGE_DETAILED = MC("energy_storage.detailed");
	Identifier UNIVERSAL_ENERGY_STORAGE_STYLE = MC("energy_storage.style");
	Identifier UNIVERSAL_PROGRESS = MC("progress");
	Identifier UNIVERSAL_HIDE_THINGS = JADE("hide_things");
	Identifier MC_ANIMAL_OWNER = MC("animal_owner");
	Identifier MC_ARMOR_STAND = MC("armor_stand");
	Identifier MC_BEEHIVE = MC("beehive");
	Identifier MC_BLOCK_DISPLAY = MC("block_display");
	Identifier MC_BREAKING_PROGRESS = MC("breaking_progress");
	Identifier MC_BREWING_STAND = MC("brewing_stand");
	Identifier MC_CAMPFIRE = MC("campfire");
	Identifier MC_COMMAND_BLOCK = MC("command_block");
	Identifier MC_CROP_PROGRESS = MC("crop_progress");
	Identifier MC_ENCHANTMENT_POWER = MC("enchantment_power");
	Identifier MC_ENTITY_ARMOR = MC("entity_armor");
	Identifier MC_ENTITY_ARMOR_MAX_FOR_RENDER = MC("entity_armor.max_for_render");
	Identifier MC_ENTITY_HEALTH = MC("entity_health");
	Identifier MC_ENTITY_HEALTH_MAX_FOR_RENDER = MC("entity_health.max_for_render");
	Identifier MC_ENTITY_HEALTH_ICONS_PER_LINE = MC("entity_health.icons_per_line");
	Identifier MC_ENTITY_HEALTH_SHOW_FRACTIONS = MC("entity_health.show_fractions");
	Identifier MC_FALLING_BLOCK = MC("falling_block");
	Identifier MC_FURNACE = MC("furnace");
	Identifier MC_HARVEST_TOOL = MC("harvest_tool");
	Identifier MC_HARVEST_TOOL_NEW_LINE = MC("harvest_tool.new_line");
	Identifier MC_EFFECTIVE_TOOL = MC("harvest_tool.effective_tool");
	Identifier MC_SHOW_UNBREAKABLE = MC("harvest_tool.show_unbreakable");
	Identifier MC_HARVEST_TOOL_CREATIVE = MC("harvest_tool.creative");
	Identifier MC_HOPPER_LOCK = MC("hopper_lock");
	Identifier MC_HORSE_STATS = MC("horse_stats");
	Identifier MC_ITEM_BER = MC("item_ber");
	Identifier MC_ITEM_DISPLAY = MC("item_display");
	Identifier MC_ITEM_FRAME = MC("item_frame");
	Identifier MC_ITEM_TOOLTIP = MC("item_tooltip");
	Identifier MC_JUKEBOX = MC("jukebox");
	Identifier MC_LECTERN = MC("lectern");
	Identifier MC_MOB_BREEDING = MC("mob_breeding");
	Identifier MC_MOB_GROWTH = MC("mob_growth");
	Identifier MC_MOB_SPAWNER = MC("mob_spawner");
	Identifier MC_MOB_SPAWNER_COOLDOWN = MC("mob_spawner.cooldown");
	Identifier MC_NEXT_ENTITY_DROP = MC("next_entity_drop");
	Identifier MC_NOTE_BLOCK = MC("note_block");
	Identifier MC_PAINTING = MC("painting");
	Identifier MC_PET_ARMOR = MC("pet_armor");
	Identifier MC_PLAYER_HEAD = MC("player_head");
	Identifier MC_POTION_EFFECTS = MC("potion_effects");
	Identifier MC_POTION_EFFECTS_LIMIT = MC("potion_effects.limit");
	Identifier MC_REDSTONE = MC("redstone");
	Identifier MC_SHELF = MC("shelf");
	Identifier MC_SULFUR_CUBE = MC("sulfur_cube");
	Identifier MC_TNT_STABILITY = MC("tnt_stability");
	Identifier MC_TOTAL_ENCHANTMENT_POWER = MC("total_enchantment_power");
	Identifier MC_VILLAGER_PROFESSION = MC("villager_profession");
	Identifier MC_WAXED = MC("waxed");
	Identifier MC_ZOMBIE_VILLAGER = MC("zombie_villager");
	Identifier ACCESS_SIGN = ACCESS("sign");
	Identifier ACCESS_BLOCK_DETAILS = ACCESS("block");
	Identifier ACCESS_BLOCK_DETAILS_BODY = ACCESS("block_body");
	Identifier ACCESS_BLOCK_AMOUNT = ACCESS("block_amount");
	Identifier ACCESS_ENTITY_DETAILS = ACCESS("entity");
	Identifier ACCESS_ENTITY_DETAILS_BODY = ACCESS("entity_body");
	Identifier ACCESS_ENTITY_VARIANT = ACCESS("entity_variant");
	Identifier ACCESS_HELD_ITEM = ACCESS("held_item");
	Identifier ACCESS_NPC_DESCRIPTION = ACCESS("npc_description");

	/**
	 * Creates an identifier in the Jade namespace.
	 *
	 * @param path identifier path
	 * @return the namespaced identifier
	 */
	static Identifier JADE(String path) {
		return Identifier.fromNamespaceAndPath("jade", path);
	}

	/**
	 * Creates an identifier in the Jade access namespace.
	 *
	 * @param path identifier path
	 * @return the namespaced identifier
	 */
	static Identifier ACCESS(String path) {
		return Identifier.fromNamespaceAndPath("jade_access", path);
	}

	/**
	 * Returns whether the identifier belongs to the Jade access namespace.
	 *
	 * @param id identifier to check
	 * @return {@code true} if the identifier is an access identifier
	 */
	static boolean isAccess(Identifier id) {
		return id.getNamespace().equals("jade_access");
	}

	private static Identifier MC(String path) {
		return Identifier.withDefaultNamespace(path);
	}
}
