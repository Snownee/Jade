package snownee.jade.api.view;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.MessageType;
import snownee.jade.api.ui.ProgressStyle;

/**
 * Client-side representation of a progress bar or multi-part progress overlay.
 */
public class ProgressView {

	/**
	 * Progress parts to render.
	 */
	public List<Part> parts = List.of();
	/**
	 * Progress bar style.
	 */
	public final ProgressStyle style;
	/**
	 * Surrounding box style.
	 */
	public final BoxStyle boxStyle;
	/**
	 * Optional text label.
	 */
	public @Nullable Component text;

	/**
	 * Creates a progress view with the given styles.
	 *
	 * @param style progress style
	 * @param boxStyle container box style
	 */
	public ProgressView(ProgressStyle style, BoxStyle boxStyle) {
		this.style = Objects.requireNonNull(style);
		this.boxStyle = Objects.requireNonNull(boxStyle);
	}

	/**
	 * Creates a progress view from a mapped stream.
	 *
	 * @param style progress style
	 * @param boxStyle container box style
	 * @param items source items
	 * @param mapper item-to-part mapper
	 * @param <T> source item type
	 */
	public <T> ProgressView(ProgressStyle style, BoxStyle boxStyle, Stream<T> items, Function<T, Part> mapper) {
		this(style, boxStyle);
		this.parts = items.map(mapper).toList();
	}

	/**
	 * Creates a single-part progress view.
	 *
	 * @param progress progress part
	 * @param text optional label
	 * @param style progress style
	 * @param boxStyle container box style
	 */
	public ProgressView(ProgressView.Part progress, @Nullable Component text, ProgressStyle style, BoxStyle boxStyle) {
		this(List.of(progress), text, style, boxStyle);
	}

	/**
	 * Creates a multi-part progress view.
	 *
	 * @param progress progress parts
	 * @param text optional label
	 * @param style progress style
	 * @param boxStyle container box style
	 */
	public ProgressView(List<ProgressView.Part> progress, @Nullable Component text, ProgressStyle style, BoxStyle boxStyle) {
		this(style, boxStyle);
		this.parts = Objects.requireNonNull(progress);
		this.text = text;
	}

	/**
	 * Builds a view from serialized data.
	 *
	 * @param data serialized progress data
	 * @return progress view
	 */
	public static ProgressView read(Data data) {
		ProgressView view = new ProgressView(JadeUI.progressStyle(), BoxStyle.nestedBox());
		view.parts = List.of(new PartBuilder()
				.progress(data.progress)
				.target(data.speed, data.target)
				.messageType(data.messageType)
				.build());
		return view;
	}

	/**
	 * Serialized progress payload.
	 */
	public record Data(float progress, float speed, float target, MessageType messageType) {
		public static final StreamCodec<ByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.FLOAT,
				Data::progress,
				ByteBufCodecs.FLOAT,
				Data::speed,
				ByteBufCodecs.FLOAT,
				Data::target,
				MessageType.STREAM_CODEC,
				Data::messageType,
				Data::new);

		/**
		 * Creates an info-style progress payload.
		 *
		 * @param progress progress value
		 */
		public Data(float progress) {
			this(progress, MessageType.INFO);
		}

		/**
		 * Creates an info-style progress payload with speed and target.
		 *
		 * @param progress progress value
		 * @param speed current speed
		 * @param target target value
		 */
		public Data(float progress, float speed, float target) {
			this(progress, speed, target, MessageType.INFO);
		}

		/**
		 * Creates a progress payload with an explicit message type.
		 *
		 * @param progress progress value
		 * @param messageType severity/type
		 */
		public Data(float progress, MessageType messageType) {
			this(progress, Float.NaN, Float.NaN, messageType);
		}
	}

		/**
		 * One rendered segment of the progress bar.
		 */
	public record Part(
			int id,
			float progress,
			float speed,
			float target,
			@Nullable Element overlay,
			@Nullable MessageType messageType,
			int color) {
		/**
		 * Creates a fully specified part.
		 *
		 * @param id part id
		 * @param progress progress value
		 * @param speed progress speed
		 * @param target target value
		 * @param overlay optional overlay element
		 * @param messageType optional severity/type
		 * @param color explicit color override
		 */
		public Part(
				int id,
				float progress,
				float speed,
				float target,
				@Nullable Element overlay,
				@Nullable MessageType messageType,
				int color) {
			this.id = id;
			this.progress = progress;
			this.speed = speed;
			this.target = target;
			this.overlay = overlay;
			this.messageType = messageType;
			this.color = color;
			Preconditions.checkArgument(progress >= 0 && progress <= 1, "Progress must be between 0 and 1, got: %s", progress);
		}

		/**
		 * Creates a simple part with no target metadata.
		 */
		public Part(int id, float progress, @Nullable Element overlay, @Nullable MessageType messageType, int color) {
			this(id, progress, Float.NaN, Float.NaN, overlay, messageType, color);
		}

		/**
		 * Creates a default part.
		 *
		 * @param progress progress value
		 * @return the part
		 */
		public static Part of(float progress) {
			return of(0, progress);
		}

		/**
		 * Creates a default part with an explicit id.
		 *
		 * @param id part id
		 * @param progress progress value
		 * @return the part
		 */
		public static Part of(int id, float progress) {
			return of(id, progress, MessageType.INFO);
		}

		/**
		 * Creates a part with an explicit message type.
		 *
		 * @param progress progress value
		 * @param messageType severity/type
		 * @return the part
		 */
		public static Part of(float progress, MessageType messageType) {
			return of(0, progress, messageType);
		}

		/**
		 * Creates a part with an explicit id and message type.
		 *
		 * @param id part id
		 * @param progress progress value
		 * @param messageType severity/type
		 * @return the part
		 */
		public static Part of(int id, float progress, MessageType messageType) {
			return new Part(id, progress, null, messageType, -1);
		}

		/**
		 * Creates a part with an overlay element.
		 *
		 * @param progress progress value
		 * @param overlay overlay element
		 * @return the part
		 */
		public static Part of(float progress, Element overlay) {
			return new Part(0, progress, overlay, null, -1);
		}

		/**
		 * Creates a part with an explicit color.
		 *
		 * @param progress progress value
		 * @param color explicit color override
		 * @return the part
		 */
		public static Part of(float progress, int color) {
			return new Part(0, progress, null, null, color);
		}

		/**
		 * Returns the effective color for this part.
		 *
		 * @return the resolved color, or {@code -1}
		 */
		public int themeColor() {
			if (color != -1) {
				return color;
			}
			if (messageType != null) {
				return IThemeHelper.get().theme().progressColors.get(messageType);
			}
			return -1;
		}

		/**
		 * Returns whether speed and target values are defined.
		 *
		 * @return {@code true} if target metadata is present
		 */
		public boolean targetDefined() {
			return !Float.isNaN(target) && !Float.isNaN(speed);
		}
	}

		/**
		 * Builder for {@link Part} instances.
		 */
	public static class PartBuilder {
		int id;
		float progress;
		float speed;
		float target;
		@Nullable Element overlay;
		@Nullable MessageType messageType;
		int color = -1;

		/**
		 * Sets the part id.
		 *
		 * @param id part id
		 * @return this builder
		 */
		public PartBuilder id(int id) {
			this.id = id;
			return this;
		}

		/**
		 * Sets the progress value.
		 *
		 * @param progress progress value
		 * @return this builder
		 */
		public PartBuilder progress(float progress) {
			this.progress = progress;
			return this;
		}

		/**
		 * Sets the speed value.
		 *
		 * @param speed progress speed
		 * @return this builder
		 */
		@Deprecated
		public PartBuilder speed(float speed) {
			this.speed = speed;
			return this;
		}

		/**
		 * Sets the target value.
		 *
		 * @param target target value
		 * @return this builder
		 */
		@Deprecated
		public PartBuilder target(float target) {
			this.target = target;
			return this;
		}

		/**
		 * Sets the speed and target values.
		 *
		 * @param speed progress speed
		 * @param target target value
		 * @return this builder
		 */
		public PartBuilder target(float speed, float target) {
			this.speed = speed;
			this.target = target;
			return this;
		}

		/**
		 * Sets the overlay element.
		 *
		 * @param overlay overlay element
		 * @return this builder
		 */
		public PartBuilder overlay(Element overlay) {
			this.overlay = overlay;
			return this;
		}

		/**
		 * Sets the message type.
		 *
		 * @param messageType severity/type
		 * @return this builder
		 */
		public PartBuilder messageType(MessageType messageType) {
			this.messageType = messageType;
			return this;
		}

		/**
		 * Sets the explicit color.
		 *
		 * @param color color override
		 * @return this builder
		 */
		public PartBuilder color(int color) {
			this.color = color;
			return this;
		}

		/**
		 * Builds the part.
		 *
		 * @return the built part
		 */
		public Part build() {
			return new Part(id, progress, speed, target, overlay, messageType, color);
		}
	}
}
