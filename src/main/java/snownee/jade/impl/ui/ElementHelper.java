package snownee.jade.impl.ui;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.IBorderStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;
import snownee.jade.impl.Tooltip;

public class ElementHelper implements IElementHelper {
	public static final ElementHelper INSTANCE = new ElementHelper();

	@Override
	public IElement text(Component component) {
		return new TextElement(component);
	}

	@Override
	public IElement item(ItemStack stack) {
		return ItemStackElement.of(stack);
	}

	@Override
	public IElement item(ItemStack stack, float scale) {
		return ItemStackElement.of(stack, scale);
	}

	@Override
	public IElement item(ItemStack stack, float scale, String text) {
		return ItemStackElement.of(stack, scale, text);
	}

	@Override
	public IElement fluid(FluidStack fluidStack) {
		return new FluidStackElement(fluidStack);
	}

	@Override
	public IElement spacer(int width, int height) {
		return new SpacerElement(new Vec2(width, height));
	}

	@Override
	public IElement progress(float progress, @Nullable Component text, IProgressStyle style, @Nullable IBorderStyle borderStyle) {
		Objects.requireNonNull(style);
		return new ProgressElement(progress, text, (ProgressStyle) style, (BorderStyle) borderStyle);
	}

	@Override
	public IElement box(ITooltip tooltip, @Nullable IBorderStyle border) {
		return new BoxElement((Tooltip) tooltip, (BorderStyle) border);
	}

	@Override
	public ITooltip tooltip() {
		return new Tooltip();
	}

	@Override
	public IBorderStyle borderStyle() {
		return new BorderStyle();
	}

	@Override
	public IProgressStyle progressStyle() {
		return new ProgressStyle();
	}

	//
	//    public static IElement sub(String text) {
	//        CompoundTag tag = new CompoundTag();
	//        tag.putString("text", text);
	//        return new RenderableTextComponent(SUB, tag);
	//    }

}
