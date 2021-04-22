package mcp.mobius.waila.overlay;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.ITaggableList;
import mcp.mobius.waila.api.RenderContext;
import mcp.mobius.waila.api.RenderableTextComponent;
import mcp.mobius.waila.api.event.WailaTooltipEvent;
import mcp.mobius.waila.api.impl.DataAccessor;
import mcp.mobius.waila.api.impl.TaggableList;
import mcp.mobius.waila.api.impl.TaggedTextComponent;
import mcp.mobius.waila.api.impl.config.WailaConfig;
import mcp.mobius.waila.api.impl.config.WailaConfig.ConfigOverlay;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;

public class Tooltip {

	private final Minecraft client;
	private final List<Line> lines;
	private final boolean showItem;
	private final Dimension totalSize;
	ItemStack identifierStack;

	public Tooltip(List<ITextComponent> components, boolean showItem) {
		WailaTooltipEvent event = new WailaTooltipEvent(components, DataAccessor.INSTANCE);
		MinecraftForge.EVENT_BUS.post(event);

		this.client = Minecraft.getInstance();
		this.lines = Lists.newArrayList();
		this.showItem = showItem;
		this.totalSize = new Dimension();

		computeLines(components);
		addPadding();
	}

	public void computeLines(List<ITextComponent> components) {
		components.forEach(c -> {
			Dimension size = getLineSize(c, components);
			totalSize.setSize(Math.max(totalSize.width, size.width), totalSize.height + size.height);
			ITextComponent component = c;
			if (component instanceof TaggedTextComponent)
				component = ((ITaggableList<ResourceLocation, ITextComponent>) components).getTag(((TaggedTextComponent) component).getTag());

			lines.add(new Line(component, size));
		});
	}

	public void addPadding() {
		totalSize.width += hasItem() ? 30 : 10;
		totalSize.height += 8;
	}

	public void draw() {
		Rectangle position = getPosition();
		WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();

		int x = position.x + (hasItem() ? 26 : 6);
		position.width += hasItem() ? 24 : 4;
		int y = position.y + 6;

		MatrixStack matrixStack = RenderContext.matrixStack;

		for (Line line : lines) {
			if (line.getComponent() instanceof RenderableTextComponent) {
				RenderableTextComponent component = (RenderableTextComponent) line.getComponent();
				int xOffset = 0;
				for (RenderableTextComponent.RenderContainer container : component.getRenderers()) {
					Dimension size = container.getRenderer().getSize(container.getData(), DataAccessor.INSTANCE);
					container.getRenderer().draw(container.getData(), DataAccessor.INSTANCE, x + xOffset, y);
					xOffset += size.width;
				}
			} else {
				RenderSystem.enableAlphaTest(); // Snownee: why?
				IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
				client.fontRenderer.drawEntityText(line.getComponent().func_241878_f(), x, y, color.getFontColor(), true, matrixStack.getLast().getMatrix(), irendertypebuffer$impl, false, 0, 15728880);
				irendertypebuffer$impl.finish();
			}
			y += line.size.height;
		}
	}

	private Dimension getLineSize(ITextComponent component, List<ITextComponent> components) {
		if (component instanceof RenderableTextComponent) {
			RenderableTextComponent renderable = (RenderableTextComponent) component;
			List<RenderableTextComponent.RenderContainer> renderers = renderable.getRenderers();
			if (renderers.isEmpty())
				return new Dimension(0, 0);

			int width = 0;
			int height = 0;
			for (RenderableTextComponent.RenderContainer container : renderers) {
				Dimension iconSize = container.getRenderer().getSize(container.getData(), DataAccessor.INSTANCE);
				width += iconSize.width;
				height = Math.max(height, iconSize.height);
			}

			return new Dimension(width, height);
		} else if (component instanceof TaggedTextComponent) {
			TaggedTextComponent tagged = (TaggedTextComponent) component;
			if (components instanceof TaggableList) {
				ITextComponent taggedLine = ((TaggableList<ResourceLocation, ITextComponent>) components).getTag(tagged.getTag());
				return taggedLine == null ? new Dimension(0, 0) : getLineSize(taggedLine, components);
			}
		}

		return new Dimension(client.fontRenderer.getStringWidth(component.getString()), client.fontRenderer.FONT_HEIGHT + 1);
	}

	public List<Line> getLines() {
		return lines;
	}

	public boolean hasItem() {
		return showItem && Waila.CONFIG.get().getGeneral().shouldShowItem() && !RayTracing.INSTANCE.getIdentifierStack().isEmpty();
	}

	public Rectangle getPosition() {
		MainWindow window = Minecraft.getInstance().getMainWindow();
		ConfigOverlay overlay = Waila.CONFIG.get().getOverlay();
		int x = (int) (window.getScaledWidth() * overlay.tryFlip(overlay.getOverlayPosX()) - totalSize.width * overlay.tryFlip(overlay.getAnchorX()));
		int y = (int) (window.getScaledHeight() * (1.0F - overlay.getOverlayPosY()) - totalSize.height * overlay.getAnchorY());
		return new Rectangle(x, y, totalSize.width, totalSize.height);
	}

	public static class Line {

		private final ITextComponent component;
		private final Dimension size;

		public Line(ITextComponent component, Dimension size) {
			this.component = component;
			this.size = size;
		}

		public ITextComponent getComponent() {
			return component;
		}

		public Dimension getSize() {
			return size;
		}
	}
}
