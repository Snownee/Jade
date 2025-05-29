package snownee.jade.api.ui;

import java.util.function.Consumer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.JadeInternals;

public abstract class Element implements Renderable, LayoutElement, NarrationSupplier {

	protected ResourceLocation tag;
	protected int width;
	protected int height;
	private int x;
	private int y;
	private @Nullable Component narration = CommonComponents.EMPTY;

	@Contract("_, _ -> new")
	public ResizeableElement offset(int x, int y) {
		return JadeUI.offset(this, x, y);
	}

	@Contract("_, _ -> new")
	public ResizeableElement size(int width, int height) {
		return JadeUI.size(this, width, height);
	}

	@Contract("_ -> this")
	public Element tag(@Nullable ResourceLocation tag) {
		this.tag = tag;
		return this;
	}

	public @Nullable ResourceLocation getTag() {
		return tag;
	}

	public @Nullable Component cachedNarration() {
		if (narration == CommonComponents.EMPTY) {
			narration = getNarration();
		}
		return narration;
	}

	public abstract @Nullable Component getNarration();

	@Contract("-> this")
	public Element refreshNarration() {
		narration = CommonComponents.EMPTY;
		return this;
	}

	@Contract("_ -> this")
	public Element narration(String narration) {
		Preconditions.checkNotNull(narration, "narration must not be null");
		this.narration = narration.isEmpty() ? null : Component.literal(narration);
		return this;
	}

	@Contract("_ -> this")
	public Element narration(Component narration) {
		Preconditions.checkNotNull(narration, "narration must not be null");
		this.narration = narration;
		return this;
	}

	@Override
	public abstract void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public final int getX() {
		return x;
	}

	@Override
	public final int getY() {
		return y;
	}

	@Override
	public final int getWidth() {
		return width;
	}

	@Override
	public final int getHeight() {
		return height;
	}

	@Override
	public final ScreenRectangle getRectangle() {
		return LayoutElement.super.getRectangle();
	}

	@Override
	public void visitWidgets(Consumer<AbstractWidget> consumer) {
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		Component message = cachedNarration();
		if (message != null) {
			narrationElementOutput.add(NarratedElementType.TITLE, message);
		}
	}

	public void renderDebug(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		JadeInternals.getDisplayHelper().drawBorder(graphics, getRectangle(), 1, 0x88FF0000, true);
	}
}