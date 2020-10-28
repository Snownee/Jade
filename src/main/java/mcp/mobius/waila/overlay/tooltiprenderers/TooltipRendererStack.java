package mcp.mobius.waila.overlay.tooltiprenderers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.overlay.DisplayUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.Dimension;

public class TooltipRendererStack implements ITooltipRenderer {

    @Override
    public Dimension getSize(CompoundNBT tag, ICommonAccessor accessor) {
        return new Dimension(18, 18);
    }

    @Override
    public void draw(CompoundNBT tag, ICommonAccessor accessor, int x, int y) {
        int count = tag.getInt("count");
        if (count <= 0)
            return;

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("id")));
        if (item == Items.AIR)
            return;

        CompoundNBT stackTag = null;
        try {
            stackTag = JsonToNBT.getTagFromJson(tag.getString("nbt"));
        } catch (CommandSyntaxException e) {
            // No-op
        }

        ItemStack stack = new ItemStack(item, count);
        if (stackTag != null)
            stack.setTag(stackTag);

        DisplayUtil.renderStack(x, y, stack);
    }

}
