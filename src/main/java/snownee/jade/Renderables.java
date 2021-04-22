package snownee.jade;

import mcp.mobius.waila.api.RenderableTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import snownee.jade.util.HackyRenderableTextComponent;
import snownee.jade.util.HackyTextComponentNBT;

public final class Renderables {
	private Renderables() {
	}

	static final ResourceLocation ITEM = new ResourceLocation("item");
	static final ResourceLocation SPACER = new ResourceLocation("spacer");

	static final ResourceLocation OFFSET_TEXT = new ResourceLocation(Jade.MODID, "text");
	static final ResourceLocation BORDER = new ResourceLocation(Jade.MODID, "border");
	static final ResourceLocation SUB = new ResourceLocation(Jade.MODID, "sub");

	public static RenderableTextComponent item(ItemStack stack) {
		return item(stack, 1, 0);
	}

	public static RenderableTextComponent item(ItemStack stack, float scale, int offsetY) {
		if (!stack.isEmpty()) {
			CompoundNBT tag = new CompoundNBT();
			tag.putString("id", stack.getItem().getRegistryName().toString());
			tag.putInt("count", stack.getCount());
			if (stack.hasTag())
				tag.putString("nbt", stack.getTag().toString());
			tag.putFloat("scale", scale);
			tag.putInt("offsetY", offsetY);
			return new RenderableTextComponent(ITEM, tag);
		} else {
			return spacer(18, 0);
		}
	}

	public static RenderableTextComponent offsetText(ITextComponent s, int x, int y) {
		return offsetText(s.getString(), x, y);
	}

	public static RenderableTextComponent offsetText(String s, int x, int y) {
		CompoundNBT tag = new CompoundNBT();
		tag.putString("text", s);
		tag.putInt("x", x);
		tag.putInt("y", y);
		return new RenderableTextComponent(OFFSET_TEXT, tag);
	}

	public static RenderableTextComponent spacer(int width, int height) {
		CompoundNBT spacerTag = new CompoundNBT();
		spacerTag.putInt("width", width);
		spacerTag.putInt("height", height);
		return new RenderableTextComponent(SPACER, spacerTag);
	}

	public static RenderableTextComponent box(ITextComponent... components) {
		CompoundNBT tag = new CompoundNBT();
		ListNBT renderData = new ListNBT();
		for (ITextComponent component : components)
			renderData.add(new HackyTextComponentNBT(component));
		tag.put("in", renderData);
		return new HackyRenderableTextComponent(BORDER, tag);
	}

	public static RenderableTextComponent sub(String text) {
		CompoundNBT tag = new CompoundNBT();
		tag.putString("text", text);
		return new RenderableTextComponent(SUB, tag);
	}

	public static RenderableTextComponent of(ITextComponent... components) {
		return new RenderableTextComponent(components);
	}

}
