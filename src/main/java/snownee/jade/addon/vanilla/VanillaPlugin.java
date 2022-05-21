package snownee.jade.addon.vanilla;

import net.minecraft.client.Minecraft;
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
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;

@WailaPlugin
public class VanillaPlugin implements IWailaPlugin {

	public static IWailaClientRegistration CLIENT_REGISTRATION;

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(BrewingStandProvider.INSTANCE, BrewingStandBlockEntity.class);
		registration.registerBlockDataProvider(BeehiveProvider.INSTANCE, BeehiveBlockEntity.class);
		registration.registerBlockDataProvider(CommandBlockProvider.INSTANCE, CommandBlockEntity.class);
		registration.registerBlockDataProvider(JukeboxProvider.INSTANCE, JukeboxBlockEntity.class);
		registration.registerBlockDataProvider(LecternProvider.INSTANCE, LecternBlockEntity.class);
		registration.registerBlockDataProvider(RedstoneProvider.INSTANCE, ComparatorBlockEntity.class);
		registration.registerBlockDataProvider(FurnaceProvider.INSTANCE, AbstractFurnaceBlockEntity.class);

		registration.registerEntityDataProvider(ChestedHorseProvider.INSTANCE, AbstractChestedHorse.class);
		registration.registerEntityDataProvider(PotionEffectsProvider.INSTANCE, LivingEntity.class);
		registration.registerEntityDataProvider(MobGrowthProvider.INSTANCE, AgeableMob.class);
		registration.registerEntityDataProvider(MobBreedingProvider.INSTANCE, Animal.class);
		registration.registerEntityDataProvider(ChickenEggProvider.INSTANCE, Chicken.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		CLIENT_REGISTRATION = registration;

		registration.registerBlockComponent(BlockStatesProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(BrewingStandProvider.INSTANCE, BrewingStandBlock.class);
		registration.registerEntityComponent(HorseStatsProvider.INSTANCE, AbstractHorse.class);
		registration.registerEntityComponent(ChestedHorseProvider.INSTANCE, AbstractChestedHorse.class);
		registration.registerEntityComponent(ItemFrameProvider.INSTANCE, ItemFrame.class);
		registration.registerEntityComponent(PotionEffectsProvider.INSTANCE, LivingEntity.class);
		registration.registerEntityComponent(MobGrowthProvider.INSTANCE, AgeableMob.class);
		registration.registerEntityComponent(MobBreedingProvider.INSTANCE, Animal.class);
		registration.registerBlockComponent(TNTStabilityProvider.INSTANCE, TntBlock.class);
		registration.registerBlockComponent(BeehiveProvider.INSTANCE, BeehiveBlock.class);
		registration.registerBlockComponent(NoteBlockProvider.INSTANCE, NoteBlock.class);
		registration.registerEntityComponent(ArmorStandProvider.INSTANCE, ArmorStand.class);
		registration.registerEntityComponent(PaintingProvider.INSTANCE, Painting.class);
		registration.registerEntityComponent(ChickenEggProvider.INSTANCE, Chicken.class);
		registration.registerBlockComponent(HarvestToolProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(CommandBlockProvider.INSTANCE, CommandBlock.class);
		registration.registerBlockComponent(EnchantmentPowerProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(TotalEnchantmentPowerProvider.INSTANCE, EnchantmentTableBlock.class);
		registration.registerBlockComponent(PlayerHeadProvider.INSTANCE, AbstractSkullBlock.class);
		registration.registerEntityComponent(VillagerProfessionProvider.INSTANCE, Villager.class);
		registration.registerEntityComponent(VillagerProfessionProvider.INSTANCE, ZombieVillager.class);
		registration.registerEntityComponent(ItemTooltipProvider.INSTANCE, ItemEntity.class);
		registration.registerBlockComponent(FurnaceProvider.INSTANCE, AbstractFurnaceBlock.class);
		registration.registerEntityComponent(AnimalOwnerProvider.INSTANCE, Entity.class);
		registration.registerEntityComponent(FallingBlockProvider.INSTANCE, FallingBlockEntity.class);
		registration.registerEntityIcon(FallingBlockProvider.INSTANCE, FallingBlockEntity.class);
		registration.registerEntityComponent(EntityHealthProvider.INSTANCE, LivingEntity.class);
		registration.registerEntityComponent(EntityArmorProvider.INSTANCE, LivingEntity.class);
		registration.registerBlockComponent(RedstoneProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(CropProgressProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(JukeboxProvider.INSTANCE, JukeboxBlock.class);
		registration.registerBlockComponent(LecternProvider.INSTANCE, LecternBlock.class);
		registration.registerBlockComponent(MobSpawnerProvider.INSTANCE, SpawnerBlock.class);

		((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(HarvestToolProvider.INSTANCE);

		registration.addConfig(Identifiers.MC_EFFECTIVE_TOOL, true);
		registration.addConfig(Identifiers.MC_HARVEST_TOOL_NEW_LINE, false);
	}

	public static IDisplayHelper getDisplayHelper() {
		return CLIENT_REGISTRATION.getDisplayHelper();
	}

	public static IElementHelper getElementHelper() {
		return CLIENT_REGISTRATION.getElementHelper();
	}

}
