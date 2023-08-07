package snownee.jade.api.view;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ITooltipRenderer;
import snownee.jade.impl.ui.HorizontalLineElement;
import snownee.jade.impl.ui.ScaledTextElement;

public class ClientViewGroup<T> {

	public final List<T> views;
	@Nullable
	public Component title;
	public int bgColor;
	public int progressColor;
	public float progress;

	public ClientViewGroup(List<T> views) {
		this.views = views;
	}

	public boolean shouldRenderGroup() {
		return title != null || bgColor != 0 || progressColor != 0;
	}

	public static <IN, OUT> List<ClientViewGroup<OUT>> map(List<ViewGroup<IN>> groups, Function<IN, OUT> itemFactory, @Nullable BiConsumer<ViewGroup<IN>, ClientViewGroup<OUT>> clientGroupDecorator) {
		return groups.stream().map($ -> {
			var group = new ClientViewGroup<>($.views.stream().map(itemFactory).filter(Objects::nonNull).toList());
			if ($.extraData != null && $.getExtraData().contains("Progress")) {
				group.progress = $.getExtraData().getFloat("Progress");
				group.progressColor = 0xFFCCCCCC;
			}
			if (clientGroupDecorator != null) {
				clientGroupDecorator.accept($, group);
			}
			return group;
		}).toList();
	}

	public static <T> void tooltip(ITooltip tooltip, List<ClientViewGroup<T>> groups, boolean renderGroup, BiConsumer<ITooltip, ClientViewGroup<T>> consumer) {
		for (var group : groups) {
			ITooltip theTooltip = renderGroup ? IElementHelper.get().tooltip() : tooltip;
			consumer.accept(theTooltip, group);
			if (renderGroup) {
				var boxStyle = BoxStyle.createGradientBorder();
				boxStyle.borderColor = group.bgColor;
				boxStyle.bgColor = group.bgColor;
				boxStyle.progress = group.progress;
				boxStyle.progressColor = group.progressColor;
				boxStyle.borderWidth = 0.75F;
				boxStyle.roundCorner = !IWailaConfig.get().getOverlay().getSquare();
				IBoxElement box = IElementHelper.get().box(theTooltip, boxStyle);
				ITooltipRenderer tooltipRenderer = box.getTooltipRenderer();
				tooltipRenderer.setPadding(ITooltipRenderer.TOP, group.title == null ? 2 : 0);
				tooltipRenderer.setPadding(ITooltipRenderer.LEFT, 2);
				tooltipRenderer.setPadding(ITooltipRenderer.RIGHT, 2);
				tooltipRenderer.recalculateSize();
				tooltip.add(box);
			}
		}
	}

	public void renderHeader(ITooltip tooltip) {
		if (title != null) {
			tooltip.add(new HorizontalLineElement());
			tooltip.append(new ScaledTextElement(title, 0.5F));
			tooltip.append(new HorizontalLineElement());
		} else if (bgColor == 0) {
			tooltip.add(new HorizontalLineElement());
		}
	}
}
