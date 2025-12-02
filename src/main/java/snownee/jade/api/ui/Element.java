package snownee.jade.api.ui;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Contract;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.Message;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import snownee.jade.JadeInternals;
import snownee.jade.gui.JadeLinearLayout;
import snownee.jade.impl.ui.JadeUIInternal;
import snownee.jade.overlay.DisplayHelper;

public abstract class Element implements Renderable, LayoutElement, NarrationSupplier, GuiEventListener, CopyBehavior {

	protected @Nullable Identifier tag;
	protected int width;
	protected int height;
	private int x;
	private int y;
	private @Nullable Component narration = CommonComponents.EMPTY;
	private @Nullable UnaryOperator<LayoutSettings> settings;
	private JadeLinearLayout.@Nullable Align alignSelf;

	@Contract("_, _ -> new")
	public ResizeableElement offset(int x, int y) {
		return JadeUIInternal.offset(this, x, y);
	}

	@Contract("_, _ -> new")
	public ResizeableElement size(int width, int height) {
		return JadeUIInternal.size(this, width, height);
	}

	@Contract("_ -> new")
	public ResizeableElement onClick(Predicate<Element> onClick) {
		return JadeUIInternal.onClick(this, onClick);
	}

	@Contract("_ -> this")
	public Element settings(UnaryOperator<LayoutSettings> settings) {
		this.settings = settings;
		return this;
	}

	public @Nullable UnaryOperator<LayoutSettings> getSettings() {
		return settings;
	}

	@Contract("-> this")
	public Element alignSelfStart() {
		alignSelf = JadeLinearLayout.Align.START;
		return this;
	}

	@Contract("-> this")
	public Element alignSelfCenter() {
		alignSelf = JadeLinearLayout.Align.CENTER;
		return this;
	}

	@Contract("-> this")
	public Element alignSelfEnd() {
		alignSelf = JadeLinearLayout.Align.END;
		return this;
	}

	@Contract("-> this")
	public Element alignSelfStretch() {
		alignSelf = JadeLinearLayout.Align.STRETCH;
		return this;
	}

	public JadeLinearLayout.@Nullable Align getAlignSelf() {
		return alignSelf;
	}

	@Contract("_ -> this")
	public Element tag(@Nullable Identifier tag) {
		this.tag = tag;
		return this;
	}

	public @Nullable Identifier getTag() {
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

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= getX() && mouseX < getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getHeight();
	}

	@Override
	public void setFocused(boolean bl) {}

	@Override
	public boolean isFocused() {
		return false;
	}

	@Override
	public boolean copyToClipboard(KeyboardHandler keyboardHandler) {
		if (this instanceof Message message) {
			keyboardHandler.setClipboard(message.getString());
			return true;
		}
		Component component = cachedNarration();
		if (component != null) {
			keyboardHandler.setClipboard(component.getString());
			return true;
		}
		return false;
	}

	public void renderDebug(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, RenderDebugContext context) {
		JadeInternals.getDisplayHelper().drawBorder(graphics, getRectangle(), 1, 0x88FF0000, true);
		if (JadeUI.hasAltDown() && getTag() != null) {
			int centerX = context.root.getX() + context.root.getWidth() / 2;
			int x = getX();
			int y = getY();
			String s = getTag().toString();
			int textWidth = DisplayHelper.font().width(s);
			Matrix3x2fStack pose = graphics.pose();
			pose.pushMatrix();
			pose.translate(x, y);
			pose.scale(0.5F);
			if (x > centerX) {
				pose.translate(getWidth() + getWidth(), 0);
			} else {
				pose.translate(-textWidth - 4, 0);
			}
			graphics.fill(0, 0, textWidth + 4, DisplayHelper.font().lineHeight + 4, 0x88000000);
			graphics.drawString(DisplayHelper.font(), s, 2, 2, 0xFFFFFFFF, false);
			pose.popMatrix();
		}
	}

	public static void setHoverEffect(GuiGraphics graphics, Component component) {
		setHoverEffect(graphics, new HoverEvent.ShowText(component));
	}

	public static void setHoverEffect(GuiGraphics graphics, HoverEvent event) {
		graphics.hoveredTextStyle = Style.EMPTY.withHoverEvent(event);
	}

	public static class RenderDebugContext {
		public final LayoutElement root;
		public final Rect2f rootRect;
		public final boolean renderChildren;

		public RenderDebugContext(LayoutElement root, Rect2f rootRect, boolean renderChildren) {
			this.root = root;
			this.rootRect = rootRect;
			this.renderChildren = renderChildren;
		}
	}
}