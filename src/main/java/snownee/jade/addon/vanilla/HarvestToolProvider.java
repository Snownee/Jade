package snownee.jade.addon.vanilla;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

public class HarvestToolProvider implements IComponentProvider, ISelectiveResourceReloadListener {

    public static final HarvestToolProvider INSTANCE = new HarvestToolProvider();

    public static final Cache<ToolType, String> toolNames = CacheBuilder.newBuilder().build();
    public static final Cache<BlockState, ToolType> toolCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    public static final List<Pair<ToolType, ItemStack>> testTools = Collections.synchronizedList(Lists.newLinkedList());

    static {
        //        testTools.add(Pair.of(left, right))
    }

    public static String getToolName(ToolType toolType) {
        try {
            return toolNames.get(toolType, () -> {
                if (I18n.hasKey("jade.harvest_tool." + toolType.getName())) {
                    return I18n.format("jade.harvest_tool." + toolType.getName());
                } else {
                    return StringUtils.capitalize(toolType.getName());
                }
            });
        } catch (ExecutionException e) {
            Waila.LOGGER.catching(e);
            return toolType.getName();
        }
    }

    //    public static ToolType getTool(BlockState state) {
    //
    //    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        //        if (!config.get(JadePlugin.HARVEST_TOOL)) {
        //            return;
        //        }
        //        float hardness = accessor.getBlockState().getPlayerRelativeBlockHardness(accessor.getPlayer(), accessor.getWorld(), accessor.getPosition());
        //        if (hardness <= 0) {
        //            tooltip.add(new TranslationTextComponent("jade.harvest_tool.cannot").mergeStyle(TextFormatting.DARK_RED));
        //            return;
        //        }
        //        int level = accessor.getBlockState().getHarvestLevel();
        //
        //        System.out.println(level);
        //        ToolType tool = accessor.getBlockState().getHarvestTool();
        //        System.out.println(tool);
        //        System.out.println(ForgeHooks.canHarvestBlock(accessor.getBlockState(), accessor.getPlayer(), accessor.getWorld(), accessor.getPosition()));
        //        if (tool == null) {
        //            return;
        //        }
        //        String name = getToolName(tool);
        //        boolean canHarvest = ForgeHooks.canHarvestBlock(accessor.getBlockState(), accessor.getPlayer(), accessor.getWorld(), accessor.getPosition());
        //        if (accessor.getBlockState().getRequiresTool()) {
        //            String levelStr = "jade.harvest_tool." + tool.getName() + "." + level;
        //            if (I18n.hasKey(levelStr)) {
        //                levelStr = I18n.format(levelStr);
        //            } else {
        //                levelStr = String.valueOf(level);
        //            }
        //            tooltip.add(new TranslationTextComponent("jade.harvest_tool.fmt", name, levelStr).mergeStyle(canHarvest ? TextFormatting.GREEN : TextFormatting.DARK_RED));
        //        } else {
        //            tooltip.add(new StringTextComponent(name).mergeStyle(canHarvest ? TextFormatting.GREEN : TextFormatting.DARK_RED));
        //        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        if (resourcePredicate.test(VanillaResourceType.LANGUAGES)) {
            toolNames.invalidateAll();
        }
    }

}
