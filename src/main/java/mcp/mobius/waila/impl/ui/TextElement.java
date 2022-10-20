package mcp.mobius.waila.impl.ui;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.config.WailaConfig;
import mcp.mobius.waila.api.ui.Element;
import mcp.mobius.waila.overlay.DisplayHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextElement extends Element {

	public final Component component;

	public TextElement(Component component) {
		this.component = component;
	}

	@Override
	public Vec2 getSize() {
		Font font = Minecraft.getInstance().font;
		return new Vec2(font.width(component), font.lineHeight + 1);
	}

	@Override
	public void render(PoseStack matrixStack, float x, float y, float maxX, float maxY) {
		WailaConfig.ConfigOverlay.ConfigOverlayColor color = Waila.CONFIG.get().getOverlay().getColor();
		DisplayHelper.INSTANCE.drawText(matrixStack, component, x, y, color.getTheme().textColor);
	}

	@Override
	public @Nullable Component getMessage() {
		return component;
	}

}
