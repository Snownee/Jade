package snownee.jade;

import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.item.ItemStack;

public class Util
{
    private Util()
    {
    }

    public static String wailaStack(ItemStack stack)
    {
        String name = stack.getItem().getRegistryName().toString();
        String count = String.valueOf(stack.getCount());
        String damage = String.valueOf(stack.getItemDamage());
        String nbt = stack.hasTagCompound() ? stack.getTagCompound().toString() : "";
        return SpecialChars.getRenderString("waila.stack", "1", name, count, damage, nbt);
    }

    public static String offsetText(String s, int x, int y)
    {
        return SpecialChars.getRenderString("jade.text", s, Integer.toString(x), Integer.toString(y));
    }

    public static String span(int x, int y)
    {
        return SpecialChars.getRenderString("jade.span", Integer.toString(x), Integer.toString(y));
    }
}
