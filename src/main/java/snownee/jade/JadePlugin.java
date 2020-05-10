package snownee.jade;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.util.ResourceLocation;
import snownee.jade.addon.vanilla.AgeableEntityProvider;
import snownee.jade.addon.vanilla.BreedingProvider;
import snownee.jade.addon.vanilla.BrewingStandProvider;
import snownee.jade.addon.vanilla.ChestedHorseProvider;
import snownee.jade.addon.vanilla.HorseProvider;
import snownee.jade.addon.vanilla.InventoryProvider;
import snownee.jade.addon.vanilla.ItemFrameProvider;
import snownee.jade.addon.vanilla.MiscEntityNameProvider;
import snownee.jade.addon.vanilla.PotionEffectsProvider;

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

        registrar.registerComponentProvider(MiscEntityNameProvider.INSTANCE, TooltipPosition.HEAD, Entity.class);
        registrar.registerEntityStackProvider(MiscEntityNameProvider.INSTANCE, Entity.class);
        registrar.addConfig(MISC_ENTITY, true);
    }

}
