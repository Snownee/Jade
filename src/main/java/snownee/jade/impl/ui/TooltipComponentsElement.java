//package mcp.mobius.waila.impl.ui;
//
//import java.util.List;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.Tesselator;
//
//import mcp.mobius.waila.api.ui.Element;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.entity.ItemRenderer;
//import net.minecraft.world.phys.Vec2;
//
//public class TooltipComponentsElement extends Element {
//
//	private final List<ClientTooltipComponent> components;
//
//	public TooltipComponentsElement(List<ClientTooltipComponent> components) {
//		this.components = components;
//	}
//
//	@Override
//	public Vec2 getSize() {
//		if (components.isEmpty()) {
//			return Vec2.ZERO;
//		}
//		Font font = Minecraft.getInstance().font;
//		int width = 0;
//		int height = -2;
//		for (ClientTooltipComponent component : components) {
//			width = Math.max(width, component.getWidth(font));
//			height += component.getHeight() + 2;
//		}
//		return new Vec2(width, height);
//	}
//
//	@Override
//	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
//		if (components.isEmpty()) {
//			return;
//		}
//		Font font = Minecraft.getInstance().font;
//		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
//		float preOffset = itemRenderer.blitOffset;
//		itemRenderer.blitOffset = 400;
//		RenderSystem.disableBlend();
//		RenderSystem.enableTexture();
//		MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//
//		int ly = (int) y;
//		for (ClientTooltipComponent component : components) {
//			component.renderText(font, (int) x, ly, matrixStack.last().pose(), multibuffersource$buffersource);
//			ly += component.getHeight() + 2;
//		}
//		multibuffersource$buffersource.endBatch();
//		ly = (int) y;
//		for (ClientTooltipComponent component : components) {
//			System.out.println(component);
//			component.renderImage(font, (int) x, ly, matrixStack, itemRenderer, 400);
//			ly += component.getHeight() + 2;
//		}
//		itemRenderer.blitOffset = preOffset;
//	}
//
//}
