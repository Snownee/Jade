package mcp.mobius.waila.overlay.tooltiprenderers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.api.RenderContext;
import mcp.mobius.waila.overlay.DisplayUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.Dimension;

public class TooltipRendererStack implements ITooltipRenderer {

	@Override
	public Dimension getSize(CompoundNBT tag, ICommonAccessor accessor) {
		float scale = tag.getFloat("scale");
		if (scale == 0)
			scale = 1;
		int size = MathHelper.floor(18 * scale);
		int offsetY = tag.getInt("offsetY");
		return new Dimension(size, size + offsetY);
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

		float scale = tag.getFloat("scale");
		if (scale == 0)
			scale = 1;
		int offsetY = tag.getInt("offsetY");
		DisplayUtil.renderStack(RenderContext.matrixStack, x, y + offsetY, stack, scale);
	}

}
