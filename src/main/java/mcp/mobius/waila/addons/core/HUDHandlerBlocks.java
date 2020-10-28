package mcp.mobius.waila.addons.core;

import java.util.List;

import com.google.common.base.Strings;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITaggableList;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import snownee.jade.JadePlugin;

public class HUDHandlerBlocks implements IComponentProvider {

    static final IComponentProvider INSTANCE = new HUDHandlerBlocks();
    static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation(Waila.MODID, "object_name");
    static final ResourceLocation REGISTRY_NAME_TAG = new ResourceLocation(Waila.MODID, "registry_name");
    static final ResourceLocation MOD_NAME_TAG = new ResourceLocation(Waila.MODID, "mod_name");

    @Override
    public void appendHead(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockState().getMaterial().isLiquid())
            return;

        String name = null;
        if (accessor.getTileEntity() instanceof INamedContainerProvider) {
            name = ((INamedContainerProvider) accessor.getTileEntity()).getDisplayName().getString();
        }
        if (name == null) {
            name = I18n.format(accessor.getBlock().getTranslationKey());
        }
        ((ITaggableList<ResourceLocation, ITextComponent>) tooltip).setTag(OBJECT_NAME_TAG, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getBlockName(), name)));
        if (config.get(PluginCore.CONFIG_SHOW_REGISTRY))
            ((ITaggableList<ResourceLocation, ITextComponent>) tooltip).setTag(REGISTRY_NAME_TAG, new StringTextComponent(accessor.getBlock().getRegistryName().toString()).mergeStyle(TextFormatting.GRAY));
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if (config.get(PluginCore.CONFIG_SHOW_STATES)) {
            BlockState state = accessor.getBlockState();
            state.getProperties().forEach(p -> {
                Comparable<?> value = state.get(p);
                ITextComponent valueText = new StringTextComponent(value.toString()).mergeStyle(p instanceof BooleanProperty ? value == Boolean.TRUE ? TextFormatting.GREEN : TextFormatting.RED : TextFormatting.RESET);
                tooltip.add(new StringTextComponent(p.getName() + ":").append(valueText));
            });
        }
    }

    @Override
    public void appendTail(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if (config.get(JadePlugin.HIDE_MOD_NAME))
            return;
        String modName = ModIdentification.getModInfo(accessor.getStack()).getName();
        if (!Strings.isNullOrEmpty(modName)) {
            modName = String.format(Waila.CONFIG.get().getFormatting().getModName(), modName);
            ((ITaggableList<ResourceLocation, ITextComponent>) tooltip).setTag(MOD_NAME_TAG, new StringTextComponent(modName));
        }
    }
}
