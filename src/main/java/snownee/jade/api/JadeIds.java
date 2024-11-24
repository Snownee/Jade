package snownee.jade.api;

import net.minecraft.resources.ResourceLocation;

public interface JadeIds {

	ResourceLocation ROOT = MC("root");
	ResourceLocation PACKET_RECEIVE_DATA = JADE("receive_data");
	ResourceLocation PACKET_SERVER_PING = JADE("server_ping_v1");
	ResourceLocation PACKET_REQUEST_ENTITY = JADE("request_entity");
	ResourceLocation PACKET_REQUEST_BLOCK = JADE("request_block");
	ResourceLocation PACKET_SHOW_OVERLAY = JADE("show_overlay");
	ResourceLocation CORE_ROOT_ICON = JADE("root_icon");
	ResourceLocation CORE_OBJECT_NAME = JADE("object_name");
	ResourceLocation CORE_MOD_NAME = JADE("mod_name");
	ResourceLocation CORE_DISTANCE = JADE("distance");
	ResourceLocation CORE_COORDINATES = JADE("coordinates");
	ResourceLocation CORE_REL_COORDINATES = JADE("coordinates.rel");
	ResourceLocation CORE_BLOCK_FACE = JADE("block_face");
	ResourceLocation DEBUG_REGISTRY_NAME = JADE("registry_name");
	ResourceLocation DEBUG_SPECIAL_REGISTRY_NAME = JADE("registry_name.special");
	ResourceLocation DEBUG_BLOCK_PROPERTIES = JADE("block_properties");
	ResourceLocation DEBUG_BLOCK_STATES = JADE("block_states");
	ResourceLocation UNIVERSAL_ITEM_STORAGE = MC("item_storage");
	ResourceLocation UNIVERSAL_ITEM_STORAGE_DEFAULT = MC("item_storage.default");
	ResourceLocation UNIVERSAL_ITEM_STORAGE_DETAILED_AMOUNT = MC("item_storage.detailed_amount");
	ResourceLocation UNIVERSAL_ITEM_STORAGE_NORMAL_AMOUNT = MC("item_storage.normal_amount");
	ResourceLocation UNIVERSAL_ITEM_STORAGE_SHOW_NAME_AMOUNT = MC("item_storage.show_name_amount");
	ResourceLocation UNIVERSAL_ITEM_STORAGE_ITEMS_PER_LINE = MC("item_storage.items_per_line");
	ResourceLocation UNIVERSAL_FLUID_STORAGE = MC("fluid_storage");
	ResourceLocation UNIVERSAL_FLUID_STORAGE_DEFAULT = MC("fluid_storage.default");
	ResourceLocation UNIVERSAL_FLUID_STORAGE_DETAILED = MC("fluid_storage.detailed");
	ResourceLocation UNIVERSAL_FLUID_STORAGE_STYLE = MC("fluid_storage.style");
	ResourceLocation UNIVERSAL_ENERGY_STORAGE = MC("energy_storage");
	ResourceLocation UNIVERSAL_ENERGY_STORAGE_DEFAULT = MC("energy_storage.default");
	ResourceLocation UNIVERSAL_ENERGY_STORAGE_DETAILED = MC("energy_storage.detailed");
	ResourceLocation UNIVERSAL_ENERGY_STORAGE_STYLE = MC("energy_storage.style");
	ResourceLocation UNIVERSAL_PROGRESS = MC("progress");
	ResourceLocation MC_ANIMAL_OWNER = MC("animal_owner");
	ResourceLocation MC_ARMOR_STAND = MC("armor_stand");
	ResourceLocation MC_BEEHIVE = MC("beehive");
	ResourceLocation MC_BLOCK_DISPLAY = MC("block_display");
	ResourceLocation MC_BREAKING_PROGRESS = MC("breaking_progress");
	ResourceLocation MC_BREWING_STAND = MC("brewing_stand");
	ResourceLocation MC_CAMPFIRE = MC("campfire");
	ResourceLocation MC_CHISELED_BOOKSHELF = MC("chiseled_bookshelf");
	ResourceLocation MC_COMMAND_BLOCK = MC("command_block");
	ResourceLocation MC_CROP_PROGRESS = MC("crop_progress");
	ResourceLocation MC_ENCHANTMENT_POWER = MC("enchantment_power");
	ResourceLocation MC_ENTITY_ARMOR = MC("entity_armor");
	ResourceLocation MC_ENTITY_ARMOR_MAX_FOR_RENDER = MC("entity_armor.max_for_render");
	ResourceLocation MC_ENTITY_HEALTH = MC("entity_health");
	ResourceLocation MC_ENTITY_HEALTH_MAX_FOR_RENDER = MC("entity_health.max_for_render");
	ResourceLocation MC_ENTITY_HEALTH_ICONS_PER_LINE = MC("entity_health.icons_per_line");
	ResourceLocation MC_ENTITY_HEALTH_SHOW_FRACTIONS = MC("entity_health.show_fractions");
	ResourceLocation MC_FALLING_BLOCK = MC("falling_block");
	ResourceLocation MC_FURNACE = MC("furnace");
	ResourceLocation MC_HARVEST_TOOL = MC("harvest_tool");
	ResourceLocation MC_HARVEST_TOOL_NEW_LINE = MC("harvest_tool.new_line");
	ResourceLocation MC_EFFECTIVE_TOOL = MC("harvest_tool.effective_tool");
	ResourceLocation MC_SHOW_UNBREAKABLE = MC("harvest_tool.show_unbreakable");
	ResourceLocation MC_HARVEST_TOOL_CREATIVE = MC("harvest_tool.creative");
	ResourceLocation MC_HOPPER_LOCK = MC("hopper_lock");
	ResourceLocation MC_HORSE_STATS = MC("horse_stats");
	ResourceLocation MC_ITEM_BER = MC("item_ber");
	ResourceLocation MC_ITEM_DISPLAY = MC("item_display");
	ResourceLocation MC_ITEM_FRAME = MC("item_frame");
	ResourceLocation MC_ITEM_TOOLTIP = MC("item_tooltip");
	ResourceLocation MC_JUKEBOX = MC("jukebox");
	ResourceLocation MC_LECTERN = MC("lectern");
	ResourceLocation MC_MOB_BREEDING = MC("mob_breeding");
	ResourceLocation MC_MOB_GROWTH = MC("mob_growth");
	ResourceLocation MC_MOB_SPAWNER = MC("mob_spawner");
	ResourceLocation MC_MOB_SPAWNER_COOLDOWN = MC("mob_spawner.cooldown");
	ResourceLocation MC_NEXT_ENTITY_DROP = MC("next_entity_drop");
	ResourceLocation MC_NOTE_BLOCK = MC("note_block");
	ResourceLocation MC_PAINTING = MC("painting");
	ResourceLocation MC_PLAYER_HEAD = MC("player_head");
	ResourceLocation MC_POTION_EFFECTS = MC("potion_effects");
	ResourceLocation MC_REDSTONE = MC("redstone");
	ResourceLocation MC_TNT_STABILITY = MC("tnt_stability");
	ResourceLocation MC_TOTAL_ENCHANTMENT_POWER = MC("total_enchantment_power");
	ResourceLocation MC_VILLAGER_PROFESSION = MC("villager_profession");
	ResourceLocation MC_WAXED = MC("waxed");
	ResourceLocation MC_ZOMBIE_VILLAGER = MC("zombie_villager");
	ResourceLocation ACCESS_SIGN = ACCESS("sign");
	ResourceLocation ACCESS_BLOCK_DETAILS = ACCESS("block");
	ResourceLocation ACCESS_BLOCK_DETAILS_BODY = ACCESS("block_body");
	ResourceLocation ACCESS_BLOCK_AMOUNT = ACCESS("block_amount");
	ResourceLocation ACCESS_ENTITY_DETAILS = ACCESS("entity");
	ResourceLocation ACCESS_ENTITY_DETAILS_BODY = ACCESS("entity_body");
	ResourceLocation ACCESS_ENTITY_VARIANT = ACCESS("entity_variant");
	ResourceLocation ACCESS_HELD_ITEM = ACCESS("held_item");

	static ResourceLocation JADE(String path) {
		return ResourceLocation.fromNamespaceAndPath("jade", path);
	}

	static ResourceLocation ACCESS(String path) {
		return ResourceLocation.fromNamespaceAndPath("jade_access", path);
	}

	static boolean isAccess(ResourceLocation id) {
		return id.getNamespace().equals("jade_access");
	}

	private static ResourceLocation MC(String path) {
		return ResourceLocation.withDefaultNamespace(path);
	}
}
