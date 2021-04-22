package snownee.jade.client.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.ITooltipRenderer;
import mcp.mobius.waila.api.RenderContext;
import mcp.mobius.waila.overlay.Tooltip;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import snownee.jade.util.HackyTextComponentNBT;

@OnlyIn(Dist.CLIENT)
public class BoxTooltipRenderer implements ITooltipRenderer {
	private final Cache<CompoundNBT, Tooltip> cachedTooltips = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();

	private static Field FIELD_SIZE;

	static {
		try {
			FIELD_SIZE = Tooltip.class.getDeclaredField("totalSize");
			FIELD_SIZE.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Dimension getSize(CompoundNBT nbt, ICommonAccessor accessor) {
		Tooltip tooltip = geTooltip(nbt);
		try {
			Dimension dimension = new Dimension((Dimension) FIELD_SIZE.get(tooltip));
			dimension.height += 4;
			dimension.width += 2;
			return dimension;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return new Dimension();
		}
	}

	@Override
	public void draw(CompoundNBT nbt, ICommonAccessor accessor, int x, int y) {
		Tooltip tooltip = geTooltip(nbt);
		if (tooltip == null) {
			return;
		}
		Rectangle rect = tooltip.getPosition();
		RenderSystem.enableBlend();
		int color = Color.GRAY.getRGB();
		MatrixStack matrix = RenderContext.matrixStack;
		matrix.push();
		matrix.translate(x, y, 0);
		AbstractGui.fill(matrix, 0, 0, 1, rect.height, color);
		AbstractGui.fill(matrix, 0, 0, rect.width, 1, color);
		AbstractGui.fill(matrix, rect.width, 0, rect.width + 1, rect.height, color);
		AbstractGui.fill(matrix, 0, rect.height, rect.width + 1, rect.height + 1, color);
		matrix.translate(-rect.x, -rect.y, 0);
		tooltip.draw();
		matrix.pop();
	}

	private Tooltip geTooltip(CompoundNBT nbt) {
		try {
			return cachedTooltips.get(nbt, () -> {
				ListNBT tags = nbt.getList("in", Constants.NBT.TAG_STRING);
				List<ITextComponent> components = tags.stream().map(tag -> ((HackyTextComponentNBT) tag).getTextComponent()).collect(Collectors.toList());
				return new Tooltip(components, false);
			});
		} catch (ExecutionException e) {
			e.printStackTrace();
			return new Tooltip(Collections.EMPTY_LIST, false);
		}
	}

}
