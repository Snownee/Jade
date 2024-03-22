package snownee.jade.api.view;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.MessageType;
import snownee.jade.impl.ui.HorizontalLineElement;

public class ClientViewGroup<T> {

	public final List<T> views;
	@Nullable
	public Component title;
	public MessageType messageType = MessageType.NORMAL;
	public float boxProgress;
	@Nullable
	public CompoundTag extraData;

	public ClientViewGroup(List<T> views) {
		this.views = views;
	}

	public static <IN, OUT> List<ClientViewGroup<OUT>> map(
			List<ViewGroup<IN>> groups,
			Function<IN, OUT> itemFactory,
			@Nullable BiConsumer<ViewGroup<IN>, ClientViewGroup<OUT>> clientGroupDecorator) {
		return groups.stream().map($ -> {
			var group = new ClientViewGroup<>($.views.stream().map(itemFactory).filter(Objects::nonNull).toList());
			CompoundTag data = $.extraData;
			if (data != null) {
				if (data.contains("Progress")) {
					group.boxProgress = data.getFloat("Progress");
				}
				if (data.contains("MessageType")) {
					group.messageType = MessageType.parse(data.getString("MessageType"));
				}
			}
			if (clientGroupDecorator != null) {
				clientGroupDecorator.accept($, group);
			}
			group.extraData = data;
			return group;
		}).toList();
	}

	public static <T> void tooltip(
			ITooltip tooltip,
			List<ClientViewGroup<T>> groups,
			boolean renderGroup,
			BiConsumer<ITooltip, ClientViewGroup<T>> consumer) {
		for (var group : groups) {
			ITooltip theTooltip = renderGroup ? IElementHelper.get().tooltip() : tooltip;
			consumer.accept(theTooltip, group);
			if (renderGroup) {
				BoxStyle boxStyle = BoxStyle.getViewGroup().clone();
				IBoxElement box = IElementHelper.get().box(theTooltip, boxStyle);
				box.setBoxProgress(group.messageType, group.boxProgress);
				if (group.title != null) {
					box.setPadding(Direction2D.UP, 0);
					box.size(null);
				}
				tooltip.add(box);
				if (box.getStyle().hasRoundCorner()) {
					tooltip.setLineMargin(-1, Direction2D.UP, 3);
					tooltip.setLineMargin(-1, Direction2D.DOWN, 3);
				}
			}
		}
	}

	public boolean shouldRenderGroup() {
		return title != null || boxProgress > 0;
	}

	public void renderHeader(ITooltip tooltip) {
		if (title != null) {
			tooltip.add(new HorizontalLineElement());
			tooltip.append(IElementHelper.get().text(title).scale(0.5F));
			tooltip.append(new HorizontalLineElement());
		}
//		else if (bgColor == 0) {
//			tooltip.add(new HorizontalLineElement());
//		}
	}
}
