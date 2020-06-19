package snownee.jade.addon.vanilla;

import java.util.List;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.JadePlugin;

public class AccurateNameProvider implements IComponentProvider {

    public static final AccurateNameProvider INSTANCE = new AccurateNameProvider();

    private static final Cache<Block, ITextComponent> CACHE = CacheBuilder.newBuilder().build();

    private static final ITextComponent DEFAULT_NAME = new TranslationTextComponent(Blocks.CHEST.getTranslationKey());

    @Override
    public void appendHead(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof TrappedChestBlock && config.get(JadePlugin.TRAPPED_CHEST)) {
            try {
                ITextComponent name = CACHE.get(accessor.getBlock(), () -> {
                    ResourceLocation trappedName = accessor.getBlock().getRegistryName();
                    if (trappedName.getPath().startsWith("trapped_")) {
                        ResourceLocation chestName = new ResourceLocation(trappedName.getNamespace(), trappedName.getPath().substring(8));
                        Block block = ForgeRegistries.BLOCKS.getValue(chestName);
                        if (block != null) {
                            return block.getNameTextComponent();
                        }
                    }
                    return DEFAULT_NAME;
                });
                tooltip.clear();
                tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getBlockName(), name.getFormattedText())));
            } catch (Exception e) {}
            return;
        }
        if (!tooltip.isEmpty() && config.get(JadePlugin.ACCURATE_NAME)) {
            tooltip.set(0, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getBlockName(), I18n.format(accessor.getBlock().getTranslationKey()))));
        }
    }

}
