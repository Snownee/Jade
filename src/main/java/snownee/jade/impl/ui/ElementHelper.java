package snownee.jade.impl.ui;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ITooltip;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.IBoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;
import snownee.jade.impl.Tooltip;

public class ElementHelper implements IElementHelper {
	public static final ElementHelper INSTANCE = new ElementHelper();
	public static final Vec2 SMALL_ITEM_SIZE = new Vec2(10, 10);
	public static final Vec2 SMALL_ITEM_OFFSET = Vec2.NEG_UNIT_Y;
	private ResourceLocation uid;

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
	public IElement smallItem(ItemStack stack) {
		return item(stack, 0.5F).size(SMALL_ITEM_SIZE).translate(SMALL_ITEM_OFFSET).message(null);
	}

	@Override
	public IElement fluid(JadeFluidObject fluid) {
		return new FluidStackElement(fluid);
	}

	@Override
	public IElement spacer(int width, int height) {
		return new SpacerElement(new Vec2(width, height));
	}

	@Override
	public IElement progress(float progress, @Nullable Component text, IProgressStyle style, IBoxStyle boxStyle, boolean canDecrease) {
		Objects.requireNonNull(style);
		Objects.requireNonNull(boxStyle);
		return new ProgressElement(progress, text, style, boxStyle, canDecrease);
	}

	@Override
	public IBoxElement box(ITooltip tooltip, IBoxStyle boxStyle) {
		Objects.requireNonNull(boxStyle);
		return new BoxElement((Tooltip) tooltip, boxStyle);
	}

	@Override
	public ITooltip tooltip() {
		return new Tooltip();
	}

	@Override
	public IProgressStyle progressStyle() {
		return new ProgressStyle();
	}

	@Nullable
	public ResourceLocation currentUid() {
		return uid;
	}

	public void setCurrentUid(ResourceLocation uid) {
		this.uid = uid;
	}

	//
	//    public static IElement sub(String text) {
	//        CompoundTag tag = new CompoundTag();
	//        tag.putString("text", text);
	//        return new RenderableTextComponent(SUB, tag);
	//    }

}
