package snownee.jade;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityAnimal;
import snownee.jade.providers.AgeableEntityProvider;
import snownee.jade.providers.BreedingProvider;
import snownee.jade.providers.BrewingStandProvider;
import snownee.jade.providers.ChestHorseProvider;
import snownee.jade.providers.HorseProvider;
import snownee.jade.providers.ItemFrameProvider;
import snownee.jade.providers.ItemHandlerProvider;
import snownee.jade.providers.PotionEffectsProvider;

@WailaPlugin
public class JadePlugin implements IWailaPlugin
{

    @Override
    public void register(IWailaRegistrar registrar)
    {
        registrar.registerBodyProvider(ItemHandlerProvider.INSTANCE, Block.class);
        registrar.registerNBTProvider(ItemHandlerProvider.INSTANCE, Block.class);
        registrar.addConfig("Capability", "capability.inventoryinfo", true);

        registrar.registerBodyProvider(BrewingStandProvider.INSTANCE, BlockBrewingStand.class);
        registrar.registerNBTProvider(BrewingStandProvider.INSTANCE, BlockBrewingStand.class);
        registrar.addConfig("Jade", "jade.brewingstand", true);

        registrar.registerBodyProvider(HorseProvider.INSTANCE, AbstractHorse.class);
        registrar.addConfig("Jade", "jade.horsestat", true);

        registrar.registerBodyProvider(ChestHorseProvider.INSTANCE, AbstractChestHorse.class);
        registrar.addConfig("Jade", "jade.horsechest", true);

        registrar.registerBodyProvider(ItemFrameProvider.INSTANCE, EntityItemFrame.class);
        registrar.addConfig("Jade", "jade.itemframe", true);

        registrar.registerBodyProvider(PotionEffectsProvider.INSTANCE, EntityLivingBase.class);
        registrar.registerNBTProvider(PotionEffectsProvider.INSTANCE, EntityLivingBase.class);
        registrar.addConfig("Jade", "jade.potioneffects", true);

        registrar.registerBodyProvider(AgeableEntityProvider.INSTANCE, EntityAgeable.class);
        registrar.registerNBTProvider(AgeableEntityProvider.INSTANCE, EntityAgeable.class);
        registrar.addConfig("Jade", "jade.mobgrowth", true);

        registrar.registerBodyProvider(BreedingProvider.INSTANCE, EntityAnimal.class);
        registrar.registerNBTProvider(BreedingProvider.INSTANCE, EntityAnimal.class);
        registrar.addConfig("Jade", "jade.mobbreeding", true);
    }

}
