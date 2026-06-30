package snownee.jade.addon.vanilla;

import java.util.List;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.vehicle.minecart.MinecartSpawner;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.jade.JadeClient;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.harvest.ToolTier;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.overlay.DatapackBlockManager;
import snownee.jade.util.CommonProxy;

@WailaPlugin
public class VanillaPlugin implements IWailaPlugin {

	private static final Cache<BlockState, BlockState> CHEST_CACHE = CacheBuilder.newBuilder().build();

	public static BlockState getCorrespondingNormalChest(BlockState state) {
		try {
			return CHEST_CACHE.get(
					state, () -> {
						Identifier trappedName = CommonProxy.getId(state.getBlock());
						Block block = Blocks.AIR;
						if (trappedName.getPath().startsWith("trapped_")) {
							Identifier chestName = trappedName.withPath(trappedName.getPath().substring(8));
							block = BuiltInRegistries.BLOCK.getValue(chestName);
						} else if (trappedName.getPath().endsWith("_trapped_chest")) {
							Identifier chestName = trappedName.withPath(
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
		for (Property.Value<?> value : oldState.getValues().toList()) {
			Property<T> property = (Property<T>) value.property();
			if (newState.hasProperty(property)) {
				newState = newState.setValue(property, property.getValueClass().cast(value.value()));
			}
		}
		return newState;
	}

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(BrewingStandProvider.INSTANCE, BrewingStandBlockEntity.class);
		registration.registerBlockDataProvider(BeehiveProvider.INSTANCE, BeehiveBlockEntity.class);
		registration.registerBlockDataProvider(CommandBlockProvider.INSTANCE, CommandBlockEntity.class);
		registration.registerBlockDataProvider(HopperLockProvider.INSTANCE, HopperBlockEntity.class);
		registration.registerBlockDataProvider(JukeboxProvider.INSTANCE, JukeboxBlockEntity.class);
		registration.registerBlockDataProvider(LecternProvider.INSTANCE, LecternBlockEntity.class);
		registration.registerBlockDataProvider(RedstoneProvider.INSTANCE, ComparatorBlockEntity.class);
		registration.registerBlockDataProvider(RedstoneProvider.INSTANCE, CalibratedSculkSensorBlockEntity.class);
		registration.registerBlockDataProvider(FurnaceProvider.INSTANCE, AbstractFurnaceBlockEntity.class);
		registration.registerBlockDataProvider(ShelfProvider.INSTANCE, ChiseledBookShelfBlockEntity.class);
		registration.registerBlockDataProvider(ShelfProvider.INSTANCE, ShelfBlockEntity.class);
		registration.registerBlockDataProvider(MobSpawnerCooldownProvider.INSTANCE, TrialSpawnerBlockEntity.class);

		registration.registerEntityDataProvider(AnimalOwnerProvider.INSTANCE, Entity.class);
		registration.registerEntityDataProvider(StatusEffectsProvider.INSTANCE, LivingEntity.class);
		registration.registerEntityDataProvider(MobGrowthProvider.INSTANCE, AgeableMob.class);
		registration.registerEntityDataProvider(MobGrowthProvider.INSTANCE, Tadpole.class);
		registration.registerEntityDataProvider(MobBreedingProvider.INSTANCE, Animal.class);
		registration.registerEntityDataProvider(MobBreedingProvider.INSTANCE, Villager.class);
		registration.registerEntityDataProvider(MobBreedingProvider.INSTANCE, Allay.class);
		registration.registerEntityDataProvider(NextEntityDropProvider.INSTANCE, Chicken.class);
		registration.registerEntityDataProvider(NextEntityDropProvider.INSTANCE, Armadillo.class);
		registration.registerEntityDataProvider(NextEntityDropProvider.INSTANCE, Sniffer.class);
		registration.registerEntityDataProvider(ZombieVillagerProvider.INSTANCE, ZombieVillager.class);
		registration.registerEntityDataProvider(PetArmorProvider.INSTANCE, Mob.class);
		registration.registerEntityDataProvider(WaxedProvider.EntityData.INSTANCE, CopperGolem.class);
		registration.registerEntityDataProvider(EntityHealthAndArmorProvider.INSTANCE, LivingEntity.class);

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
		registration.addConfig(JadeIds.MC_POTION_EFFECTS_LIMIT, 7, 1, 99, false);

		registration.registerBlockComponent(BrewingStandProvider.Client.INSTANCE, BrewingStandBlock.class);
		registration.registerEntityComponent(HorseStatsProvider.INSTANCE, AbstractHorse.class);
		registration.registerEntityComponent(ItemFrameProvider.INSTANCE, ItemFrame.class);
		registration.registerEntityComponent(StatusEffectsProvider.Client.INSTANCE, LivingEntity.class);
		registration.registerEntityComponent(MobGrowthProvider.Client.INSTANCE, AgeableMob.class);
		registration.registerEntityComponent(MobGrowthProvider.Client.INSTANCE, Tadpole.class);
		registration.registerEntityComponent(MobBreedingProvider.Client.INSTANCE, Animal.class);
		registration.registerEntityComponent(MobBreedingProvider.Client.INSTANCE, Villager.class);
		registration.registerEntityComponent(MobBreedingProvider.Client.INSTANCE, Allay.class);
		registration.registerBlockComponent(TNTStabilityProvider.INSTANCE, TntBlock.class);
		registration.registerBlockComponent(BeehiveProvider.Client.INSTANCE, BeehiveBlock.class);
		registration.registerBlockComponent(NoteBlockProvider.INSTANCE, NoteBlock.class);
		registration.registerEntityComponent(ArmorStandProvider.INSTANCE, ArmorStand.class);
		registration.registerEntityComponent(PaintingProvider.INSTANCE, Painting.class);
		registration.registerEntityComponent(NextEntityDropProvider.Client.INSTANCE, Chicken.class);
		registration.registerEntityComponent(NextEntityDropProvider.Client.INSTANCE, Armadillo.class);
		registration.registerEntityComponent(NextEntityDropProvider.Client.INSTANCE, Sniffer.class);
		registration.registerBlockComponent(HarvestToolProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(CommandBlockProvider.Client.INSTANCE, CommandBlock.class);
		registration.registerBlockComponent(EnchantmentPowerProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(TotalEnchantmentPowerProvider.INSTANCE, EnchantingTableBlock.class);
		registration.registerBlockComponent(PlayerHeadProvider.INSTANCE, AbstractSkullBlock.class);
		registration.registerBlockIcon(ItemBERProvider.INSTANCE, AbstractSkullBlock.class);
		registration.registerBlockIcon(ItemBERProvider.INSTANCE, DecoratedPotBlock.class);
		registration.registerEntityComponent(VillagerProfessionProvider.INSTANCE, Villager.class);
		registration.registerEntityComponent(VillagerProfessionProvider.INSTANCE, ZombieVillager.class);
		registration.registerEntityComponent(ItemTooltipProvider.INSTANCE, ItemEntity.class);
		registration.registerBlockComponent(FurnaceProvider.Client.INSTANCE, AbstractFurnaceBlock.class);
		registration.registerEntityComponent(AnimalOwnerProvider.Client.INSTANCE, Entity.class);
		registration.registerEntityIcon(FallingBlockProvider.INSTANCE, FallingBlockEntity.class);
		registration.registerEntityComponent(EntityHealthAndArmorProvider.Client.INSTANCE, LivingEntity.class);
		registration.registerBlockComponent(RedstoneProvider.Client.INSTANCE, Block.class);
		registration.registerBlockComponent(HopperLockProvider.Client.INSTANCE, HopperBlock.class);
		registration.registerBlockComponent(CropProgressProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(JukeboxProvider.Client.INSTANCE, JukeboxBlock.class);
		registration.registerBlockComponent(LecternProvider.Client.INSTANCE, LecternBlock.class);
		registration.registerBlockComponent(MobSpawnerProvider.ForBlock.INSTANCE, SpawnerBlock.class);
		registration.registerBlockComponent(MobSpawnerProvider.ForBlock.INSTANCE, TrialSpawnerBlock.class);
		registration.registerEntityComponent(MobSpawnerProvider.ForEntity.INSTANCE, MinecartSpawner.class);
		registration.registerBlockComponent(MobSpawnerCooldownProvider.Client.INSTANCE, TrialSpawnerBlock.class);
		registration.registerBlockComponent(ShelfProvider.Client.INSTANCE, ChiseledBookShelfBlock.class);
		registration.registerBlockIcon(ShelfProvider.Client.INSTANCE, ChiseledBookShelfBlock.class);
		registration.registerBlockComponent(ShelfProvider.Client.INSTANCE, ShelfBlock.class);
		registration.registerBlockIcon(ShelfProvider.Client.INSTANCE, ShelfBlock.class);
		registration.registerEntityIcon(ItemDisplayProvider.INSTANCE, ItemDisplay.class);
		registration.registerEntityIcon(BlockDisplayProvider.INSTANCE, BlockDisplay.class);
		registration.registerEntityComponent(ZombieVillagerProvider.Client.INSTANCE, ZombieVillager.class);
		registration.registerBlockComponent(WaxedProvider.BlockComponent.INSTANCE, SignBlock.class);
		registration.registerBlockIcon(WaxedProvider.BlockComponent.INSTANCE, SignBlock.class);
		registration.registerEntityComponent(WaxedProvider.EntityComponent.INSTANCE, CopperGolem.class);
		registration.registerEntityIcon(WaxedProvider.EntityComponent.INSTANCE, CopperGolem.class);
		registration.registerEntityComponent(PetArmorProvider.Client.INSTANCE, Mob.class);
		registration.registerEntityComponent(SulfurCubeProvider.INSTANCE, SulfurCube.class);

		registration.registerItemStorageClient(CampfireProvider.INSTANCE);

		registration.addRayTraceCallback(-10010, DatapackBlockManager::override);
		registration.addRayTraceCallback(-1000, JadeClient::limitMobEffectFog);
		registration.addRayTraceCallback(-10, JadeClient::builtInOverrides);
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
		registration.markAsClientFeature(JadeIds.MC_ENTITY_ARMOR);
		registration.markAsClientFeature(JadeIds.MC_CROP_PROGRESS);
		registration.markAsClientFeature(JadeIds.MC_MOB_SPAWNER);
		registration.markAsClientFeature(JadeIds.MC_WAXED);
		registration.markAsClientFeature(JadeIds.MC_SULFUR_CUBE);

		Component block = Component.translatable("config.jade.plugin_minecraft.block");
		Component entity = Component.translatable("config.jade.plugin_minecraft.entity");
		List<Component> both = List.of(block, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_ANIMAL_OWNER, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_ARMOR_STAND, both);
		registration.setConfigCategoryOverride(JadeIds.MC_BEEHIVE, block);
		registration.setConfigCategoryOverride(JadeIds.MC_BREAKING_PROGRESS, block);
		registration.setConfigCategoryOverride(JadeIds.MC_BREWING_STAND, block);
		registration.setConfigCategoryOverride(JadeIds.MC_SHELF, block);
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
		registration.setConfigCategoryOverride(JadeIds.MC_WAXED, both);
		registration.setConfigCategoryOverride(JadeIds.MC_ZOMBIE_VILLAGER, entity);
		registration.setConfigCategoryOverride(JadeIds.MC_SULFUR_CUBE, entity);

		WailaCommonRegistration.instance().priorities.putUnsafe(JadeIds.MC_ENTITY_ARMOR, -4499);

		registration.addHarvestPlugin(registry -> {
			registry.type(JadeIds.JADE("pickaxe"))
					.addTier(ToolTier.item(Items.WOODEN_PICKAXE))
					.addTier(ToolTier.item(Items.GOLDEN_PICKAXE))
					.addTier(ToolTier.item(Items.STONE_PICKAXE))
					.addTier(ToolTier.item(Items.IRON_PICKAXE))
					.addTier(ToolTier.item(Items.DIAMOND_PICKAXE))
					.addTier(ToolTier.item(Items.NETHERITE_PICKAXE));
			registry.type(JadeIds.JADE("axe"))
					.addTier(ToolTier.item(Items.WOODEN_AXE))
					.addTier(ToolTier.item(Items.GOLDEN_AXE))
					.addTier(ToolTier.item(Items.STONE_AXE))
					.addTier(ToolTier.item(Items.IRON_AXE))
					.addTier(ToolTier.item(Items.DIAMOND_AXE))
					.addTier(ToolTier.item(Items.NETHERITE_AXE));
			registry.type(JadeIds.JADE("shovel"))
					.addTier(ToolTier.item(Items.WOODEN_SHOVEL))
					.addTier(ToolTier.item(Items.GOLDEN_SHOVEL))
					.addTier(ToolTier.item(Items.STONE_SHOVEL))
					.addTier(ToolTier.item(Items.IRON_SHOVEL))
					.addTier(ToolTier.item(Items.DIAMOND_SHOVEL))
					.addTier(ToolTier.item(Items.NETHERITE_SHOVEL));
			registry.type(JadeIds.JADE("hoe"))
					.addTier(ToolTier.item(Items.WOODEN_HOE))
					.addTier(ToolTier.item(Items.GOLDEN_HOE))
					.addTier(ToolTier.item(Items.STONE_HOE))
					.addTier(ToolTier.item(Items.IRON_HOE))
					.addTier(ToolTier.item(Items.DIAMOND_HOE))
					.addTier(ToolTier.item(Items.NETHERITE_HOE));
			registry.type(JadeIds.JADE("sword"))
					.addTier(ToolTier.item(Items.WOODEN_SWORD)
							.addExtraBlocks(List.of(Blocks.BAMBOO, Blocks.BAMBOO_SAPLING)));
			registry.type(JadeIds.JADE("shears"))
					.addTier(registry.defaultShearsTier());
		});
	}
}
