package snownee.jade.addon.vanilla;

import java.util.List;
import java.util.Map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.jade.JadeClient;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.overlay.DatapackBlockManager;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;

@WailaPlugin
public class VanillaPlugin implements IWailaPlugin {

	private static final Cache<BlockState, BlockState> CHEST_CACHE = CacheBuilder.newBuilder().build();

	public static BlockState getCorrespondingNormalChest(BlockState state) {
		try {
			return CHEST_CACHE.get(
					state, () -> {
						ResourceLocation trappedName = CommonProxy.getId(state.getBlock());
						Block block = Blocks.AIR;
						if (trappedName.getPath().startsWith("trapped_")) {
							ResourceLocation chestName = trappedName.withPath(trappedName.getPath().substring(8));
							block = BuiltInRegistries.BLOCK.getValue(chestName);
						} else if (trappedName.getPath().endsWith("_trapped_chest")) {
							ResourceLocation chestName = trappedName.withPath(
									trappedName.getPath().substring(0, trappedName.getPath().length() - 14) + "_chest");
							block = BuiltInRegistries.BLOCK.getValue(chestName);
						}
						if (block != Blocks.AIR) {
							return copyProperties(state, block.defaultBlockState());
						}
						return state;
					});
		} catch (Exception e) {
			return state;
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> BlockState copyProperties(BlockState oldState, BlockState newState) {
		for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
			Property<T> property = (Property<T>) entry.getKey();
			if (newState.hasProperty(property)) {
				newState = newState.setValue(property, property.getValueClass().cast(entry.getValue()));
			}
		}
		return newState;
	}

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(BrewingStandProvider.INSTANCE, BrewingStandBlock.class);
		registration.registerBlockDataProvider(BeehiveProvider.INSTANCE, BeehiveBlock.class);
		registration.registerBlockDataProvider(CommandBlockProvider.INSTANCE, CommandBlock.class);
		registration.registerBlockDataProvider(HopperLockProvider.INSTANCE, HopperBlock.class);
		registration.registerBlockDataProvider(JukeboxProvider.INSTANCE, JukeboxBlock.class);
		registration.registerBlockDataProvider(LecternProvider.INSTANCE, LecternBlock.class);
		registration.registerBlockDataProvider(RedstoneProvider.INSTANCE, ComparatorBlockEntity.class);
		registration.registerBlockDataProvider(RedstoneProvider.INSTANCE, CalibratedSculkSensorBlockEntity.class);
		registration.registerBlockDataProvider(FurnaceProvider.INSTANCE, AbstractFurnaceBlock.class);
		registration.registerBlockDataProvider(ChiseledBookshelfProvider.INSTANCE, ChiseledBookShelfBlock.class);
		registration.registerBlockDataProvider(MobSpawnerCooldownProvider.INSTANCE, TrialSpawnerBlock.class);

		registration.registerEntityDataProvider(AnimalOwnerProvider.INSTANCE, Entity.class);
		registration.registerEntityDataProvider(StatusEffectsProvider.INSTANCE, LivingEntity.class);
		registration.registerEntityDataProvider(MobGrowthProvider.INSTANCE, AgeableMob.class);
		registration.registerEntityDataProvider(MobGrowthProvider.INSTANCE, Tadpole.class);
		registration.registerEntityDataProvider(MobBreedingProvider.INSTANCE, Animal.class);
		registration.registerEntityDataProvider(MobBreedingProvider.INSTANCE, Allay.class);
		registration.registerEntityDataProvider(NextEntityDropProvider.INSTANCE, Chicken.class);
		registration.registerEntityDataProvider(NextEntityDropProvider.INSTANCE, Armadillo.class);
		registration.registerEntityDataProvider(ZombieVillagerProvider.INSTANCE, ZombieVillager.class);
		registration.registerEntityDataProvider(PetArmorProvider.INSTANCE, Mob.class);

		registration.registerItemStorage(CampfireProvider.INSTANCE, CampfireBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addConfig(JadeIds.MC_EFFECTIVE_TOOL, true);
		registration.addConfig(JadeIds.MC_HARVEST_TOOL_NEW_LINE, false);
		registration.addConfig(JadeIds.MC_SHOW_UNBREAKABLE, false);
		registration.addConfig(JadeIds.MC_HARVEST_TOOL_CREATIVE, false);
		registration.addConfig(JadeIds.MC_BREAKING_PROGRESS, true);
		registration.addConfig(JadeIds.MC_ENTITY_HEALTH, true);
		registration.addConfig(JadeIds.MC_ENTITY_ARMOR, true);

		registration.addConfig(JadeIds.MC_ENTITY_ARMOR_MAX_FOR_RENDER, 20, 0, 200, false);
		registration.addConfig(JadeIds.MC_ENTITY_HEALTH_MAX_FOR_RENDER, 40, 0, 200, false);
		registration.addConfig(JadeIds.MC_ENTITY_HEALTH_ICONS_PER_LINE, 10, 5, 40, false);
		registration.addConfig(JadeIds.MC_ENTITY_HEALTH_SHOW_FRACTIONS, false);
		registration.addConfig(JadeIds.MC_PET_ARMOR, PetArmorProvider.Mode.SHOW_DAMAGEABLE);

		registration.registerBlockComponent(BrewingStandProvider.INSTANCE, BrewingStandBlock.class);
		registration.registerEntityComponent(HorseStatsProvider.INSTANCE, AbstractHorse.class);
		registration.registerEntityComponent(ItemFrameProvider.INSTANCE, ItemFrame.class);
		registration.registerEntityComponent(StatusEffectsProvider.INSTANCE, LivingEntity.class);
		registration.registerEntityComponent(MobGrowthProvider.INSTANCE, AgeableMob.class);
		registration.registerEntityComponent(MobGrowthProvider.INSTANCE, Tadpole.class);
		registration.registerEntityComponent(MobBreedingProvider.INSTANCE, Animal.class);
		registration.registerEntityComponent(MobBreedingProvider.INSTANCE, Allay.class);
		registration.registerBlockComponent(TNTStabilityProvider.INSTANCE, TntBlock.class);
		registration.registerBlockComponent(BeehiveProvider.INSTANCE, BeehiveBlock.class);
		registration.registerBlockComponent(NoteBlockProvider.INSTANCE, NoteBlock.class);
		registration.registerEntityComponent(ArmorStandProvider.INSTANCE, ArmorStand.class);
		registration.registerEntityComponent(PaintingProvider.INSTANCE, Painting.class);
		registration.registerEntityComponent(NextEntityDropProvider.INSTANCE, Chicken.class);
		registration.registerEntityComponent(NextEntityDropProvider.INSTANCE, Armadillo.class);
		registration.registerBlockComponent(HarvestToolProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(CommandBlockProvider.INSTANCE, CommandBlock.class);
		registration.registerBlockComponent(EnchantmentPowerProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(TotalEnchantmentPowerProvider.INSTANCE, EnchantingTableBlock.class);
		registration.registerBlockComponent(PlayerHeadProvider.INSTANCE, AbstractSkullBlock.class);
		registration.registerBlockIcon(ItemBERProvider.INSTANCE, AbstractSkullBlock.class);
		registration.registerBlockIcon(ItemBERProvider.INSTANCE, DecoratedPotBlock.class);
		registration.registerEntityComponent(VillagerProfessionProvider.INSTANCE, Villager.class);
		registration.registerEntityComponent(VillagerProfessionProvider.INSTANCE, ZombieVillager.class);
		registration.registerEntityComponent(ItemTooltipProvider.INSTANCE, ItemEntity.class);
		registration.registerBlockComponent(FurnaceProvider.INSTANCE, AbstractFurnaceBlock.class);
		registration.registerEntityComponent(AnimalOwnerProvider.INSTANCE, Entity.class);
		registration.registerEntityIcon(FallingBlockProvider.INSTANCE, FallingBlockEntity.class);
		registration.registerEntityComponent(EntityHealthAndArmorProvider.INSTANCE, LivingEntity.class);
		registration.registerBlockComponent(RedstoneProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(HopperLockProvider.INSTANCE, HopperBlock.class);
		registration.registerBlockComponent(CropProgressProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(JukeboxProvider.INSTANCE, JukeboxBlock.class);
		registration.registerBlockComponent(LecternProvider.INSTANCE, LecternBlock.class);
		registration.registerBlockComponent(MobSpawnerProvider.getBlock(), SpawnerBlock.class);
		registration.registerBlockComponent(MobSpawnerProvider.getBlock(), TrialSpawnerBlock.class);
		registration.registerEntityComponent(MobSpawnerProvider.getEntity(), MinecartSpawner.class);
		registration.registerBlockComponent(MobSpawnerCooldownProvider.INSTANCE, TrialSpawnerBlock.class);
		registration.registerBlockComponent(ChiseledBookshelfProvider.INSTANCE, ChiseledBookShelfBlock.class);
		registration.registerBlockIcon(ChiseledBookshelfProvider.INSTANCE, ChiseledBookShelfBlock.class);
		registration.registerEntityIcon(ItemDisplayProvider.INSTANCE, ItemDisplay.class);
		registration.registerEntityIcon(BlockDisplayProvider.INSTANCE, BlockDisplay.class);
		registration.registerEntityComponent(ZombieVillagerProvider.INSTANCE, ZombieVillager.class);
		registration.registerBlockComponent(WaxedProvider.INSTANCE, SignBlock.class);
		registration.registerBlockIcon(WaxedProvider.INSTANCE, SignBlock.class);
		registration.registerEntityComponent(PetArmorProvider.INSTANCE, Mob.class);

		registration.registerItemStorageClient(CampfireProvider.INSTANCE);

		ClientProxy.registerReloadListener(HarvestToolProvider.INSTANCE);

		registration.addRayTraceCallback(-1000, JadeClient::limitMobEffectFog);
		registration.addRayTraceCallback(-10, JadeClient::builtInOverrides);
		registration.addRayTraceCallback(5000, DatapackBlockManager::override);
		registration.addAfterRenderCallback(100, JadeClient::drawBreakingProgress);

		registration.markAsClientFeature(JadeIds.MC_EFFECTIVE_TOOL);
		registration.markAsClientFeature(JadeIds.MC_HARVEST_TOOL_NEW_LINE);
		registration.markAsClientFeature(JadeIds.MC_SHOW_UNBREAKABLE);
		registration.markAsClientFeature(JadeIds.MC_HARVEST_TOOL_CREATIVE);
		registration.markAsClientFeature(JadeIds.MC_BREAKING_PROGRESS);
		registration.markAsClientFeature(JadeIds.MC_ENTITY_ARMOR_MAX_FOR_RENDER);
		registration.markAsClientFeature(JadeIds.MC_ENTITY_HEALTH_MAX_FOR_RENDER);
		registration.markAsClientFeature(JadeIds.MC_ENTITY_HEALTH_ICONS_PER_LINE);
		registration.markAsClientFeature(JadeIds.MC_ENTITY_HEALTH_SHOW_FRACTIONS);
		registration.markAsClientFeature(JadeIds.MC_HORSE_STATS);
		registration.markAsClientFeature(JadeIds.MC_ITEM_FRAME);
		registration.markAsClientFeature(JadeIds.MC_TNT_STABILITY);
		registration.markAsClientFeature(JadeIds.MC_NOTE_BLOCK);
		registration.markAsClientFeature(JadeIds.MC_ARMOR_STAND);
		registration.markAsClientFeature(JadeIds.MC_PAINTING);
		registration.markAsClientFeature(JadeIds.MC_HARVEST_TOOL);
		registration.markAsClientFeature(JadeIds.MC_ENCHANTMENT_POWER);
		registration.markAsClientFeature(JadeIds.MC_TOTAL_ENCHANTMENT_POWER);
		registration.markAsClientFeature(JadeIds.MC_PLAYER_HEAD);
		registration.markAsClientFeature(JadeIds.MC_VILLAGER_PROFESSION);
		registration.markAsClientFeature(JadeIds.MC_ITEM_TOOLTIP);
		registration.markAsClientFeature(JadeIds.MC_ENTITY_HEALTH);
		registration.markAsClientFeature(JadeIds.MC_ENTITY_ARMOR);
		registration.markAsClientFeature(JadeIds.MC_CROP_PROGRESS);
		registration.markAsClientFeature(JadeIds.MC_MOB_SPAWNER);
		registration.markAsClientFeature(JadeIds.MC_WAXED);

		Component block = Component.translatable("config.jade.plugin_minecraft.block");
		Component entity = Component.translatable("config.jade.plugin_minecraft.entity");
		List<Component> both = List.of(block, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_ANIMAL_OWNER, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_ARMOR_STAND, both);
		registration.setConfigCategoryOverride(JadeIds.MC_BEEHIVE, block);
		registration.setConfigCategoryOverride(JadeIds.MC_BREAKING_PROGRESS, block);
		registration.setConfigCategoryOverride(JadeIds.MC_BREWING_STAND, block);
		registration.setConfigCategoryOverride(JadeIds.MC_CHISELED_BOOKSHELF, block);
		registration.setConfigCategoryOverride(JadeIds.MC_COMMAND_BLOCK, block);
		registration.setConfigCategoryOverride(JadeIds.MC_CROP_PROGRESS, block);
		registration.setConfigCategoryOverride(JadeIds.MC_ENCHANTMENT_POWER, block);
		registration.setConfigCategoryOverride(JadeIds.MC_ENTITY_ARMOR, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_ENTITY_HEALTH, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_FURNACE, block);
		registration.setConfigCategoryOverride(JadeIds.MC_HARVEST_TOOL, block);
		registration.setConfigCategoryOverride(JadeIds.MC_HORSE_STATS, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_ITEM_FRAME, both);
		registration.setConfigCategoryOverride(JadeIds.MC_ITEM_TOOLTIP, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_JUKEBOX, block);
		registration.setConfigCategoryOverride(JadeIds.MC_LECTERN, block);
		registration.setConfigCategoryOverride(JadeIds.MC_MOB_BREEDING, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_MOB_GROWTH, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_MOB_SPAWNER, block);
		registration.setConfigCategoryOverride(JadeIds.MC_NEXT_ENTITY_DROP, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_NOTE_BLOCK, block);
		registration.setConfigCategoryOverride(JadeIds.MC_PAINTING, both);
		registration.setConfigCategoryOverride(JadeIds.MC_PET_ARMOR, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_PLAYER_HEAD, block);
		registration.setConfigCategoryOverride(JadeIds.MC_POTION_EFFECTS, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_REDSTONE, block);
		registration.setConfigCategoryOverride(JadeIds.MC_TNT_STABILITY, block);
		registration.setConfigCategoryOverride(JadeIds.MC_TOTAL_ENCHANTMENT_POWER, block);
		registration.setConfigCategoryOverride(JadeIds.MC_VILLAGER_PROFESSION, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_WAXED, block);
		registration.setConfigCategoryOverride(JadeIds.MC_ZOMBIE_VILLAGER, entity);

		WailaCommonRegistration.instance().priorities.putUnsafe(JadeIds.MC_ENTITY_ARMOR, -4499);
	}
}
