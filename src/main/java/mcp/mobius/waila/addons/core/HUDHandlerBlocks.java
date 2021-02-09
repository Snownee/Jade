package mcp.mobius.waila.addons.core;

import java.util.List;

import com.google.common.base.Strings;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITaggableList;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import snownee.jade.JadeCommonConfig;
import snownee.jade.JadePlugin;

public class HUDHandlerBlocks implements IComponentProvider, IServerDataProvider<TileEntity> {

    static final HUDHandlerBlocks INSTANCE = new HUDHandlerBlocks();
    static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation(Waila.MODID, "object_name");
    static final ResourceLocation REGISTRY_NAME_TAG = new ResourceLocation(Waila.MODID, "registry_name");
    static final ResourceLocation MOD_NAME_TAG = new ResourceLocation(Waila.MODID, "mod_name");

    @Override
    public void appendHead(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockState().getMaterial().isLiquid())
            return;

        String name;
        if (accessor.getServerData().contains("givenName", Constants.NBT.TAG_STRING)) {
            ITextComponent component = ITextComponent.Serializer.getComponentFromJson(accessor.getServerData().getString("givenName"));
            name = component.getString();
        } else {
            String key = accessor.getBlock().getTranslationKey();
            if (I18n.hasKey(key)) {
                name = I18n.format(key);
            } else {
                ItemStack stack = accessor.getBlockState().getPickBlock(accessor.getHitResult(), accessor.getWorld(), accessor.getPosition(), accessor.getPlayer());
                if (stack != null && !stack.isEmpty()) {
                    name = stack.getDisplayName().getString();
                } else {
                    name = key;
                }
            }
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

    @Override
    public void appendServerData(CompoundNBT data, ServerPlayerEntity player, World world, TileEntity t) {
        if (t instanceof INameable && JadeCommonConfig.shouldShowCustomName(t)) {
            INameable nameable = (INameable) t;
            if (nameable.hasCustomName()) {
                data.putString("givenName", ITextComponent.Serializer.toJson(nameable.getCustomName()));
            }
        }
    }
}
