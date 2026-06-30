package snownee.jade.api.view;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.MessageType;
import snownee.jade.impl.ui.HorizontalLineElement;

/**
 * Client-side rendering state derived from a {@link ViewGroup}.
 *
 * @param <T> contained client view type
 */
public class ClientViewGroup<T> {

	/**
	 * Contained client views.
	 */
	public final List<T> views;
	/**
	 * Optional group title.
	 */
	@Nullable
	public Component title;
	/**
	 * Message severity for this group.
	 */
	public MessageType messageType = MessageType.NORMAL;
	/**
	 * Progress value copied from the server payload.
	 */
	public float boxProgress;
	/**
	 * Optional additional group data.
	 */
	@Nullable
	public CompoundTag extraData;

	/**
	 * Creates a new client group.
	 *
	 * @param views contained client views
	 */
	public ClientViewGroup(List<T> views) {
		this.views = views;
	}

	/**
	 * Maps server groups to client groups.
	 *
	 * @param groups server groups
	 * @param itemFactory mapper for individual views
	 * @param clientGroupDecorator optional client-side customization hook
	 * @param <IN> server view type
	 * @param <OUT> client view type
	 * @return mapped client groups
	 */
	public static <IN, OUT> List<ClientViewGroup<OUT>> map(
			List<ViewGroup<IN>> groups,
			Function<IN, @Nullable OUT> itemFactory,
			@Nullable BiConsumer<ViewGroup<IN>, ClientViewGroup<OUT>> clientGroupDecorator) {
		return groups.stream().map($ -> {
			var group = new ClientViewGroup<>($.views.stream().map(itemFactory).filter(Objects::nonNull).toList());
			CompoundTag data = $.extraData;
			if (data != null) {
				group.boxProgress = data.getFloatOr("Progress", 0F);
				group.messageType = data.getString("MessageType").map(MessageType::parse).orElse(MessageType.NORMAL);
			}
			if (clientGroupDecorator != null) {
				clientGroupDecorator.accept($, group);
			}
			group.extraData = data;
			return group;
		}).toList();
	}

	/**
	 * Renders this group into a tooltip.
	 *
	 * @param tooltip tooltip to write to
	 */
	public static <T> void tooltip(
			ITooltip tooltip,
			List<ClientViewGroup<T>> groups,
			boolean renderGroup,
			BiConsumer<ITooltip, ClientViewGroup<T>> consumer) {
		for (var group : groups) {
			ITooltip theTooltip = renderGroup ? JadeUI.tooltip() : tooltip;
			consumer.accept(theTooltip, group);
			if (renderGroup) {
				BoxStyle boxStyle = BoxStyle.viewGroup().copy();
				BoxElement box = JadeUI.box(theTooltip, boxStyle);
//				box.setBoxProgress(group.messageType, group.boxProgress); //TODO
//				if (group.title != null) {
//					box.setPadding(ScreenDirection.UP, 0);
//					box.size(null);
//				}
				tooltip.add(box.flexGrow(1));
			}
		}
	}

	/**
	 * Returns whether this group should render a header.
	 *
	 * @return {@code true} if a title or progress is present
	 */
	public boolean shouldRenderGroup() {
		return title != null || boxProgress > 0;
	}

	/**
	 * Renders the group header into the tooltip.
	 *
	 * @param tooltip tooltip to modify
	 */
	public void renderHeader(ITooltip tooltip) {
		if (title != null) {
			tooltip.add(new HorizontalLineElement());
			tooltip.append(JadeUI.text(title).scale(0.5F));
			tooltip.append(new HorizontalLineElement().flexGrow(1));
		}
//		else if (bgColor == 0) {
//			tooltip.add(new HorizontalLineElement());
//		}
	}
}
